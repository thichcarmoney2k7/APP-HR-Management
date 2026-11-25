package Controller;

import dto.response.AttendanceResponse;
import dto.response.EmployeeResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import service.AttendanceClientService;
import service.EmployeeClientService;
import util.WebcamUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AdminAttendanceController {

    // --- Chọn nhân viên & Camera quét mặt ---
    @FXML private ComboBox<EmployeeResponse> cboEmployee;
    @FXML private ImageView imgWebcam;
    @FXML private Button btnRegisterFace;
    @FXML private Button btnCheckIn;
    @FXML private Button btnCheckOut;

    // --- Trạng thái của nhân viên đang chọn hôm nay ---
    @FXML private Label lblSelectedEmployee;
    @FXML private Label lblCheckInTime;
    @FXML private Label lblCheckOutTime;
    @FXML private Label lblTodayStatus;
    @FXML private Label lblMessage;

    // --- Bảng chấm công theo ngày ---
    @FXML private DatePicker dpDateFilter;
    @FXML private TableView<AttendanceResponse> tblAttendance;
    @FXML private TableColumn<AttendanceResponse, Long> colEmployeeId;
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

        if (dpDateFilter != null) {
            dpDateFilter.setValue(LocalDate.now());
        }

        if (cboEmployee != null) {
            cboEmployee.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    if (lblSelectedEmployee != null) {
                        lblSelectedEmployee.setText("Nhân viên: " + newVal.getFullName() + " (ID: " + newVal.getId() + ")");
                    }
                    loadSelectedEmployeeTodayStatus(newVal.getId());
                }
            });
        }

        loadEmployees();
        handleStartCamera();
        loadAttendanceByDate();
    }

    private void setupTableColumns() {
        if (colEmployeeId != null) colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        if (colWorkDate != null) colWorkDate.setCellValueFactory(new PropertyValueFactory<>("workDate"));
        if (colCheckIn != null) colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        if (colCheckOut != null) colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
        if (colLateMinutes != null) colLateMinutes.setCellValueFactory(new PropertyValueFactory<>("lateMinutes"));
        if (colWorkHours != null) colWorkHours.setCellValueFactory(new PropertyValueFactory<>("workHours"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadEmployees() {
        if (cboEmployee == null) return;
        new Thread(() -> {
            try {
                List<EmployeeResponse> list = employeeService.getAllEmployees();
                Platform.runLater(() -> {
                    cboEmployee.setItems(FXCollections.observableArrayList(list));
                    if (!list.isEmpty()) {
                        cboEmployee.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("Lỗi tải danh sách nhân viên: " + e.getMessage(), true));
            }
        }).start();
    }

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

    @FXML
    public void handleStopCamera() {
        WebcamUtils.stopCamera();
        if (imgWebcam != null) {
            imgWebcam.setImage(null);
        }
    }

    @FXML
    public void handleRegisterFace() {
        EmployeeResponse emp = (cboEmployee != null) ? cboEmployee.getValue() : null;
        if (emp == null) {
            showMessage("Vui lòng chọn nhân viên trước khi đăng ký khuôn mặt!", true);
            return;
        }

        String faceData = captureFaceDataFromWebcam();
        if (faceData == null || faceData.isBlank()) return;

        setButtonsDisabled(true);
        showMessage("Đang lưu khuôn mặt mẫu cho " + emp.getFullName() + "...", false);

        new Thread(() -> {
            try {
                employeeService.registerFaceId(emp.getId(), faceData);
                Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    showMessage("Đăng ký khuôn mặt thành công cho " + emp.getFullName() + "!", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    showMessage("Lỗi đăng ký khuôn mặt: " + e.getMessage(), true);
                });
            }
        }).start();
    }

    @FXML
    public void handleCheckIn() {
        EmployeeResponse emp = (cboEmployee != null) ? cboEmployee.getValue() : null;
        if (emp == null) {
            showMessage("Vui lòng chọn nhân viên cần check-in!", true);
            return;
        }

        String faceData = captureFaceDataFromWebcam();
        if (faceData == null || faceData.isBlank()) return;

        setButtonsDisabled(true);
        showMessage("Đang xác thực khuôn mặt và check-in cho " + emp.getFullName() + "...", false);

        new Thread(() -> {
            try {
                AttendanceResponse res = attendanceService.checkIn(emp.getId(), faceData);
                Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    if (res != null && res.getAttendanceId() != null) {
                        showMessage(res.getMessage(), false);
                        updateTodayLabels(res);
                        loadAttendanceByDate();
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

    @FXML
    public void handleCheckOut() {
        EmployeeResponse emp = (cboEmployee != null) ? cboEmployee.getValue() : null;
        if (emp == null) {
            showMessage("Vui lòng chọn nhân viên cần check-out!", true);
            return;
        }

        setButtonsDisabled(true);
        showMessage("Đang xử lý check-out cho " + emp.getFullName() + "...", false);

        new Thread(() -> {
            try {
                AttendanceResponse res = attendanceService.checkOut(emp.getId());
                Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    if (res != null && res.getCheckOutTime() != null) {
                        showMessage(res.getMessage() + " (Tổng giờ làm: " + res.getWorkHours() + "h)", false);
                        updateTodayLabels(res);
                        loadAttendanceByDate();
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

    @FXML
    public void loadAttendanceByDate() {
        LocalDate date = (dpDateFilter != null && dpDateFilter.getValue() != null)
                ? dpDateFilter.getValue()
                : LocalDate.now();

        new Thread(() -> {
            try {
                List<AttendanceResponse> list = attendanceService.getAllAttendanceByDate(date);
                Platform.runLater(() -> {
                    if (tblAttendance != null) {
                        tblAttendance.setItems(FXCollections.observableArrayList(list));
                    }
                    showMessage("Đã tải " + list.size() + " bản ghi chấm công ngày " + date + ".", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("Lỗi tải bảng chấm công theo ngày: " + e.getMessage(), true));
            }
        }).start();
    }

    private void loadSelectedEmployeeTodayStatus(Long employeeId) {
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
                        if (btnCheckIn != null) btnCheckIn.setDisable(false);
                        if (btnCheckOut != null) btnCheckOut.setDisable(true);
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

        boolean hasCheckedIn = att.getCheckInTime() != null;
        boolean hasCheckedOut = att.getCheckOutTime() != null;
        if (btnCheckIn != null) btnCheckIn.setDisable(hasCheckedIn);
        if (btnCheckOut != null) btnCheckOut.setDisable(!hasCheckedIn || hasCheckedOut);
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