package Controller;

import dto.response.AttendanceResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import service.AttendanceClientService;
import service.EmployeeClientService;
import util.SessionManager;
import util.WebcamUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

public class UserAttendanceController {

    // --- Khu vực Camera quét mặt ---
    @FXML private ImageView imgWebcam;
    @FXML private Button btnRegisterFace;
    @FXML private Button btnCheckIn;
    @FXML private Button btnCheckOut;

    // --- Khu vực hiển thị trạng thái ca làm việc hôm nay ---
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
    private final EmployeeClientService employeeService = new EmployeeClientService();

    @FXML
    public void initialize() {
        setupTableColumns();

        if (lblEmployeeName != null && SessionManager.getFullName() != null) {
            lblEmployeeName.setText("Nhân viên: " + SessionManager.getFullName());
        }
        if (lblTodayDate != null) {
            lblTodayDate.setText("Ngày: " + LocalDate.now());
        }
        if (txtMonthFilter != null) {
            txtMonthFilter.setText(YearMonth.now().toString());
        }

        // Mở camera trên luồng phụ để không làm khựng giao diện khi mới chuyển tab
        handleStartCamera();

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

    // Bật camera (có thể gắn thêm vào nút "Bật Camera" nếu FXML có)
    @FXML
    public void handleStartCamera() {
        if (imgWebcam == null) return;
        new Thread(() -> {
            try {
                WebcamUtils.startCamera(imgWebcam);
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("Không mở được Webcam: " + e.getMessage(), true));
            }
        }).start();
    }

    // Tắt camera (có thể gắn thêm vào nút "Tắt Camera" nếu FXML có)
    @FXML
    public void handleStopCamera() {
        WebcamUtils.stopCamera();
        if (imgWebcam != null) {
            imgWebcam.setImage(null);
        }
    }

    // 1. Nút Check-in bằng quét khuôn mặt
    @FXML
    public void handleCheckIn() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) {
            showMessage("Không tìm thấy mã nhân viên (employeeId) trong phiên đăng nhập!", true);
            return;
        }

        String faceData = captureFaceDataFromWebcam();
        if (faceData == null || faceData.isBlank()) {
            return; // Thông báo lỗi đã hiển thị trong captureFaceDataFromWebcam()
        }

        setButtonsDisabled(true);
        showMessage("Đang nhận diện khuôn mặt và check-in...", false);

        new Thread(() -> {
            try {
                AttendanceResponse res = attendanceService.checkIn(employeeId, faceData);
                Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    if (res != null && res.getAttendanceId() != null) {
                        showMessage(res.getMessage(), false);
                        updateTodayLabels(res);
                        loadFullHistory();
                    } else {
                        String err = (res != null && res.getMessage() != null) ? res.getMessage() : "Check-in thất bại!";
                        showMessage(err, true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    showMessage(e.getMessage(), true);
                });
            }
        }).start();
    }

    // 2. Nút Check-out kết thúc ca làm việc
    @FXML
    public void handleCheckOut() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) {
            showMessage("Không tìm thấy mã nhân viên trong phiên đăng nhập!", true);
            return;
        }

        setButtonsDisabled(true);
        showMessage("Đang xử lý check-out...", false);

        new Thread(() -> {
            try {
                AttendanceResponse res = attendanceService.checkOut(employeeId);
                Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    if (res != null && res.getCheckOutTime() != null) {
                        showMessage(res.getMessage() + " (Tổng giờ làm: " + res.getWorkHours() + "h)", false);
                        updateTodayLabels(res);
                        loadFullHistory();
                    } else {
                        String err = (res != null && res.getMessage() != null) ? res.getMessage() : "Check-out thất bại!";
                        showMessage(err, true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    showMessage(e.getMessage(), true);
                });
            }
        }).start();
    }

    // 3. Nút Đăng ký khuôn mặt lần đầu
    @FXML
    public void handleRegisterFace() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) {
            showMessage("Không tìm thấy mã nhân viên trong phiên đăng nhập!", true);
            return;
        }

        String faceData = captureFaceDataFromWebcam();
        if (faceData == null || faceData.isBlank()) {
            return;
        }

        setButtonsDisabled(true);
        showMessage("Đang lưu dữ liệu khuôn mặt mẫu lên hệ thống...", false);

        new Thread(() -> {
            try {
                employeeService.registerFaceId(employeeId, faceData);
                Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    showMessage("Đăng ký khuôn mặt thành công! Bạn đã có thể bấm Check-in.", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    showMessage("Lỗi đăng ký khuôn mặt: " + e.getMessage(), true);
                });
            }
        }).start();
    }

    // 4. Lọc bảng công theo tháng (yyyy-MM)
    @FXML
    public void handleFilterByMonth() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) return;

        String monthYear = (txtMonthFilter != null) ? txtMonthFilter.getText().trim() : YearMonth.now().toString();
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

    // 5. Tải toàn bộ lịch sử chấm công
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

    private String captureFaceDataFromWebcam() {
        try {
            return WebcamUtils.captureBase64();
        } catch (Exception e) {
            showMessage(e.getMessage(), true);
            return null;
        }
    }

    private void setButtonsDisabled(boolean disabled) {
        if (btnRegisterFace != null) btnRegisterFace.setDisable(disabled);
        if (btnCheckIn != null) btnCheckIn.setDisable(disabled);
        if (btnCheckOut != null) btnCheckOut.setDisable(disabled);
    }

    private void showMessage(String msg, boolean isError) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        }
    }
}