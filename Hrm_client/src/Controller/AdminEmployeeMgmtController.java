package Controller;

import dto.response.EmployeeResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.EmployeeClientService;
import util.SessionManager;

import java.util.List;

public class AdminEmployeeMgmtController {

    // --- Bảng danh sách nhân viên ---
    @FXML private TableView<EmployeeResponse> tblEmployees;
    @FXML private TableColumn<EmployeeResponse, Long> colId;
    @FXML private TableColumn<EmployeeResponse, String> colFullName;
    @FXML private TableColumn<EmployeeResponse, String> colEmail;
    @FXML private TableColumn<EmployeeResponse, String> colPhone;
    @FXML private TableColumn<EmployeeResponse, String> colPosition;
    @FXML private TableColumn<EmployeeResponse, Integer> colDepartmentId;
    @FXML private TableColumn<EmployeeResponse, Double> colBaseSalary;
    @FXML private TableColumn<EmployeeResponse, Integer> colExperience;

    // --- Form cập nhật thông tin nhân viên ---
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtPosition;
    @FXML private TextField txtDepartmentId;
    @FXML private TextField txtBaseSalary;
    @FXML private TextField txtExperience;

    // --- Form tuyển dụng nhanh từ Candidate ID (nếu Manager duyệt thủ công) ---
    @FXML private TextField txtHireCandidateId;
    @FXML private TextField txtHireBaseSalary;
    @FXML private TextField txtHireDepartmentId;

    // --- Form giao việc hàng ngày (nối TaskClientService ở bước tiếp theo) ---
    @FXML private TextField txtTaskTitle;
    @FXML private TextArea txtTaskDescription;
    @FXML private DatePicker dpTaskDeadline;

    @FXML private Label lblStatusMessage;

    private final EmployeeClientService employeeService = new EmployeeClientService();

