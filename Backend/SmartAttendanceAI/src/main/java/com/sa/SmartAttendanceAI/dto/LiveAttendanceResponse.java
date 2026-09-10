package com.sa.SmartAttendanceAI.dto;

import java.util.List;

public class LiveAttendanceResponse {

    private List<AttendanceDTO> presentList;
    private long totalEnrolled;

    public LiveAttendanceResponse(List<AttendanceDTO> presentList, long totalEnrolled) {
        this.presentList = presentList;
        this.totalEnrolled = totalEnrolled;
    }

    public List<AttendanceDTO> getPresentList() {
        return presentList;
    }

    public long getTotalEnrolled() {
        return totalEnrolled;
    }
}