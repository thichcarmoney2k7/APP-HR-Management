package dto.response;

public class CVExtractResponse {

    private Long candidateId;
    private String fullName;
    private String email;
    private String phone;
    private String positionApplied;
    private Integer yearsOfExperience;
    private Double aiScore;
    private String aiFeedback;
    private String status;
    private String interviewQuestions;
    private String message;

    public CVExtractResponse() {
    }

    public CVExtractResponse(Long candidateId, String fullName, String email, String phone,
                             String positionApplied, Integer yearsOfExperience, Double aiScore,
                             String aiFeedback, String status, String interviewQuestions, String message) {
        this.candidateId = candidateId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.positionApplied = positionApplied;
        this.yearsOfExperience = yearsOfExperience;
        this.aiScore = aiScore;
        this.aiFeedback = aiFeedback;
        this.status = status;
        this.interviewQuestions = interviewQuestions;
        this.message = message;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPositionApplied() {
        return positionApplied;
    }

    public void setPositionApplied(String positionApplied) {
        this.positionApplied = positionApplied;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public Double getAiScore() {
        return aiScore;
    }

    public void setAiScore(Double aiScore) {
        this.aiScore = aiScore;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInterviewQuestions() {
        return interviewQuestions;
    }

    public void setInterviewQuestions(String interviewQuestions) {
        this.interviewQuestions = interviewQuestions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}