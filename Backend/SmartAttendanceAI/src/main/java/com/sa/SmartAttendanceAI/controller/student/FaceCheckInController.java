package com.sa.SmartAttendanceAI.controller.student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sa.SmartAttendanceAI.dto.StudentCheckInRequest;
import com.sa.SmartAttendanceAI.dto.StudentCheckInResponse;
import com.sa.SmartAttendanceAI.entity.Student;
import com.sa.SmartAttendanceAI.repository.StudentRepository;
import com.sa.SmartAttendanceAI.service.attendance.AttendanceService;
import com.sa.SmartAttendanceAI.service.face.FaceRecognitionService;

/**
 * Face Check-in Controller
 * Student marks attendance using:
 * 1. Face photo (compared with profile)
 * 2. GPS location (geo-fence check)
 * 3. ACI score calculated
 */
@RestController
@RequestMapping("/api/student/attendance")
@CrossOrigin
public class FaceCheckInController {

    @Autowired private AttendanceService attendanceService;
    @Autowired private FaceRecognitionService faceService;
    @Autowired private StudentRepository studentRepo;

    /**
     * POST /api/student/attendance/face-checkin
     * Multipart: faceImage + GPS coordinates + timetableId
     */
    @PostMapping(value = "/face-checkin", consumes = "multipart/form-data")
    public StudentCheckInResponse faceCheckIn(
            @RequestParam Long timetableId,
            @RequestParam Double studentLatitude,
            @RequestParam Double studentLongitude,
            @RequestPart("faceImage") MultipartFile faceImage) {

        try {
            // Get logged-in student
            String email = SecurityContextHolder.getContext()
                    .getAuthentication().getName();

            Student student = studentRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // Compare face with stored profile photo
            double faceConfidence = 0.0;
            if (student.getProfilePic() == null || student.getProfilePic().length == 0) {
                return StudentCheckInResponse.fail(
                    "Profile photo not found. Please upload your photo in Settings.",
                    "NO_PROFILE_PHOTO", 0.0, 0.0);
            }

            faceConfidence = faceService.compareFace(student.getProfilePic(), faceImage);

            // Face mismatch — return confidence % so student knows how close
            if (faceConfidence < 70.0) {
                return StudentCheckInResponse.fail(
                    "Face mismatch. Your face matched " + faceConfidence
                    + "% with your profile photo. Minimum required: 70%. "
                    + "Try better lighting or retake your profile photo.",
                    "FACE_MISMATCH", 0.0, faceConfidence);
            }

            // Build check-in request with face confidence
            StudentCheckInRequest req = new StudentCheckInRequest();
            req.setTimetableId(timetableId);
            req.setStudentId(student.getId());
            req.setStudentLatitude(studentLatitude);
            req.setStudentLongitude(studentLongitude);
            req.setFaceConfidence(faceConfidence);

            return attendanceService.studentCheckIn(req);

        } catch (Exception e) {
            return new StudentCheckInResponse(false,
                    "Error: " + e.getMessage(), null, null, 0.0);
        }
    }
}
