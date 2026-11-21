package Controller;

import dto.response.TaskResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.TaskClientService;
import util.SessionManager;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class UserTaskController {

    // --- Bộ lọc công việc cá nhân ---
    @FXML private ComboBox<String> cboStatusFilter; // ALL, COMPLETED, INCOMPLETE
    @FXML private TextField txtMonthFilter;         // Định dạng yyyy-MM (VD: 2026-11)

    // --- Bảng công việc được giao ---
    @FXML private TableView<TaskResponse> tblMyTasks;
    @FXML private TableColumn<TaskResponse, Long> colId;
    @FXML private TableColumn<TaskResponse, String> colTitle;
    @FXML private TableColumn<TaskResponse, String> colDescription;
    @FXML private TableColumn<TaskResponse, LocalDate> colAssignedDate;
    @FXML private TableColumn<TaskResponse, LocalDate> colDeadline;
    @FXML private TableColumn<TaskResponse, Boolean> colCompleted;

    @FXML private TextArea txtTaskDetail;
    @FXML private Label lblStatusMessage;

    private final TaskClientService taskService = new TaskClientService();

    @FXML
    public void initialize() {
        setupTableColumns();

        if (cboStatusFilter != null) {
            cboStatusFilter.setItems(FXCollections.observableArrayList("ALL", "COMPLETED", "INCOMPLETE"));
            cboStatusFilter.setValue("ALL");
        }
        if (txtMonthFilter != null) {
            txtMonthFilter.setText(YearMonth.now().toString());
        }

        if (tblMyTasks != null) {
            tblMyTasks.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null && txtTaskDetail != null) {
                    txtTaskDetail.setText(
                            "Tiêu đề: " + newSel.getTitle() + "\n" +
                                    "Hạn chót: " + newSel.getDeadline() + "\n" +
                                    "Mô tả chi tiết:\n" + (newSel.getDescription() != null ? newSel.getDescription() : "Không có mô tả")
                    );
                }
            });
        }

        loadMyTasks();
    }

    private void setupTableColumns() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colTitle != null) colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colDescription != null) colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (colAssignedDate != null) colAssignedDate.setCellValueFactory(new PropertyValueFactory<>("assignedDate"));
        if (colDeadline != null) colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        if (colCompleted != null) colCompleted.setCellValueFactory(new PropertyValueFactory<>("completed"));
    }

    // 1. Tải công việc của nhân viên (theo trạng thái đang chọn ở ComboBox)
    @FXML
    public void loadMyTasks() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) {
            showMessage("Không tìm thấy mã nhân viên trong phiên đăng nhập!", true);
            return;
        }

        String filter = (cboStatusFilter != null && cboStatusFilter.getValue() != null)
                ? cboStatusFilter.getValue()
                : "ALL";

        Boolean completedParam = null;
        if ("COMPLETED".equalsIgnoreCase(filter)) completedParam = true;
        else if ("INCOMPLETE".equalsIgnoreCase(filter)) completedParam = false;

        final Boolean finalCompleted = completedParam;
        new Thread(() -> {
            try {
                List<TaskResponse> tasks = taskService.getTasksByEmployee(employeeId, finalCompleted, null);
                Platform.runLater(() -> {
                    if (tblMyTasks != null) {
                        tblMyTasks.setItems(FXCollections.observableArrayList(tasks));
                    }
                    showMessage("Bạn có " + tasks.size() + " công việc.", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage(e.getMessage(), true));
            }
        }).start();
    }

    // 2. Lọc công việc theo tháng (yyyy-MM)
    @FXML
    public void handleFilterByMonth() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) return;

        String monthYear = (txtMonthFilter != null) ? txtMonthFilter.getText().trim() : YearMonth.now().toString();
        new Thread(() -> {
            try {
                List<TaskResponse> tasks = taskService.getTasksByEmployee(employeeId, null, monthYear);
                Platform.runLater(() -> {
                    if (tblMyTasks != null) {
                        tblMyTasks.setItems(FXCollections.observableArrayList(tasks));
                    }
                    showMessage("Đã tải " + tasks.size() + " công việc trong tháng " + monthYear, false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage(e.getMessage(), true));
            }
        }).start();
    }

    // 3. Nhân viên báo cáo đã hoàn thành công việc đang chọn
    @FXML
    public void handleCompleteSelectedTask() {
        TaskResponse selected = (tblMyTasks != null) ? tblMyTasks.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showMessage("Vui lòng chọn 1 công việc trong bảng trước!", true);
            return;
        }

        new Thread(() -> {
            try {
                TaskResponse updated = taskService.updateTaskStatus(selected.getId(), true);
                Platform.runLater(() -> {
                    showMessage(updated.getMessage(), false);
                    loadMyTasks();
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