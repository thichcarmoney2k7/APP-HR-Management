package Controller;

import dto.response.AttendanceResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.AttendanceClientService;
import util.SessionManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

public class UserAttendanceController {

    // --- Trạng thái ca làm việc hôm nay ---
    @FXML private Label lblEmployeeName;
    @FXML private Label lblTodayDate;
    @FXML private Label lblCheckInTime;
    @FXML private Label lblCheckOutTime;
    @FXML private Label lblTodayStatus;
    @FXML private Label lblMessage;

    // --- Bảng lịch sử chấm công & bộ lọc tháng ---
    @FXML private TextField txtMonthFilter; // Định dạng yyyy-MM (VD: 2026-11)
    @FXML private TableView<AttendanceResponse> tblAttendance;
    @FXML private TableColumn<AttendanceResponse, LocalDate> colWorkDate;
    @FXML private TableColumn<AttendanceResponse, LocalTime> colCheckIn;
    @FXML private TableColumn<AttendanceResponse, LocalTime> colCheckOut;
    @FXML private TableColumn<AttendanceResponse, Integer> colLateMinutes;
    @FXML private TableColumn<AttendanceResponse, Double> colWorkHours;
    @FXML private TableColumn<AttendanceResponse, String> colStatus;

    private final AttendanceClientService attendanceService = new AttendanceClientService();

    @FXML
    public void initialize() {
        setupTableColumns();

        if (lblEmployeeName != null) {
            String name = SessionManager.getFullName() != null && !SessionManager.getFullName().isBlank()
                    ? SessionManager.getFullName()
                    : SessionManager.getUsername();
            lblEmployeeName.setText("Nhân viên: " + (name != null ? name : ""));
        }
        if (lblTodayDate != null) {
            lblTodayDate.setText("Ngày: " + LocalDate.now());
        }
        if (txtMonthFilter != null) {
            txtMonthFilter.setText(YearMonth.now().toString());
        }

        if (SessionManager.getEmployeeId() == null) {
            showMessage("Tài khoản của bạn chưa được liên kết hồ sơ nhân viên (employeeId).", true);
            return;
        }

        loadTodayStatus();
        loadFullHistory();
    }

    private void setupTableColumns() {
        if (colWorkDate != null) colWorkDate.setCellValueFactory(new PropertyValueFactory<>("workDate"));
        if (colCheckIn != null) colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        if (colCheckOut != null) colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
        if (colLateMinutes != null) colLateMinutes.setCellValueFactory(new PropertyValueFactory<>("lateMinutes"));
        if (colWorkHours != null) colWorkHours.setCellValueFactory(new PropertyValueFactory<>("workHours"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    public void handleFilterByMonth() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) return;

        String monthYear = (txtMonthFilter != null && !txtMonthFilter.getText().isBlank())
                ? txtMonthFilter.getText().trim()
                : YearMonth.now().toString();

        new Thread(() -> {
            try {
                List<AttendanceResponse> list = attendanceService.getMonthlyAttendance(employeeId, monthYear);
                Platform.runLater(() -> {
                    if (tblAttendance != null) {
                        tblAttendance.setItems(FXCollections.observableArrayList(list));
                    }
                    showMessage("Đã tải bảng công tháng " + monthYear + " (" + list.size() + " ngày).", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage(e.getMessage(), true));
            }
        }).start();
    }

    @FXML
    public void loadFullHistory() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) return;

        new Thread(() -> {
            try {
                List<AttendanceResponse> list = attendanceService.getEmployeeAttendanceHistory(employeeId);
                Platform.runLater(() -> {
                    if (tblAttendance != null) {
                        tblAttendance.setItems(FXCollections.observableArrayList(list));
                    }
                    showMessage("Đã tải toàn bộ lịch sử chấm công (" + list.size() + " bản ghi).", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("Lỗi tải lịch sử chấm công: " + e.getMessage(), true));
            }
        }).start();
    }

    private void loadTodayStatus() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) return;

        new Thread(() -> {
            try {
                AttendanceResponse today = attendanceService.getTodayAttendance(employeeId);
                Platform.runLater(() -> {
                    if (today != null && today.getAttendanceId() != null) {
                        updateTodayLabels(today);
                    } else {
                        if (lblCheckInTime != null) lblCheckInTime.setText("Giờ vào: Chưa check-in");
                        if (lblCheckOutTime != null) lblCheckOutTime.setText("Giờ ra: Chưa check-out");
                        if (lblTodayStatus != null) lblTodayStatus.setText("Trạng thái: Chưa điểm danh");
                    }
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void updateTodayLabels(AttendanceResponse att) {
        if (lblCheckInTime != null) {
            lblCheckInTime.setText("Giờ vào: " + (att.getCheckInTime() != null ? att.getCheckInTime() : "Chưa có"));
        }
        if (lblCheckOutTime != null) {
            lblCheckOutTime.setText("Giờ ra: " + (att.getCheckOutTime() != null ? att.getCheckOutTime() : "Chưa có"));
        }
        if (lblTodayStatus != null && att.getStatus() != null) {
            lblTodayStatus.setText("Trạng thái: " + att.getStatus()
                    + (att.getLateMinutes() > 0 ? " (Muộn " + att.getLateMinutes() + " phút)" : ""));
        }
    }

    private void showMessage(String msg, boolean isError) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        }
    }
}
