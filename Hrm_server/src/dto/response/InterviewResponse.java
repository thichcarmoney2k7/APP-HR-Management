package dto.response;

import java.util.List;

public class InterviewResponse {

    private Long candidateId;
    private String position;
    private List<String> generatedQuestions; // Danh sách các câu hỏi do AI tạo ra
    private String message;

    public InterviewResponse() {
    }

    public InterviewResponse(Long candidateId, String position, List<String> generatedQuestions, String message) {
        this.candidateId = candidateId;
        this.position = position;
        this.generatedQuestions = generatedQuestions;
        this.message = message;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<String> getGeneratedQuestions() {
        return generatedQuestions;
    }

    public void setGeneratedQuestions(List<String> generatedQuestions) {
        this.generatedQuestions = generatedQuestions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}