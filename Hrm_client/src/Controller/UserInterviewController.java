package Controller;

import dto.request.CVScreenRequest;
import dto.response.CVExtractResponse;
import dto.response.InterviewResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import service.CandidateClientService;
import service.InterviewClientService;
import util.SessionManager;
import util.WebcamUtils;

public class UserInterviewController {

    // --- Khu vực Nộp CV cho AI chấm điểm ---
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtPositionApplied;
    @FXML private TextField txtYearsOfExperience;
    @FXML private TextArea txtCvContent;
    @FXML private Button btnSubmitCV;

    // --- Khu vực Kết quả chấm CV ---
    @FXML private Label lblAiScore;
    @FXML private Label lblCandidateStatus;
    @FXML private TextArea txtAiFeedback;

    // --- Khu vực Phỏng vấn AI (Mở khóa khi đạt CV) ---
    @FXML private ImageView imgWebcam; // Tùy chọn: khung camera giám sát lúc phỏng vấn (nếu FXML có)
    @FXML private Button btnStartInterview;
    @FXML private TextArea txtInterviewQuestions;
    @FXML private TextArea txtCandidateAnswers;
    @FXML private Button btnSubmitInterview;
    @FXML private Label lblStatusMessage;

    private final CandidateClientService candidateService = new CandidateClientService();
    private final InterviewClientService interviewService = new InterviewClientService();
    private Long currentCandidateId;

    @FXML
    public void initialize() {
        if (txtFullName != null && SessionManager.getFullName() != null) {
            txtFullName.setText(SessionManager.getFullName());
        }
        if (txtEmail != null && SessionManager.getUsername() != null) {
            txtEmail.setText(SessionManager.getUsername());
            txtEmail.setEditable(false);
        }
        if (txtInterviewQuestions != null) {
            txtInterviewQuestions.setEditable(false);
        }
        if (btnStartInterview != null) {
            btnStartInterview.setDisable(true);
        }
        if (btnSubmitInterview != null) {
            btnSubmitInterview.setDisable(true);
        }

        // Khôi phục hồ sơ, điểm CV & bộ câu hỏi cũ nếu ứng viên đã từng thao tác trước đó
        loadExistingCandidateProfile();
    }

