package service;

import com.google.gson.reflect.TypeToken;
import dto.request.TaskAssignRequest;
import dto.response.TaskResponse;
import util.ApiClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TaskClientService {

    // 1. Manager giao task mới cho nhân viên -> POST /api/tasks/assign
    public TaskResponse assignTask(TaskAssignRequest request) throws Exception {
        return ApiClient.post("/tasks/assign", request, TaskResponse.class);
    }

    // 2. Lấy toàn bộ danh sách task trong công ty -> GET /api/tasks
    public List<TaskResponse> getAllTasks() throws Exception {
        return ApiClient.getList("/tasks", new TypeToken<List<TaskResponse>>() {}.getType());
    }

    // 3. Lấy danh sách task của 1 nhân viên (lọc tùy chọn theo completed hoặc monthYear) -> GET /api/tasks/employee/{id}
    public List<TaskResponse> getTasksByEmployee(Long employeeId, Boolean completed, String monthYear) throws Exception {
        List<String> params = new ArrayList<>();
        if (completed != null) {
            params.add("completed=" + completed);
        } else if (monthYear != null && !monthYear.isBlank()) {
            String encodedMonth = URLEncoder.encode(monthYear.trim(), StandardCharsets.UTF_8);
            params.add("monthYear=" + encodedMonth);
        }

        String query = params.isEmpty() ? "" : "?" + String.join("&", params);
        return ApiClient.getList(
                "/tasks/employee/" + employeeId + query,
                new TypeToken<List<TaskResponse>>() {}.getType()
        );
    }

    // 4. Cập nhật trạng thái hoàn thành công việc -> PUT /api/tasks/{id}/status?completed=...
    public TaskResponse updateTaskStatus(Long taskId, boolean completed) throws Exception {
        return ApiClient.put(
                "/tasks/" + taskId + "/status?completed=" + completed,
                null,
                TaskResponse.class
        );
    }

    // 5. Xóa công việc -> DELETE /api/tasks/{id}
    public void deleteTask(Long taskId) throws Exception {
        ApiClient.delete("/tasks/" + taskId);
    }
}