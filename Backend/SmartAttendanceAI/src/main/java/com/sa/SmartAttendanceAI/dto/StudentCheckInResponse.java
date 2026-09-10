package com.sa.SmartAttendanceAI.dto;

/**
 * Response returned to student after check-in attempt.
 * failReason tells frontend exactly WHY it failed:
 *   NO_SESSION       — no active class session
 *   WINDOW_CLOSED    — check-in time window passed
 *   GPS_OUT_OF_RANGE — student outside classroom
 *   FACE_MISMATCH    — face did not match profile photo
 *   NO_PROFILE_PHOTO — student has no registered photo
 *   DUPLICATE        — already marked today
 *   SERVER_ERROR     — unexpected error
 */
public class StudentCheckInResponse {

    private boolean success;
    private String message;
    private String failReason;       // NEW — frontend uses this for icon/color
    private Double aciScore;
    private String aciLevel;
    private Double gpsDistanceMeters;
    private Double faceConfidence;   // NEW — show actual face % to student

    public StudentCheckInResponse(boolean success, String message,
                                   Double aciScore, String aciLevel,
                                   Double gpsDistanceMeters) {
        this.success = success;
        this.message = message;
        this.aciScore = aciScore;
        this.aciLevel = aciLevel;
        this.gpsDistanceMeters = gpsDistanceMeters;
    }

    // Convenience method — failure with reason code
    public static StudentCheckInResponse fail(String message, String failReason,
                                               Double gpsDistance, Double faceConf) {
        StudentCheckInResponse r = new StudentCheckInResponse(
                false, message, null, null, gpsDistance);
        r.failReason = failReason;
        r.faceConfidence = faceConf;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getFailReason() { return failReason; }
    public Double getAciScore() { return aciScore; }
    public String getAciLevel() { return aciLevel; }
    public Double getGpsDistanceMeters() { return gpsDistanceMeters; }
    public Double getFaceConfidence() { return faceConfidence; }

    public void setFailReason(String r) { this.failReason = r; }
    public void setFaceConfidence(Double f) { this.faceConfidence = f; }
}

