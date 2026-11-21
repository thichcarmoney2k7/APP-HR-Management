package Controller;

import dto.response.EmployeeResponse;
import dto.response.KPISummaryResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.EmployeeClientService;
import service.KPIClientService;

import java.time.YearMonth;
import java.util.List;

public class AdminKPIController {

    // --- Khu vực chọn nhân viên & tháng để chạy AI đánh giá ---
    @FXML private ComboBox<EmployeeResponse> cboEmployee;
    @FXML private TextField txtMonthYear; // Định dạng yyyy-MM (VD: 2026-11)
    @FXML private Button btnEvaluateKPI;
    @FXML private Button btnLoadReport;

    // --- Bảng báo cáo KPI toàn công ty ---
    @FXML private TableView<KPISummaryResponse> tblCompanyKPI;
    @FXML private TableColumn<KPISummaryResponse, Long> colEmployeeId;
    @FXML private TableColumn<KPISummaryResponse, String> colEmployeeName;
    @FXML private TableColumn<KPISummaryResponse, String> colMonthYear;
    @FXML private TableColumn<KPISummaryResponse, Double> colCompletionRate;
    @FXML private TableColumn<KPISummaryResponse, Double> colBonusAmount;

    // --- Khu vực hiển thị chi tiết dòng đang chọn ---
    @FXML private Label lblTaskStats;
    @FXML private Label lblAttendanceStats;
    @FXML private Label lblBonusDetail;
    @FXML private TextArea txtAiFeedback;
    @FXML private Label lblStatusMessage;

    private final KPIClientService kpiService = new KPIClientService();
    private final EmployeeClientService employeeService = new EmployeeClientService();

    @FXML
    public void initialize() {
        setupTableColumns();

        if (txtMonthYear != null) {
            txtMonthYear.setText(YearMonth.now().toString());
        }

        // Bấm chọn 1 dòng trên bảng KPI -> hiển thị chi tiết nhận xét AI & thưởng
        if (tblCompanyKPI != null) {
            tblCompanyKPI.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    showKPIDetail(newSel);
                }
            });
        }

        loadEmployeeComboBox();
        loadCompanyReport();
    }

    private void setupTableColumns() {
        if (colEmployeeId != null) colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        if (colEmployeeName != null) colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        if (colMonthYear != null) colMonthYear.setCellValueFactory(new PropertyValueFactory<>("monthYear"));
        if (colCompletionRate != null) colCompletionRate.setCellValueFactory(new PropertyValueFactory<>("completionRate"));
        if (colBonusAmount != null) colBonusAmount.setCellValueFactory(new PropertyValueFactory<>("bonusAmount"));
    }

    private void loadEmployeeComboBox() {
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

    // 1. Bấm nút "Đánh giá KPI bằng AI" cho nhân viên được chọn
    @FXML
    public void handleEvaluateKPI() {
        EmployeeResponse selectedEmp = (cboEmployee != null) ? cboEmployee.getValue() : null;
        if (selectedEmp == null) {
            showMessage("Vui lòng chọn nhân viên cần đánh giá KPI!", true);
            return;
        }

        String monthYear = (txtMonthYear != null && !txtMonthYear.getText().isBlank())
                ? txtMonthYear.getText().trim()
                : YearMonth.now().toString();

        if (btnEvaluateKPI != null) btnEvaluateKPI.setDisable(true);
        showMessage("Gemini AI đang tổng hợp Task, Chấm công và tính KPI cho " + selectedEmp.getFullName() + "...", false);

        new Thread(() -> {
            try {
                KPISummaryResponse result = kpiService.evaluateEmployeeKPI(selectedEmp.getId(), monthYear);
                Platform.runLater(() -> {
                    if (btnEvaluateKPI != null) btnEvaluateKPI.setDisable(false);
                    showKPIDetail(result);
                    showMessage("Đánh giá KPI tháng " + monthYear + " thành công!", false);
                    loadCompanyReport();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (btnEvaluateKPI != null) btnEvaluateKPI.setDisable(false);
                    showMessage(e.getMessage(), true);
                });
            }
        }).start();
    }

    // 2. Tải báo cáo KPI toàn công ty theo tháng
    @FXML
    public void loadCompanyReport() {
        String monthYear = (txtMonthYear != null && !txtMonthYear.getText().isBlank())
                ? txtMonthYear.getText().trim()
                : YearMonth.now().toString();

        new Thread(() -> {
            try {
                List<KPISummaryResponse> reportList = kpiService.getCompanyKPIByMonth(monthYear);
                Platform.runLater(() -> {
                    if (tblCompanyKPI != null) {
                        tblCompanyKPI.setItems(FXCollections.observableArrayList(reportList));
                    }
                    showMessage("Đã tải báo cáo KPI tháng " + monthYear + " (" + reportList.size() + " nhân viên).", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage(e.getMessage(), true));
            }
        }).start();
    }

    private void showKPIDetail(KPISummaryResponse kpi) {
        if (lblTaskStats != null) {
            lblTaskStats.setText(String.format("Tiến độ Task: %d / %d (%.1f%%)",
                    kpi.getCompletedTasks(), kpi.getTotalTasks(), kpi.getCompletionRate()));
        }
        if (lblAttendanceStats != null) {
            lblAttendanceStats.setText(String.format("Chuyên cần: %d ngày làm | Đi muộn: %d phút",
                    kpi.getDaysWorked(), kpi.getTotalLateMinutes()));
        }
        if (lblBonusDetail != null) {
            double bonus = kpi.getBonusAmount() != null ? kpi.getBonusAmount() : 0.0;
            lblBonusDetail.setText(String.format("Thưởng đề xuất: %,.0f VNĐ", bonus));
        }
        if (txtAiFeedback != null) {
            txtAiFeedback.setText(kpi.getAiFeedback() != null ? kpi.getAiFeedback() : "");
        }
    }

    private void showMessage(String msg, boolean isError) {
        if (lblStatusMessage != null) {
            lblStatusMessage.setText(msg);
            lblStatusMessage.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        }
    }
}