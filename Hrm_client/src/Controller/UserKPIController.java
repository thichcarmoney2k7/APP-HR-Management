package Controller;

import dto.response.KPISummaryResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.KPIClientService;
import util.SessionManager;

import java.time.YearMonth;
import java.util.List;

public class UserKPIController {

    // --- Tra cứu KPI tháng cụ thể ---
    @FXML private TextField txtMonthYear; // Định dạng yyyy-MM (VD: 2026-11)
    @FXML private Label lblCompletionRate;
    @FXML private Label lblTaskSummary;
    @FXML private Label lblAttendanceSummary;
    @FXML private Label lblBonusAmount;
    @FXML private TextArea txtAiFeedback;

    // --- Bảng lịch sử KPI qua các tháng ---
    @FXML private TableView<KPISummaryResponse> tblKpiHistory;
    @FXML private TableColumn<KPISummaryResponse, String> colMonthYear;
    @FXML private TableColumn<KPISummaryResponse, Double> colCompletionRate;
    @FXML private TableColumn<KPISummaryResponse, Double> colBonusAmount;
    @FXML private TableColumn<KPISummaryResponse, String> colFeedback;

    @FXML private Label lblStatusMessage;

    private final KPIClientService kpiService = new KPIClientService();

    @FXML
    public void initialize() {
        setupHistoryTable();

        if (txtMonthYear != null) {
            txtMonthYear.setText(YearMonth.now().toString());
        }

        // Bấm chọn 1 tháng trong bảng lịch sử -> tải chi tiết tháng đó
        if (tblKpiHistory != null) {
            tblKpiHistory.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null && newSel.getMonthYear() != null) {
                    if (txtMonthYear != null) txtMonthYear.setText(newSel.getMonthYear());
                    loadMonthlyKPI();
                }
            });
        }

        loadMonthlyKPI();
        loadKPIHistory();
    }

    private void setupHistoryTable() {
        if (colMonthYear != null) colMonthYear.setCellValueFactory(new PropertyValueFactory<>("monthYear"));
        if (colCompletionRate != null) colCompletionRate.setCellValueFactory(new PropertyValueFactory<>("completionRate"));
        if (colBonusAmount != null) colBonusAmount.setCellValueFactory(new PropertyValueFactory<>("bonusAmount"));
        if (colFeedback != null) colFeedback.setCellValueFactory(new PropertyValueFactory<>("aiFeedback"));
    }

    // 1. Tải chi tiết KPI của nhân viên đang đăng nhập theo tháng
    @FXML
    public void loadMonthlyKPI() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) {
            showMessage("Không tìm thấy mã nhân viên trong phiên đăng nhập!", true);
            return;
        }

        String monthYear = (txtMonthYear != null && !txtMonthYear.getText().isBlank())
                ? txtMonthYear.getText().trim()
                : YearMonth.now().toString();

        new Thread(() -> {
            try {
                KPISummaryResponse res = kpiService.getKPIByEmployeeAndMonth(employeeId, monthYear);
                Platform.runLater(() -> {
                    if (res != null && res.getEmployeeId() != null) {
                        if (lblCompletionRate != null) {
                            lblCompletionRate.setText(String.format("Tỷ lệ hoàn thành KPI: %.1f%%", res.getCompletionRate()));
                        }
                        if (lblTaskSummary != null) {
                            lblTaskSummary.setText("Công việc hoàn thành: " + res.getCompletedTasks() + " / " + res.getTotalTasks());
                        }
                        if (lblAttendanceSummary != null) {
                            lblAttendanceSummary.setText("Ngày đi làm: " + res.getDaysWorked() + " ngày | Đi muộn: " + res.getTotalLateMinutes() + " phút");
                        }
                        if (lblBonusAmount != null) {
                            double bonus = res.getBonusAmount() != null ? res.getBonusAmount() : 0.0;
                            lblBonusAmount.setText(String.format("Thưởng KPI: %,.0f VNĐ", bonus));
                        }
                        if (txtAiFeedback != null) {
                            txtAiFeedback.setText(res.getAiFeedback() != null ? res.getAiFeedback() : "");
                        }
                        showMessage("Đã tải dữ liệu KPI tháng " + monthYear, false);
                    } else {
                        if (lblCompletionRate != null) lblCompletionRate.setText("Tỷ lệ hoàn thành KPI: Chưa đánh giá");
                        if (lblTaskSummary != null) lblTaskSummary.setText("Công việc hoàn thành: --");
                        if (lblAttendanceSummary != null) lblAttendanceSummary.setText("Ngày đi làm: --");
                        if (lblBonusAmount != null) lblBonusAmount.setText("Thưởng KPI: 0 VNĐ");
                        if (txtAiFeedback != null) {
                            txtAiFeedback.setText(res != null && res.getAiFeedback() != null
                                    ? res.getAiFeedback()
                                    : "Chưa có dữ liệu KPI cho tháng " + monthYear);
                        }
                        showMessage("Tháng " + monthYear + " chưa có kết quả đánh giá KPI.", true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage(e.getMessage(), true));
            }
        }).start();
    }

    // 2. Tải lịch sử KPI qua các tháng của nhân viên
    @FXML
    public void loadKPIHistory() {
        Long employeeId = SessionManager.getEmployeeId();
        if (employeeId == null) return;

        new Thread(() -> {
            try {
                List<KPISummaryResponse> history = kpiService.getKPIHistoryByEmployee(employeeId);
                Platform.runLater(() -> {
                    if (tblKpiHistory != null) {
                        tblKpiHistory.setItems(FXCollections.observableArrayList(history));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("Lỗi tải lịch sử KPI: " + e.getMessage(), true));
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
