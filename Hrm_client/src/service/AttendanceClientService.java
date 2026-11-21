package service;

import com.google.gson.reflect.TypeToken;
import dto.request.FaceCheckinRequest;
import dto.response.AttendanceResponse;
import util.ApiClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AttendanceClientService {

    // 1. Check-in vào ca bằng khuôn mặt -> POST /api/attendance/checkin
    public AttendanceResponse checkIn(Long employeeId, String faceData) throws Exception {
        FaceCheckinRequest request = new FaceCheckinRequest();
        request.setEmployeeId(employeeId);
        request.setFaceData(faceData);
        return ApiClient.post("/attendance/checkin", request, AttendanceResponse.class);
    }

    // 2. Check-out ra về -> POST /api/attendance/checkout?employeeId=...
    public AttendanceResponse checkOut(Long employeeId) throws Exception {
        return ApiClient.post("/attendance/checkout?employeeId=" + employeeId, null, AttendanceResponse.class);
    }

    // 3. Kiểm tra hôm nay nhân viên đã check-in chưa -> GET /api/attendance/employee/{id}/has-checked-in
    public boolean hasCheckedInToday(Long employeeId) throws Exception {
        Map<?, ?> response = ApiClient.get("/attendance/employee/" + employeeId + "/has-checked-in", Map.class);
        return response != null && Boolean.TRUE.equals(response.get("hasCheckedIn"));
    }

    // 4. Lấy chi tiết chấm công hôm nay -> GET /api/attendance/employee/{id}/today
    public AttendanceResponse getTodayAttendance(Long employeeId) throws Exception {
        return ApiClient.get("/attendance/employee/" + employeeId + "/today", AttendanceResponse.class);
    }

    // 5. Lấy toàn bộ lịch sử chấm công của nhân viên -> GET /api/attendance/employee/{id}
    public List<AttendanceResponse> getEmployeeAttendanceHistory(Long employeeId) throws Exception {
        return ApiClient.getList(
                "/attendance/employee/" + employeeId,
                new TypeToken<List<AttendanceResponse>>() {}.getType()
        );
    }

    // 6. Lấy bảng công theo tháng (monthYear định dạng "yyyy-MM", VD: "2026-11")
    public List<AttendanceResponse> getMonthlyAttendance(Long employeeId, String monthYear) throws Exception {
        String endpoint = "/attendance/employee/" + employeeId + "/monthly";
        if (monthYear != null && !monthYear.isBlank()) {
            endpoint += "?monthYear=" + monthYear.trim();
        }
        return ApiClient.getList(endpoint, new TypeToken<List<AttendanceResponse>>() {}.getType());
    }

    // 7. Lấy danh sách chấm công toàn bộ nhân viên theo ngày (Dành cho Manager/Admin)
    public List<AttendanceResponse> getAllAttendanceByDate(LocalDate date) throws Exception {
        String endpoint = "/attendance";
        if (date != null) {
            endpoint += "?date=" + date; // ISO_LOCAL_DATE: yyyy-MM-dd
        }
        return ApiClient.getList(endpoint, new TypeToken<List<AttendanceResponse>>() {}.getType());
    }
}
