package com.sa.SmartAttendanceAI.service.attendance;

import org.springframework.stereotype.Service;

/**
 * ================================================================
 *  Attendance Confidence Index (ACI) — Calculator Service
 * ================================================================
 *
 *  ACI Score = Weighted combination of 3 real-time factors:
 *
 *    GPS Score  (40%) — How close student is to faculty position
 *    Face Score (45%) — OpenCV face match confidence
 *    Time Score (15%) — How early student checked in
 *
 *  Final Score: 0.0 to 100.0
 *
 *  Levels:
 *    HIGH        >= 80   Fully trusted, no doubt
 *    MEDIUM      >= 55   Acceptable, minor concern
 *    LOW         >= 35   Suspicious, faculty should review
 *    SUSPICIOUS  <  35   Flagged for investigation
 * ================================================================
 */
@Service
public class ACICalculatorService {

    /**
     * Calculate ACI score for a student check-in event.
     *
     * @param gpsDistanceMeters   Student's GPS distance from faculty (meters)
     * @param radiusMeters        Session geo-fence radius (meters)
     * @param faceConfidence      OpenCV face match score 0–100
     * @param secondsSinceStart   Seconds elapsed since session started
     * @param sessionDurationSec  Total expected session duration in seconds
     * @return ACI score (0.0–100.0), rounded to 1 decimal
     */
    public double calculate(double gpsDistanceMeters,
                            double radiusMeters,
                            double faceConfidence,
                            long secondsSinceStart,
                            long sessionDurationSec) {

        // GPS Score: student at center = 100, at boundary edge = 0
        double gpsScore = 100.0 - (gpsDistanceMeters / radiusMeters * 100.0);
        gpsScore = Math.max(0.0, Math.min(100.0, gpsScore));

        // Face Score: directly from OpenCV (0–100)
        double faceScore = Math.max(0.0, Math.min(100.0, faceConfidence));

        // Time Score: checked in at session start = 100, at end = 0
        double timeRatio = (sessionDurationSec > 0)
                ? (double) secondsSinceStart / sessionDurationSec
                : 1.0;
        double timeScore = Math.max(0.0, 100.0 - (timeRatio * 100.0));

        // Weighted ACI
        double aci = (gpsScore * 0.40) + (faceScore * 0.45) + (timeScore * 0.15);

        // Round to 1 decimal place
        return Math.round(aci * 10.0) / 10.0;
    }

    /**
     * Classify ACI score into a level label.
     */
    public String classify(double aciScore) {
        if (aciScore >= 80.0) return "HIGH";
        if (aciScore >= 55.0) return "MEDIUM";
        if (aciScore >= 35.0) return "LOW";
        return "SUSPICIOUS";
    }
}
