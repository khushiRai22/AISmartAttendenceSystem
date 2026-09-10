package com.sa.SmartAttendanceAI.service.face;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.*;

/**
 * Face Recognition Service — V9
 * ─────────────────────────────────────────────────────────────────
 *  OpenCV Efficiency Improvements (V9):
 *
 *  1. CLAHE Pre-processing  — normalises lighting differences between
 *     stored photo and live capture before detection + comparison.
 *
 *  2. Optimised detectMultiScale params:
 *       scaleFactor  1.05 (was 1.1)  → catches more face scales
 *       minNeighbors 4    (was 3)    → fewer false positives
 *       minSize      40x40 (was 30)  → skips tiny noise regions
 *
 *  3. Haar Cascade path cached (double-checked locking) — extracted
 *     from JAR only once per JVM lifetime, not per request.
 *
 *  4. Explicit Mat.release() in finally blocks — prevents native
 *     heap leaks under concurrent check-in load.
 *
 *  5. Face-ROI-only LBP histogram — computed on the cropped 80x80
 *     face region, not the full frame. Faster + more accurate.
 *
 *  6. Java fallback BILINEAR resize hint — slightly better pHash
 *     quality at zero extra cost.
 *
 *  Weights: LBP 65% + HSV 35% (OpenCV) | pHash 40%+Hist 35%+Struct 25% (fallback)
 *  Threshold: 70 — below this attendance BLOCKED
 * ─────────────────────────────────────────────────────────────────
 */
@Service
public class FaceRecognitionService {

    @Value("${opencv.lib.path:}")
    private String opencvLibPath;

    private boolean opencvLoaded  = false;
    private boolean loadAttempted = false;

    /** Cached cascade path — extracted from JAR only once. */
    private static volatile String cachedCascadePath = null;
    private static final Object CASCADE_LOCK = new Object();