    @FXML
    public void initialize() {
        setupTableColumns();

        if (tblEmployees != null) {
            tblEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    fillEmployeeForm(newSel);
                }
            });
        }

        loadAllEmployees();
    }

    private void setupTableColumns() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colFullName != null) colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colPhone != null) colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (colPosition != null) colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        if (colDepartmentId != null) colDepartmentId.setCellValueFactory(new PropertyValueFactory<>("departmentId"));
        if (colBaseSalary != null) colBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        if (colExperience != null) colExperience.setCellValueFactory(new PropertyValueFactory<>("yearsOfExperience"));
    }

    private void fillEmployeeForm(EmployeeResponse emp) {
        if (txtFullName != null) txtFullName.setText(emp.getFullName() != null ? emp.getFullName() : "");
        if (txtEmail != null) txtEmail.setText(emp.getEmail() != null ? emp.getEmail() : "");
        if (txtPhone != null) txtPhone.setText(emp.getPhone() != null ? emp.getPhone() : "");
        if (txtPosition != null) txtPosition.setText(emp.getPosition() != null ? emp.getPosition() : "");
        if (txtDepartmentId != null) {
            txtDepartmentId.setText(emp.getDepartmentId() != null ? String.valueOf(emp.getDepartmentId()) : "");
        }
        if (txtBaseSalary != null) {
            txtBaseSalary.setText(emp.getBaseSalary() != null ? String.valueOf(emp.getBaseSalary()) : "0");
        }
        if (txtExperience != null) {
            txtExperience.setText(emp.getYearsOfExperience() != null ? String.valueOf(emp.getYearsOfExperience()) : "0");
        }
    }

    // 1. Tải toàn bộ nhân viên
    @FXML
    public void loadAllEmployees() {
        new Thread(() -> {
            try {
                List<EmployeeResponse> list = employeeService.getAllEmployees();
                Platform.runLater(() -> {
                    if (tblEmployees != null) {
                        tblEmployees.setItems(FXCollections.observableArrayList(list));
                    }
                    showMessage("Đã tải " + list.size() + " nhân viên.", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("Lỗi tải danh sách nhân viên: " + e.getMessage(), true));
            }
        }).start();
    }

    // 2. Lọc nhân viên dưới quyền quản lý của Manager đang đăng nhập
    @FXML
    public void loadMyTeamEmployees() {
        Long managerId = SessionManager.getEmployeeId();
        if (managerId == null) {
            showMessage("Tài khoản hiện tại chưa gắn với mã quản lý (employeeId)!", true);
            return;
        }

        new Thread(() -> {
            try {
                List<EmployeeResponse> list = employeeService.getEmployeesByManagerId(managerId);
                Platform.runLater(() -> {
                    if (tblEmployees != null) {
                        tblEmployees.setItems(FXCollections.observableArrayList(list));
                    }
                    showMessage("Đang hiển thị " + list.size() + " nhân viên thuộc nhóm của bạn.", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage(e.getMessage(), true));
            }
        }).start();
    }

    // 3. Tuyển dụng ứng viên thành Employee chính thức
    @FXML
    public void handleHireCandidate() {
        if (txtHireCandidateId == null || txtHireCandidateId.getText().trim().isEmpty()) {
            showMessage("Vui lòng nhập ID ứng viên cần tuyển!", true);
            return;
        }

        try {
            Long candidateId = Long.parseLong(txtHireCandidateId.getText().trim());
            Double salary = (txtHireBaseSalary != null && !txtHireBaseSalary.getText().trim().isEmpty())
                    ? Double.parseDouble(txtHireBaseSalary.getText().trim())
                    : 0.0;
            Integer deptId = (txtHireDepartmentId != null && !txtHireDepartmentId.getText().trim().isEmpty())
                    ? Integer.parseInt(txtHireDepartmentId.getText().trim())
                    : null;
            Long managerId = SessionManager.getEmployeeId();

            new Thread(() -> {
                try {
                    EmployeeResponse newEmp = employeeService.hireCandidateToEmployee(candidateId, salary, deptId, managerId);
                    Platform.runLater(() -> {
                        showMessage("Đã tuyển dụng " + newEmp.getFullName() + " thành nhân viên chính thức!", false);
                        loadAllEmployees();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showMessage(e.getMessage(), true));
                }
            }).start();
        } catch (NumberFormatException e) {
            showMessage("ID ứng viên, Lương và Mã phòng ban phải là số hợp lệ!", true);
        }
    }

    // 4. Cập nhật thông tin hồ sơ nhân viên (Đã bọc chống lỗi null)
    @FXML
    public void handleUpdateEmployee() {
        EmployeeResponse selected = (tblEmployees != null)
                ? tblEmployees.getSelectionModel().getSelectedItem()
                : null;

        if (selected == null) {
            showMessage("Vui lòng chọn 1 nhân viên trong bảng để cập nhật!", true);
            return;
        }

        EmployeeResponse updated = new EmployeeResponse();
        if (txtFullName != null) updated.setFullName(txtFullName.getText().trim());
        if (txtEmail != null) updated.setEmail(txtEmail.getText().trim());
        if (txtPhone != null) updated.setPhone(txtPhone.getText().trim());
        if (txtPosition != null) updated.setPosition(txtPosition.getText().trim());

        try {
            if (txtBaseSalary != null && !txtBaseSalary.getText().trim().isEmpty()) {
                updated.setBaseSalary(Double.parseDouble(txtBaseSalary.getText().trim()));
            }
            if (txtDepartmentId != null && !txtDepartmentId.getText().trim().isEmpty()) {
                updated.setDepartmentId(Integer.parseInt(txtDepartmentId.getText().trim()));
            }
            if (txtExperience != null && !txtExperience.getText().trim().isEmpty()) {
                updated.setYearsOfExperience(Integer.parseInt(txtExperience.getText().trim()));
            }
        } catch (NumberFormatException e) {
            showMessage("Lương, Mã phòng ban hoặc Số năm kinh nghiệm phải là số hợp lệ!", true);
            return;
        }

        new Thread(() -> {
            try {
                employeeService.updateEmployee(selected.getId(), updated);
                Platform.runLater(() -> {
                    showMessage("Cập nhật thông tin nhân viên " + selected.getFullName() + " thành công!", false);
                    loadAllEmployees();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage(e.getMessage(), true));
            }
        }).start();
    }

    // 5. Xóa nhân viên (Có hộp thoại xác nhận)
    @FXML
    public void handleDeleteEmployee() {
        EmployeeResponse selected = (tblEmployees != null)
                ? tblEmployees.getSelectionModel().getSelectedItem()
                : null;

        if (selected == null) {
            showMessage("Vui lòng chọn 1 nhân viên để xóa!", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa nhân viên");
        confirm.setHeaderText("Bạn có chắc muốn xóa nhân viên: " + selected.getFullName() + "?");
        confirm.setContentText("Hành động này không thể hoàn tác.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        new Thread(() -> {
            try {
                employeeService.deleteEmployee(selected.getId());
                Platform.runLater(() -> {
                    showMessage("Đã xóa nhân viên: " + selected.getFullName(), false);
                    loadAllEmployees();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("Không thể xóa nhân viên này: " + e.getMessage(), true));
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

