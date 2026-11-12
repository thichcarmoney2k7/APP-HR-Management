package service;

import dto.request.TaskAssignRequest;
import dto.response.TaskResponse;
import entity.Employee;
import entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.EmployeeRepository;
import repository.TaskRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, EmployeeRepository employeeRepository) {
        this.taskRepository = taskRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * 1. Manager giao task cho nhân viên
     */
    public TaskResponse assignTask(TaskAssignRequest request) {
        Optional<Employee> empOpt = employeeRepository.findById(request.getEmployeeId());
        if (empOpt.isEmpty()) {
            TaskResponse response = new TaskResponse();
            response.setMessage("Không tìm thấy nhân viên có ID: " + request.getEmployeeId());
            return response;
        }

        Employee employee = empOpt.get();

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setEmployeeId(employee.getId());
        task.setCompleted(false);
        task.setAssignedDate(LocalDate.now());
        task.setdeadline(request.getDeadline());

        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask, employee.getFullName(), "Giao công việc thành công!");
    }

    /**
     * 2. Lấy danh sách toàn bộ task trong công ty (Dùng cho màn hình quản lý của Manager/Admin)
     */
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(task -> mapToResponse(task, getEmployeeName(task.getEmployeeId()), "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * 3. Lấy danh sách tất cả task của 1 nhân viên (Repo method 1)
     */
    public List<TaskResponse> getTasksByEmployee(Long employeeId) {
        String empName = getEmployeeName(employeeId);

        return taskRepository.findByEmployeeId(employeeId)
                .stream()
                .map(task -> mapToResponse(task, empName, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * 4. Lọc danh sách task theo trạng thái hoàn thành: true/false (Repo method 2)
     */
    public List<TaskResponse> getTasksByEmployeeAndStatus(Long employeeId, boolean completed) {
        String empName = getEmployeeName(employeeId);

        return taskRepository.findByEmployeeIdAndCompleted(employeeId, completed)
                .stream()
                .map(task -> mapToResponse(task, empName, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * 5. Lọc danh sách task của 1 nhân viên theo tháng, định dạng monthYear: "2026-11" (Repo method 3)
     */
    public List<TaskResponse> getTasksByEmployeeAndMonth(Long employeeId, String monthYear) {
        YearMonth ym = YearMonth.parse(monthYear);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        String empName = getEmployeeName(employeeId);

        return taskRepository.findByEmployeeIdAndAssignedDateBetween(employeeId, startDate, endDate)
                .stream()
                .map(task -> mapToResponse(task, empName, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * 6. Cập nhật trạng thái hoàn thành công việc
     */
    public TaskResponse updateTaskStatus(Long taskId, boolean isCompleted) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            TaskResponse response = new TaskResponse();
            response.setMessage("Không tìm thấy công việc!");
            return response;
        }

        Task task = taskOpt.get();
        task.setCompleted(isCompleted);
        Task updatedTask = taskRepository.save(task);

        String empName = getEmployeeName(task.getEmployeeId());

        return mapToResponse(updatedTask, empName, "Cập nhật trạng thái task thành công!");
    }

    /**
     * 7. Xóa công việc (Trường hợp Manager giao nhầm)
     */
    public boolean deleteTask(Long taskId) {
        if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }

    // --- Helper Methods ---
    private String getEmployeeName(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .map(Employee::getFullName)
                .orElse("N/A");
    }

    private TaskResponse mapToResponse(Task task, String employeeName, String message) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setEmployeeId(task.getEmployeeId());
        response.setEmployeeName(employeeName);
        response.setCompleted(task.getCompleted());
        response.setAssignedDate(task.getAssignedDate());
        response.setDeadline(task.getDeadline());
        response.setMessage(message);
        return response;
    }
}