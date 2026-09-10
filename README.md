# SmartAttendanceAI

> AI-powered attendance system with Face Recognition, GPS Geo-fencing, and Attendance Confidence Index (ACI)

## What Makes This System Unique

### Attendance Confidence Index (ACI)
Every attendance record stores a trust score from 0–100:
- **GPS Score** (40%) — how close student is to faculty location
- **Face Score** (45%) — face recognition confidence
- **Time Score** (15%) — how early in the session student checked in

| ACI Level | Score | Meaning |
|-----------|-------|---------|
| HIGH | ≥ 80 | Fully trusted |
| MEDIUM | ≥ 55 | Acceptable |
| LOW | ≥ 35 | Suspicious — review recommended |
| SUSPICIOUS | < 35 | Flagged for investigation |

No commercial attendance system has this concept.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.5.10, Java 17 |
| Database | PostgreSQL |
| Security | JWT + Spring Security |
| Face Recognition | Pure Java (pHash + RGB Histogram + Structural Pixel) + OpenCV optional |
| GPS | Haversine formula |
| Email | Spring Boot Mail (SMTP) |
| API Docs | Swagger / OpenAPI (springdoc 2.5) |
| Excel Export | Apache POI 5.2.3 |

## How to Run

### Prerequisites
- Java 17+
- PostgreSQL running on port 5432
- Database: `smart_attendance_ai`

### Setup
1. Clone the project
2. Open `src/main/resources/application.properties`
3. Set your database password: `spring.datasource.password=YOUR_PASSWORD`
4. Set Gmail App Password: `spring.mail.password=YOUR_16_CHAR_APP_PASSWORD`
5. Set your base URL: `app.base-url=http://localhost:5500`
6. Run: `mvn spring-boot:run`

### API Documentation
After running, open: `http://localhost:8080/swagger-ui.html`

## System Architecture

```
Admin Dashboard
    │
    ├── Invite Student/Faculty (email token, 24h expiry)
    ├── Create Timetable (with conflict detection)
    └── Manage Batches

Faculty Dashboard
    │
    ├── Start Session (sets GPS location + radius)
    ├── View Live Attendance (with ACI scores)
    ├── Manual Override
    └── Download Excel Report (with ACI columns)

Student Dashboard
    │
    ├── See Today's Classes (LIVE/UPCOMING/DONE status)
    ├── Face Check-in (10-minute window)
    │       ├── GPS geo-fence check (Haversine)
    │       ├── Face recognition (3-algorithm)
    │       └── ACI score calculated & stored
    └── View Attendance History (subject-wise with ACI)
```

## Key API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login (Admin/Faculty/Student) |
| POST | `/api/admin/invite-student` | Invite student by email |
| POST | `/api/admin/invite-faculty` | Invite faculty by email |
| POST | `/api/admin/timetables` | Create timetable (with conflict check) |
| POST | `/api/faculty/attendance/start` | Start attendance session with GPS |
| POST | `/api/student/attendance/check-in` | Face + GPS check-in |
| GET | `/api/faculty/reports/batch/{id}` | Batch attendance report |
| GET | `/api/faculty/reports/batch/{id}/export` | Download Excel with ACI columns |
| GET | `/api/student/dashboard` | Student dashboard with active sessions |

## ACI Formula
```
ACI = (GPS_Score × 0.40) + (Face_Score × 0.45) + (Time_Score × 0.15)

GPS_Score  = 100 - (distance_metres / radius_metres × 100)
Face_Score = face_recognition_confidence (0-100)
Time_Score = 100 - (seconds_since_start / session_duration × 100)
```
