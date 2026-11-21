package Controller;

import dto.response.CVExtractResponse;
import dto.response.EmployeeResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.CandidateClientService;
import service.EmployeeClientService;
import util.SessionManager;

import java.util.List;

public class AdminCVScreeningController {

    // --- Bộ lọc & Bảng danh sách ứng viên ---
    @FXML private ComboBox<String> cboStatusFilter;
    @FXML private TableView<CVExtractResponse> tblCandidates;
    @FXML private TableColumn<CVExtractResponse, Long> colId;
    @FXML private TableColumn<CVExtractResponse, String> colFullName;
    @FXML private TableColumn<CVExtractResponse, String> colEmail;
    @FXML private TableColumn<CVExtractResponse, String> colPosition;
    @FXML private TableColumn<CVExtractResponse, Integer> colExperience;
    @FXML private TableColumn<CVExtractResponse, Double> colAiScore;
    @FXML private TableColumn<CVExtractResponse, String> colStatus;

    // --- Chi tiết đánh giá CV & Phỏng vấn của AI ---
    @FXML private TextArea txtAiFeedbackDetail;
    @FXML private Button btnApprove;
    @FXML private Button btnReject;

    // --- Khu vực Tuyển dụng ứng viên đạt chuẩn thành Nhân viên chính thức ---
    @FXML private TextField txtOfferSalary;
    @FXML private TextField txtDepartmentId;
    @FXML private Button btnHireCandidate;

    @FXML private Label lblStatusMessage;

    private final CandidateClientService candidateService = new CandidateClientService();
    private final EmployeeClientService employeeService = new EmployeeClientService();

    @FXML
    public void initialize() {
        setupTable();

        if (cboStatusFilter != null) {
            cboStatusFilter.setItems(FXCollections.observableArrayList(
                    "ALL", "APPROVED", "PENDING", "REJECTED", "HIRED"
            ));
            cboStatusFilter.setValue("ALL");
        }

        if (btnHireCandidate != null) {
            btnHireCandidate.setDisable(true);
        }

        // Khi chọn 1 ứng viên trên bảng -> hiển thị đánh giá AI & bật/tắt nút Tuyển dụng
        if (tblCandidates != null) {
            tblCandidates.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (txtAiFeedbackDetail != null) {
                        txtAiFeedbackDetail.setText(
                                newSel.getAiFeedback() != null ? newSel.getAiFeedback() : "Chưa có nhận xét từ AI."
                        );
                    }
                    // Chỉ cho phép bấm Tuyển dụng khi ứng viên đang ở trạng thái APPROVED
                    boolean canHire = "APPROVED".equalsIgnoreCase(newSel.getStatus());
                    if (btnHireCandidate != null) {
                        btnHireCandidate.setDisable(!canHire);
                    }
                }
            });
        }

        loadCandidates();
    }

    private void setupTable() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("candidateId"));
        if (colFullName != null) colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colPosition != null) colPosition.setCellValueFactory(new PropertyValueFactory<>("positionApplied"));
        if (colExperience != null) colExperience.setCellValueFactory(new PropertyValueFactory<>("yearsOfExperience"));
        if (colAiScore != null) colAiScore.setCellValueFactory(new PropertyValueFactory<>("aiScore"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    public void loadCandidates() {
        String selectedStatus = (cboStatusFilter != null && cboStatusFilter.getValue() != null)
                ? cboStatusFilter.getValue()
                : "ALL";

        new Thread(() -> {
            try {
                List<CVExtractResponse> list = "ALL".equalsIgnoreCase(selectedStatus)
                        ? candidateService.getAllCandidates()
                        : candidateService.getCandidatesByStatus(selectedStatus);

                Platform.runLater(() -> {
                    if (tblCandidates != null) {
                        tblCandidates.setItems(FXCollections.observableArrayList(list));
                    }
                    showMessage("Đã tải " + list.size() + " hồ sơ ứng viên.", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("Lỗi tải danh sách ứng viên: " + e.getMessage(), true));
            }
        }).start();
    }

    // Duyệt thủ công ứng viên (nếu Manager muốn cho qua vòng CV/Phỏng vấn)
    @FXML
    public void handleApproveCandidate() {
        changeSelectedCandidateStatus("APPROVED");
    }

    // Từ chối ứng viên
    @FXML
    public void handleRejectCandidate() {
        changeSelectedCandidateStatus("REJECTED");
    }

    // Tuyển dụng ứng viên APPROVED thành Employee chính thức
    @FXML
    public void handleHireCandidate() {
        CVExtractResponse selected = (tblCandidates != null)
                ? tblCandidates.getSelectionModel().getSelectedItem()
                : null;

        if (selected == null) {
            showMessage("Vui lòng chọn 1 ứng viên trong bảng!", true);
            return;
        }
        if (!"APPROVED".equalsIgnoreCase(selected.getStatus())) {
            showMessage("Chỉ có thể tuyển dụng ứng viên đang ở trạng thái APPROVED!", true);
            return;
        }

        double baseSalary = 0.0;
        Integer departmentId = null;

        try {
            if (txtOfferSalary != null && !txtOfferSalary.getText().trim().isEmpty()) {
                baseSalary = Double.parseDouble(txtOfferSalary.getText().trim());
            }
            if (txtDepartmentId != null && !txtDepartmentId.getText().trim().isEmpty()) {
                departmentId = Integer.parseInt(txtDepartmentId.getText().trim());
            }
        } catch (NumberFormatException e) {
            showMessage("Lương cơ bản và Mã phòng ban phải là số hợp lệ!", true);
            return;
        }

        Long managerId = SessionManager.getEmployeeId();
        final double finalSalary = baseSalary;
        final Integer finalDeptId = departmentId;

        if (btnHireCandidate != null) btnHireCandidate.setDisable(true);
        showMessage("Đang tạo hồ sơ nhân viên và cấp quyền tài khoản cho " + selected.getFullName() + "...", false);

        new Thread(() -> {
            try {
                EmployeeResponse newEmp = employeeService.hireCandidateToEmployee(
                        selected.getCandidateId(), finalSalary, finalDeptId, managerId
                );
                Platform.runLater(() -> {
                    showMessage("Tuyển dụng thành công! " + newEmp.getFullName()
                            + " đã trở thành Nhân viên (Mã NV: " + newEmp.getId() + ").", false);
                    if (txtOfferSalary != null) txtOfferSalary.clear();
                    if (txtDepartmentId != null) txtDepartmentId.clear();
                    loadCandidates(); // Tải lại bảng -> trạng thái ứng viên chuyển thành HIRED
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (btnHireCandidate != null) btnHireCandidate.setDisable(false);
                    showMessage(e.getMessage(), true);
                });
            }
        }).start();
    }

    private void changeSelectedCandidateStatus(String newStatus) {
        CVExtractResponse selected = (tblCandidates != null)
                ? tblCandidates.getSelectionModel().getSelectedItem()
                : null;

        if (selected == null) {
            showMessage("Vui lòng chọn 1 ứng viên trong bảng trước!", true);
            return;
        }

        new Thread(() -> {
            try {
                CVExtractResponse updated = candidateService.updateCandidateStatus(selected.getCandidateId(), newStatus);
                Platform.runLater(() -> {
                    showMessage(updated.getMessage() + " (" + selected.getFullName() + " -> " + newStatus + ")", false);
                    loadCandidates();
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
