package service;

import com.google.gson.reflect.TypeToken;
import dto.response.EmployeeResponse;
import util.ApiClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmployeeClientService {

    // 1. Tuyển dụng ứng viên -> Tạo hồ sơ Employee & nâng quyền User -> POST /api/employees/hire/{candidateId}
    public EmployeeResponse hireCandidateToEmployee(Long candidateId, Double baseSalary,
                                                    Integer departmentId, Long managerId) throws Exception {
        List<String> params = new ArrayList<>();
        if (baseSalary != null) params.add("baseSalary=" + baseSalary);
        if (departmentId != null) params.add("departmentId=" + departmentId);
        if (managerId != null) params.add("managerId=" + managerId);

        String query = params.isEmpty() ? "" : "?" + String.join("&", params);
        return ApiClient.post("/employees/hire/" + candidateId + query, null, EmployeeResponse.class);
    }

    // 2. Lấy danh sách toàn bộ nhân viên -> GET /api/employees
    public List<EmployeeResponse> getAllEmployees() throws Exception {
        return ApiClient.getList("/employees", new TypeToken<List<EmployeeResponse>>() {}.getType());
    }

    // 3. Lấy chi tiết nhân viên theo Employee ID -> GET /api/employees/{id}
    public EmployeeResponse getEmployeeById(Long id) throws Exception {
        return ApiClient.get("/employees/" + id, EmployeeResponse.class);
    }

    // 4. Tìm nhân viên theo User ID -> GET /api/employees/user/{userId}
    public EmployeeResponse getEmployeeByUserId(Long userId) throws Exception {
        return ApiClient.get("/employees/user/" + userId, EmployeeResponse.class);
    }

    // 5. Tìm nhân viên theo Face ID -> GET /api/employees/face?faceId=...
    public EmployeeResponse getEmployeeByFaceId(String faceId) throws Exception {
        String encoded = URLEncoder.encode(faceId, StandardCharsets.UTF_8);
        return ApiClient.get("/employees/face?faceId=" + encoded, EmployeeResponse.class);
    }

    // 6. Tìm nhân viên theo Candidate ID gốc -> GET /api/employees/candidate/{candidateId}
    public EmployeeResponse getEmployeeByCandidateId(Long candidateId) throws Exception {
        return ApiClient.get("/employees/candidate/" + candidateId, EmployeeResponse.class);
    }

    // 7. Lấy danh sách nhân viên thuộc quyền quản lý của 1 Manager -> GET /api/employees/manager/{managerId}
    public List<EmployeeResponse> getEmployeesByManagerId(Long managerId) throws Exception {
        return ApiClient.getList(
                "/employees/manager/" + managerId,
                new TypeToken<List<EmployeeResponse>>() {}.getType()
        );
    }

    // 8. Tìm nhân viên theo Email -> GET /api/employees/email?email=...
    public EmployeeResponse getEmployeeByEmail(String email) throws Exception {
        String encoded = URLEncoder.encode(email.trim(), StandardCharsets.UTF_8);
        return ApiClient.get("/employees/email?email=" + encoded, EmployeeResponse.class);
    }

    // 9. Đăng ký khuôn mặt mẫu từ ảnh Base64 Webcam JavaFX -> POST /api/employees/{id}/register-face
    public EmployeeResponse registerFaceId(Long employeeId, String imageBase64) throws Exception {
        Map<String, String> payload = Map.of("imageBase64", imageBase64);
        return ApiClient.post("/employees/" + employeeId + "/register-face", payload, EmployeeResponse.class);
    }

    // 10. Cập nhật thông tin hồ sơ nhân viên -> PUT /api/employees/{id}
    public EmployeeResponse updateEmployee(Long id, EmployeeResponse updatedInfo) throws Exception {
        return ApiClient.put("/employees/" + id, updatedInfo, EmployeeResponse.class);
    }

    // 11. Xóa nhân viên -> DELETE /api/employees/{id}
    public void deleteEmployee(Long id) throws Exception {
        ApiClient.delete("/employees/" + id);
    }
}