    // Bước 1: Ứng viên gửi CV cho Gemini AI phân tích & chấm điểm
    @FXML
    public void handleSubmitCV() {
        String fullName = (txtFullName != null) ? txtFullName.getText().trim() : SessionManager.getFullName();
        String email = (txtEmail != null && !txtEmail.getText().isBlank())
                ? txtEmail.getText().trim()
                : SessionManager.getUsername();
        String phone = (txtPhone != null) ? txtPhone.getText().trim() : "";
        String position = (txtPositionApplied != null) ? txtPositionApplied.getText().trim() : "";
        String cvText = (txtCvContent != null) ? txtCvContent.getText().trim() : "";

        if (position.isEmpty() || cvText.isEmpty()) {
            showMessage("Vui lòng nhập Vị trí ứng tuyển và Nội dung CV!", true);
            return;
        }

        int yearsExp = 0;
        if (txtYearsOfExperience != null && !txtYearsOfExperience.getText().trim().isEmpty()) {
            try {
                yearsExp = Integer.parseInt(txtYearsOfExperience.getText().trim());
            } catch (NumberFormatException e) {
                showMessage("Số năm kinh nghiệm phải là số nguyên!", true);
                return;
            }
        }

        CVScreenRequest request = new CVScreenRequest();
        request.setFullName(fullName);
        request.setEmail(email);
        request.setPhone(phone);
        request.setPositionApplied(position);
        request.setYearsOfExperience(yearsExp);
        request.setCvText(cvText);

        if (btnSubmitCV != null) btnSubmitCV.setDisable(true);
        showMessage("Gemini AI đang phân tích và chấm điểm CV của bạn...", false);

        new Thread(() -> {
            try {
                CVExtractResponse res = candidateService.screenCV(request);
                this.currentCandidateId = res.getCandidateId();

                Platform.runLater(() -> {
                    if (btnSubmitCV != null) btnSubmitCV.setDisable(false);
                    if (lblAiScore != null) {
                        lblAiScore.setText("Điểm CV: " + res.getAiScore() + " / 100");
                    }
                    if (lblCandidateStatus != null) {
                        lblCandidateStatus.setText("Trạng thái: " + res.getStatus());
                    }
                    if (txtAiFeedback != null) {
                        txtAiFeedback.setText(res.getAiFeedback());
                    }

                    boolean isApproved = "APPROVED".equalsIgnoreCase(res.getStatus());
                    if (btnStartInterview != null) {
                        btnStartInterview.setDisable(!isApproved);
                    }

                    if (isApproved) {
                        showMessage("Chúc mừng! CV đạt yêu cầu. Hãy bấm 'Bắt đầu phỏng vấn AI'!", false);
                    } else {
                        showMessage("CV chưa đạt điểm chuẩn để vào vòng phỏng vấn. Bạn có thể chỉnh sửa và nộp lại.", true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (btnSubmitCV != null) btnSubmitCV.setDisable(false);
                    showMessage(e.getMessage(), true);
                });
            }
        }).start();
    }

    // Bước 2: Lấy/Sinh bộ 5 câu hỏi phỏng vấn AI & bật camera giám sát (nếu có)
    @FXML
    public void handleStartInterview() {
        if (currentCandidateId == null) {
            showMessage("Chưa xác định được mã hồ sơ ứng viên!", true);
            return;
        }

        if (btnStartInterview != null) btnStartInterview.setDisable(true);
        showMessage("Đang tải bộ câu hỏi phỏng vấn từ Gemini AI...", false);

        // Nếu FXML có khung ImageView giám sát phỏng vấn thì bật webcam
        if (imgWebcam != null) {
            try {
                WebcamUtils.startCamera(imgWebcam);
            } catch (Exception ignored) {
            }
        }

        new Thread(() -> {
            try {
                InterviewResponse res = interviewService.getOrCreateInterviewQuestions(currentCandidateId);
                Platform.runLater(() -> {
                    if (res != null && res.getGeneratedQuestions() != null) {
                        if (txtInterviewQuestions != null) {
                            txtInterviewQuestions.setText(res.getGeneratedQuestions());
                        }
                        if (btnSubmitInterview != null) {
                            btnSubmitInterview.setDisable(false);
                        }
                        showMessage("Đã sẵn sàng! Hãy nhập câu trả lời của bạn vào khung bên dưới và bấm Nộp bài.", false);
                    } else {
                        if (btnStartInterview != null) btnStartInterview.setDisable(false);
                        showMessage(res != null ? res.getMessage() : "Không lấy được câu hỏi phỏng vấn!", true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (btnStartInterview != null) btnStartInterview.setDisable(false);
                    showMessage(e.getMessage(), true);
                });
            }
        }).start();
    }

    // Bước 3: Nộp câu trả lời để Gemini AI chấm điểm vòng phỏng vấn
    @FXML
    public void handleSubmitInterview() {
        if (currentCandidateId == null) {
            showMessage("Chưa xác định được mã hồ sơ ứng viên!", true);
            return;
        }

        String answers = (txtCandidateAnswers != null) ? txtCandidateAnswers.getText().trim() : "";
        if (answers.isEmpty()) {
            showMessage("Vui lòng nhập câu trả lời phỏng vấn trước khi nộp bài!", true);
            return;
        }

        if (btnSubmitInterview != null) btnSubmitInterview.setDisable(true);
        showMessage("Gemini AI đang chấm điểm bài phỏng vấn của bạn...", false);

        new Thread(() -> {
            try {
                InterviewResponse res = interviewService.evaluateInterview(currentCandidateId, answers);
                Platform.runLater(() -> {
                    WebcamUtils.stopCamera();
                    String resultMsg = (res != null && res.getMessage() != null) ? res.getMessage() : "";
                    boolean isPassed = resultMsg.startsWith("ĐẠT PHỎNG VẤN");

                    if (txtAiFeedback != null) {
                        txtAiFeedback.setText(resultMsg);
                    }
                    if (lblCandidateStatus != null) {
                        lblCandidateStatus.setText(isPassed ? "Trạng thái: APPROVED (Chờ tuyển dụng)" : "Trạng thái: REJECTED");
                    }

                    if (isPassed) {
                        showMessage("Chúc mừng bạn đã ĐẠT vòng phỏng vấn AI! Vui lòng chờ Manager xác nhận tuyển dụng để mở khóa tính năng Nhân viên.", false);
                    } else {
                        showMessage("Rất tiếc, bài phỏng vấn của bạn chưa đạt điểm chuẩn.", true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (btnSubmitInterview != null) btnSubmitInterview.setDisable(false);
                    showMessage(e.getMessage(), true);
                });
            }
        }).start();
    }

    private void loadExistingCandidateProfile() {
        String email = SessionManager.getUsername();
        if (email == null || email.isBlank()) return;

        new Thread(() -> {
            try {
                CVExtractResponse existing = candidateService.getCandidateByEmail(email);
                if (existing != null) {
                    this.currentCandidateId = existing.getCandidateId();

                    Platform.runLater(() -> {
                        if (txtPhone != null && existing.getPhone() != null) {
                            txtPhone.setText(existing.getPhone());
                        }
                        if (txtPositionApplied != null && existing.getPositionApplied() != null) {
                            txtPositionApplied.setText(existing.getPositionApplied());
                        }
                        if (txtYearsOfExperience != null && existing.getYearsOfExperience() != null) {
                            txtYearsOfExperience.setText(String.valueOf(existing.getYearsOfExperience()));
                        }

                        if (existing.getAiScore() != null) {
                            if (lblAiScore != null) {
                                lblAiScore.setText("Điểm hiện tại: " + existing.getAiScore() + " / 100");
                            }
                            if (lblCandidateStatus != null) {
                                lblCandidateStatus.setText("Trạng thái: " + existing.getStatus());
                            }
                            if (txtAiFeedback != null && existing.getAiFeedback() != null) {
                                txtAiFeedback.setText(existing.getAiFeedback());
                            }

                            boolean isHired = "HIRED".equalsIgnoreCase(existing.getStatus());
                            boolean isApproved = "APPROVED".equalsIgnoreCase(existing.getStatus());
                            if (btnStartInterview != null) {
                                btnStartInterview.setDisable(!isApproved);
                            }
                            if (btnSubmitCV != null) {
                                btnSubmitCV.setDisable(isHired);
                            }
                        }

                        if (existing.getInterviewQuestions() != null && !existing.getInterviewQuestions().isBlank()) {
                            if (txtInterviewQuestions != null) {
                                txtInterviewQuestions.setText(existing.getInterviewQuestions());
                            }
                            if (btnSubmitInterview != null && "APPROVED".equalsIgnoreCase(existing.getStatus())) {
                                btnSubmitInterview.setDisable(false);
                            }
                        }
                    });
                }
            } catch (Exception ignored) {
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

