package Controller;

import dto.request.TaskAssignRequest;
import dto.response.EmployeeResponse;
import dto.response.TaskResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.EmployeeClientService;
import service.TaskClientService;

import java.time.LocalDate;
import java.util.List;

public class AdminTaskController {

    // --- Form giao việc mới ---
    @FXML private ComboBox<EmployeeResponse> cboEmployee;
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dpDeadline;
    @FXML private Button btnAssignTask;

    // --- Bảng quản lý toàn bộ công việc ---
    @FXML private TableView<TaskResponse> tblTasks;
    @FXML private TableColumn<TaskResponse, Long> colId;
    @FXML private TableColumn<TaskResponse, String> colEmployeeName;
    @FXML private TableColumn<TaskResponse, String> colTitle;
    @FXML private TableColumn<TaskResponse, String> colDescription;
    @FXML private TableColumn<TaskResponse, LocalDate> colAssignedDate;
    @FXML private TableColumn<TaskResponse, LocalDate> colDeadline;
    @FXML private TableColumn<TaskResponse, Boolean> colCompleted;

    @FXML private Label lblStatusMessage;

    private final TaskClientService taskService = new TaskClientService();
    private final EmployeeClientService employeeService = new EmployeeClientService();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadEmployeesToComboBox();
        loadAllTasks();
    }

    private void setupTableColumns() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colEmployeeName != null) colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        if (colTitle != null) colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colDescription != null) colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (colAssignedDate != null) colAssignedDate.setCellValueFactory(new PropertyValueFactory<>("assignedDate"));
        if (colDeadline != null) colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        if (colCompleted != null) colCompleted.setCellValueFactory(new PropertyValueFactory<>("completed"));
    }

    private void loadEmployeesToComboBox() {
        if (cboEmployee == null) return;
        new Thread(() -> {
            try {
                List<EmployeeResponse> employees = employeeService.getAllEmployees();
                Platform.runLater(() -> {
                    cboEmployee.setItems(FXCollections.observableArrayList(employees));
                    if (!employees.isEmpty()) {
                        cboEmployee.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    // 1. Giao task mới cho nhân viên
    @FXML
    public void handleAssignTask() {
        EmployeeResponse selectedEmp = (cboEmployee != null) ? cboEmployee.getValue() : null;
        String title = (txtTitle != null) ? txtTitle.getText().trim() : "";
        String desc = (txtDescription != null) ? txtDescription.getText().trim() : "";
        LocalDate deadline = (dpDeadline != null) ? dpDeadline.getValue() : null;

        if (selectedEmp == null) {
            showMessage("Vui lòng chọn nhân viên nhận việc!", true);
            return;
        }
        if (title.isEmpty()) {
            showMessage("Vui lòng nhập tiêu đề công việc!", true);
            return;
        }
        if (deadline == null) {
            showMessage("Vui lòng chọn hạn hoàn thành (Deadline)!", true);
            return;
        }

        TaskAssignRequest request = new TaskAssignRequest();
        request.setEmployeeId(selectedEmp.getId());
        request.setTitle(title);
        request.setDescription(desc);
        request.setDeadline(deadline);

        if (btnAssignTask != null) btnAssignTask.setDisable(true);

        new Thread(() -> {
            try {
                TaskResponse res = taskService.assignTask(request);
                Platform.runLater(() -> {
                    if (btnAssignTask != null) btnAssignTask.setDisable(false);
                    if (res != null && res.getId() != null) {
                        showMessage(res.getMessage(), false);
                        if (txtTitle != null) txtTitle.clear();
                        if (txtDescription != null) txtDescription.clear();
                        if (dpDeadline != null) dpDeadline.setValue(null);
                        loadAllTasks();
                    } else {
                        String err = (res != null && res.getMessage() != null) ? res.getMessage() : "Giao việc thất bại!";
                        showMessage(err, true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (btnAssignTask != null) btnAssignTask.setDisable(false);
                    showMessage(e.getMessage(), true);
                });
            }
        }).start();
    }

    // 2. Tải toàn bộ danh sách task
    @FXML
    public void loadAllTasks() {
        new Thread(() -> {
            try {
                List<TaskResponse> tasks = taskService.getAllTasks();
                Platform.runLater(() -> {
                    if (tblTasks != null) {
                        tblTasks.setItems(FXCollections.observableArrayList(tasks));
                    }
                    showMessage("Đã tải " + tasks.size() + " công việc.", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("Lỗi tải danh sách công việc: " + e.getMessage(), true));
            }
        }).start();
    }

    // 3. Manager xác nhận ĐÃ HOÀN THÀNH task đang chọn
    @FXML
    public void handleMarkCompleted() {
        changeSelectedTaskStatus(true);
    }

    // 4. Manager đánh dấu CHƯA HOÀN THÀNH (yêu cầu làm lại)
    @FXML
    public void handleMarkIncomplete() {
        changeSelectedTaskStatus(false);
    }

    private void changeSelectedTaskStatus(boolean completed) {
        TaskResponse selected = (tblTasks != null) ? tblTasks.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showMessage("Vui lòng chọn 1 công việc trong bảng!", true);
            return;
        }

        new Thread(() -> {
            try {
                TaskResponse updated = taskService.updateTaskStatus(selected.getId(), completed);
                Platform.runLater(() -> {
                    showMessage(updated.getMessage(), false);
                    loadAllTasks();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage(e.getMessage(), true));
            }
        }).start();
    }

    // 5. Xóa công việc
    @FXML
    public void handleDeleteTask() {
        TaskResponse selected = (tblTasks != null) ? tblTasks.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showMessage("Vui lòng chọn 1 công việc để xóa!", true);
            return;
        }

        new Thread(() -> {
            try {
                taskService.deleteTask(selected.getId());
                Platform.runLater(() -> {
                    showMessage("Đã xóa công việc: " + selected.getTitle(), false);
                    loadAllTasks();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage(e.getMessage(), true));
            }
        }).start();
    }

    private void showMessage(String msg, boolean isError) {
        if (lblStatusMessage != null) {
            lblStatusMessage.setText(msg);
            lblStatusMessage.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        }
    }
}