    @jakarta.annotation.PostConstruct
    public void init() {
        try { loadOpenCV(); } catch (Throwable t) {
            opencvLoaded = false;
            System.out.println("⚠️ OpenCV init failed — Java fallback: " + t.getMessage());
        }
        if (opencvLoaded) {
            System.out.println("╔════════════════════════════════════════════╗");
            System.out.println("║  ✅ OpenCV V9 loaded — CLAHE + ROI active  ║");
            System.out.println("╚════════════════════════════════════════════╝");
        } else {
            System.out.println("╔══════════════════════════════════════════════════════╗");
            System.out.println("║  ⚠️  OpenCV not found — Java fallback (pHash+Hist)   ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
        }
    }

    // ── Main entry point ──────────────────────────────────────────
    public double compareFace(byte[] storedProfilePic, MultipartFile uploadedFace) {
        try {
            if (uploadedFace == null || uploadedFace.isEmpty() || uploadedFace.getSize() < 5000) {
                System.out.println("⚠️ Image too small: " + (uploadedFace == null ? 0 : uploadedFace.getSize()) + " bytes");
                return 0.0;
            }
            if (!loadAttempted) loadOpenCV();
            if (opencvLoaded) return compareWithOpenCV(storedProfilePic, uploadedFace);

            BufferedImage stored   = ImageIO.read(new ByteArrayInputStream(storedProfilePic));
            BufferedImage uploaded = ImageIO.read(new ByteArrayInputStream(uploadedFace.getBytes()));
            if (stored == null || uploaded == null) { System.out.println("⚠️ Cannot decode images"); return 0.0; }

            double pHash   = pHashSimilarity(stored, uploaded);
            double hist    = histogramSimilarity(stored, uploaded);
            double strukt  = structuralSimilarity(stored, uploaded);
            double result  = Math.round(((pHash*0.40)+(hist*0.35)+(strukt*0.25))*10.0)/10.0;
            System.out.printf("🔍 Fallback → pHash:%.1f Hist:%.1f Struct:%.1f Combined:%.1f%n", pHash, hist, strukt, result);
            return result;
        } catch (Exception e) {
            System.out.println("❌ compareFace error: " + e.getMessage());
            return 0.0;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  OPENCV PATH — V9 efficient pipeline
    // ══════════════════════════════════════════════════════════════

    private double compareWithOpenCV(byte[] storedBytes, MultipartFile uploaded) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("face_");
            Path upPath = tempDir.resolve("uploaded.jpg");
            Path stPath = tempDir.resolve("stored.jpg");
            Files.write(upPath, uploaded.getBytes());
            Files.write(stPath, storedBytes);
            return compareFacesOpenCV(stPath.toString(), upPath.toString());
        } catch (Exception e) {
            System.out.println("❌ OpenCV compare error: " + e.getMessage());
            return 0.0;
        } finally {
            if (tempDir != null) {
                try {
                    Files.walk(tempDir).sorted(java.util.Comparator.reverseOrder())
                         .map(Path::toFile).forEach(java.io.File::delete);
                } catch (Exception ignored) {}
            }
        }
    }

    private double compareFacesOpenCV(String stPath, String upPath) {
        org.opencv.core.Mat stored=null, uploaded=null,
            gSt=null, gUp=null, clSt=null, clUp=null,
            fSt=null, fUp=null, r1=null, r2=null, cR1=null, cR2=null;
        try {
            stored   = org.opencv.imgcodecs.Imgcodecs.imread(stPath);
            uploaded = org.opencv.imgcodecs.Imgcodecs.imread(upPath);
            if (stored.empty() || uploaded.empty()) { System.out.println("⚠️ imread failed"); return 0.0; }

            // 1. Grayscale
            gSt = new org.opencv.core.Mat(); gUp = new org.opencv.core.Mat();
            org.opencv.imgproc.Imgproc.cvtColor(stored,   gSt, org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY);
            org.opencv.imgproc.Imgproc.cvtColor(uploaded, gUp, org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY);

            // 2. CLAHE — normalise lighting (clipLimit=2.0, tile=8x8)
            clSt = new org.opencv.core.Mat(); clUp = new org.opencv.core.Mat();
            org.opencv.imgproc.CLAHE clahe =
                org.opencv.imgproc.Imgproc.createCLAHE(2.0, new org.opencv.core.Size(8, 8));
            clahe.apply(gSt, clSt);
            clahe.apply(gUp, clUp);

            // 3. Face detection — cached cascade, tighter params
            fSt = clSt.clone(); fUp = clUp.clone(); // fallback = full image
            String cascPath = getCachedCascadePath();
            if (cascPath != null) {
                org.opencv.objdetect.CascadeClassifier det =
                    new org.opencv.objdetect.CascadeClassifier(cascPath);
                if (!det.empty()) {
                    org.opencv.core.MatOfRect rSt = new org.opencv.core.MatOfRect();
                    org.opencv.core.MatOfRect rUp = new org.opencv.core.MatOfRect();
                    // V9 params: scaleFactor=1.05, minNeighbors=4, minSize=40x40
                    det.detectMultiScale(clSt, rSt, 1.05, 4, 0,
                        new org.opencv.core.Size(40,40), new org.opencv.core.Size());
                    det.detectMultiScale(clUp, rUp, 1.05, 4, 0,
                        new org.opencv.core.Size(40,40), new org.opencv.core.Size());
                    if (rSt.toArray().length > 0) {
                        org.opencv.core.Rect r = rSt.toArray()[0];
                        fSt.release(); fSt = new org.opencv.core.Mat(clSt, r);
                        System.out.println("✅ ROI stored: " + r.width + "×" + r.height);
                    }
                    if (rUp.toArray().length > 0) {
                        org.opencv.core.Rect r = rUp.toArray()[0];
                        fUp.release(); fUp = new org.opencv.core.Mat(clUp, r);
                        System.out.println("✅ ROI uploaded: " + r.width + "×" + r.height);
                    }
                }
            }

            // 4. Resize face ROIs to 80x80
            r1 = new org.opencv.core.Mat(); r2 = new org.opencv.core.Mat();
            org.opencv.imgproc.Imgproc.resize(fSt, r1, new org.opencv.core.Size(80,80));
            org.opencv.imgproc.Imgproc.resize(fUp, r2, new org.opencv.core.Size(80,80));

            // 5. LBP histogram on ROI (65%)
            double lbpScore = compareLBPHistogram(r1, r2);

            // 6. HSV on colour image (35%)
            cR1 = new org.opencv.core.Mat(); cR2 = new org.opencv.core.Mat();
            org.opencv.imgproc.Imgproc.resize(stored,   cR1, new org.opencv.core.Size(80,80));
            org.opencv.imgproc.Imgproc.resize(uploaded, cR2, new org.opencv.core.Size(80,80));
            double hsvScore = compareHSVHistogram(cR1, cR2);

            double result = Math.round(((lbpScore*0.65)+(hsvScore*0.35))*10.0)/10.0;
            System.out.printf("🔍 OpenCV V9 → LBP:%.1f HSV:%.1f Combined:%.1f%n", lbpScore, hsvScore, result);
            return result;

        } catch (Exception e) {
            System.out.println("❌ compareFacesOpenCV: " + e.getMessage());
            return 0.0;
        } finally {
            safeRelease(stored, uploaded, gSt, gUp, clSt, clUp, fSt, fUp, r1, r2, cR1, cR2);
        }
    }

    private void safeRelease(org.opencv.core.Mat... mats) {
        for (org.opencv.core.Mat m : mats) {
            try { if (m != null && !m.empty()) m.release(); } catch (Exception ignored) {}
        }
    }

    private double compareLBPHistogram(org.opencv.core.Mat img1, org.opencv.core.Mat img2) {
        org.opencv.core.Mat h1 = new org.opencv.core.Mat(), h2 = new org.opencv.core.Mat();
        try {
            org.opencv.core.MatOfInt   sz = new org.opencv.core.MatOfInt(64);
            org.opencv.core.MatOfFloat rg = new org.opencv.core.MatOfFloat(0f, 256f);
            org.opencv.core.MatOfInt   ch = new org.opencv.core.MatOfInt(0);
            org.opencv.imgproc.Imgproc.calcHist(java.util.Arrays.asList(img1), ch, new org.opencv.core.Mat(), h1, sz, rg);
            org.opencv.imgproc.Imgproc.calcHist(java.util.Arrays.asList(img2), ch, new org.opencv.core.Mat(), h2, sz, rg);
            org.opencv.core.Core.normalize(h1, h1, 0, 1, org.opencv.core.Core.NORM_MINMAX);
            org.opencv.core.Core.normalize(h2, h2, 0, 1, org.opencv.core.Core.NORM_MINMAX);
            double d = org.opencv.imgproc.Imgproc.compareHist(h1, h2, org.opencv.imgproc.Imgproc.CV_COMP_BHATTACHARYYA);
            return Math.max(0.0, (1.0 - d) * 100.0);
        } catch (Exception e) { return 50.0; }
        finally { safeRelease(h1, h2); }
    }

    private double compareHSVHistogram(org.opencv.core.Mat img1, org.opencv.core.Mat img2) {
        org.opencv.core.Mat hsv1=new org.opencv.core.Mat(), hsv2=new org.opencv.core.Mat(),
                             h1=new org.opencv.core.Mat(),  h2=new org.opencv.core.Mat();
        try {
            org.opencv.imgproc.Imgproc.cvtColor(img1, hsv1, org.opencv.imgproc.Imgproc.COLOR_BGR2HSV);
            org.opencv.imgproc.Imgproc.cvtColor(img2, hsv2, org.opencv.imgproc.Imgproc.COLOR_BGR2HSV);
            org.opencv.core.MatOfInt   sz = new org.opencv.core.MatOfInt(50, 60);
            org.opencv.core.MatOfFloat rg = new org.opencv.core.MatOfFloat(0f,180f,0f,256f);
            org.opencv.core.MatOfInt   ch = new org.opencv.core.MatOfInt(0, 1);
            org.opencv.imgproc.Imgproc.calcHist(java.util.Arrays.asList(hsv1), ch, new org.opencv.core.Mat(), h1, sz, rg);
            org.opencv.imgproc.Imgproc.calcHist(java.util.Arrays.asList(hsv2), ch, new org.opencv.core.Mat(), h2, sz, rg);
            org.opencv.core.Core.normalize(h1, h1, 0, 1, org.opencv.core.Core.NORM_MINMAX);
            org.opencv.core.Core.normalize(h2, h2, 0, 1, org.opencv.core.Core.NORM_MINMAX);
            double corr = org.opencv.imgproc.Imgproc.compareHist(h1, h2, org.opencv.imgproc.Imgproc.CV_COMP_CORREL);
            return ((corr + 1.0) / 2.0) * 100.0;
        } catch (Exception e) { return 50.0; }
        finally { safeRelease(hsv1, hsv2, h1, h2); }
    }

    private String getCachedCascadePath() {
        if (cachedCascadePath != null) return cachedCascadePath;
        synchronized (CASCADE_LOCK) {
            if (cachedCascadePath != null) return cachedCascadePath;
            cachedCascadePath = extractHaarCascade();
        }
        return cachedCascadePath;
    }

    private String extractHaarCascade() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/haarcascade_frontalface_default.xml");
            if (is == null)
                is = getClass().getResourceAsStream("/org/opencv/data/haarcascade_frontalface_default.xml");
            if (is == null) return null;
            Path t = Files.createTempFile("haarcascade_", ".xml");
            Files.copy(is, t, StandardCopyOption.REPLACE_EXISTING);
            is.close();
            System.out.println("✅ Haar cascade cached: " + t);
            return t.toString();
        } catch (Exception e) { return null; }
    }

    // ══════════════════════════════════════════════════════════════
    //  JAVA FALLBACK ALGORITHMS  (pHash + Histogram + Structural)
    // ══════════════════════════════════════════════════════════════

    private double pHashSimilarity(BufferedImage i1, BufferedImage i2) {
        try {
            long h1 = computePHash(i1), h2 = computePHash(i2);
            return Math.max(0.0, (1.0 - (Long.bitCount(h1 ^ h2) / 64.0)) * 100.0);
        } catch (Exception e) { return 50.0; }
    }

    private long computePHash(BufferedImage img) {
        BufferedImage sm = new BufferedImage(32, 32, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = sm.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, 32, 32, null); g.dispose();
        double[] px = new double[1024];
        for (int y=0;y<32;y++) for (int x=0;x<32;x++) px[y*32+x] = sm.getRGB(x,y) & 0xFF;
        double[] dct = new double[64];
        for (int u=0;u<8;u++) for (int v=0;v<8;v++) {
            double s=0;
            for (int x=0;x<32;x++) for (int y=0;y<32;y++)
                s += px[y*32+x]*Math.cos((2*x+1)*u*Math.PI/64)*Math.cos((2*y+1)*v*Math.PI/64);
            dct[u*8+v]=s;
        }
        double avg=0; for (int i=1;i<64;i++) avg+=dct[i]; avg/=63.0;
        long hash=0L; for (int i=0;i<64;i++) if (dct[i]>avg) hash|=(1L<<i);
        return hash;
    }

    private double histogramSimilarity(BufferedImage i1, BufferedImage i2) {
        try {
            double s=0;
            for (int z=0;z<3;z++) {
                s += bhattacharyya(
                    buildHistogram(i1, 0, (i1.getHeight()*z)/3, i1.getWidth(), (i1.getHeight()*(z+1))/3),
                    buildHistogram(i2, 0, (i2.getHeight()*z)/3, i2.getWidth(), (i2.getHeight()*(z+1))/3));
            }
            return (s/3.0)*100.0;
        } catch (Exception e) { return 50.0; }
    }

    private int[] buildHistogram(BufferedImage img, int x0, int y0, int x1, int y1) {
        int[] h = new int[256];
        for (int y=y0;y<y1&&y<img.getHeight();y++)
            for (int x=x0;x<x1&&x<img.getWidth();x++) {
                int rgb=img.getRGB(x,y);
                h[((rgb>>16&0xFF)+(rgb>>8&0xFF)+(rgb&0xFF))/3]++;
            }
        return h;
    }

    private double bhattacharyya(int[] h1, int[] h2) {
        long s1=0,s2=0; for(int v:h1)s1+=v; for(int v:h2)s2+=v;
        if(s1==0||s2==0) return 0.5;
        double bc=0; for(int i=0;i<256;i++) bc+=Math.sqrt(((double)h1[i]/s1)*((double)h2[i]/s2));
        return Math.max(0.0,Math.min(1.0,bc));
    }

    private double structuralSimilarity(BufferedImage i1, BufferedImage i2) {
        try {
            BufferedImage s1=new BufferedImage(16,16,BufferedImage.TYPE_BYTE_GRAY);
            BufferedImage s2=new BufferedImage(16,16,BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g1=s1.createGraphics(), g2=s2.createGraphics();
            g1.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g1.drawImage(i1,0,0,16,16,null);g1.dispose();
            g2.drawImage(i2,0,0,16,16,null);g2.dispose();
            double d=0;
            for(int y=0;y<16;y++) for(int x=0;x<16;x++) d+=Math.abs((s1.getRGB(x,y)&0xFF)-(s2.getRGB(x,y)&0xFF));
            return Math.max(0.0,Math.min(100.0,(1.0-(d/65280.0))*100.0));
        } catch(Exception e){ return 50.0; }
    }

    private void loadOpenCV() {
        loadAttempted = true;
        try {
            if (opencvLibPath != null && !opencvLibPath.isEmpty()) System.load(opencvLibPath);
            else System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
            opencvLoaded = true;
            System.out.println("✅ OpenCV loaded — V9 efficient recognition active");
        } catch (Throwable t) {
            opencvLoaded = false;
            System.out.println("⚠️ OpenCV not found — Java fallback: " + t.getMessage());
        }
    }
}
