package dto.response;

import java.util.List;

public class InterviewResponse {

    private Long candidateId;
    private String position;
    private String generatedQuestions; // Danh sách các câu hỏi do AI tạo ra
    private String message;

    public InterviewResponse() {
    }

    public InterviewResponse(Long candidateId, String position, String generatedQuestions, String message) {
        this.candidateId = candidateId;
        this.position = position;
        this.generatedQuestions = generatedQuestions;
        this.message = message;
    }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getGeneratedQuestions() { return generatedQuestions; }
    public void setGeneratedQuestions(String generatedQuestions) { this.generatedQuestions = generatedQuestions; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}