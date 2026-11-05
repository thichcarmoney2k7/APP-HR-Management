//Thông tin ứng viên AI bóc tách được
package dto.response;

public class CVExtractResponse {

    private Long candidateId;
    private String fullName;
    private String positionApplied;
    private Integer aiScore;
    private String aiFeedback;
    private String status;

    public CVExtractResponse() {
    }

    public CVExtractResponse(Long candidateId, String fullName, String positionApplied,
                             Integer aiScore, String aiFeedback, String status) {
        this.candidateId = candidateId;
        this.fullName = fullName;
        this.positionApplied = positionApplied;
        this.aiScore = aiScore;
        this.aiFeedback = aiFeedback;
        this.status = status;
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

    public String getPositionApplied() {
        return positionApplied;
    }

    public void setPositionApplied(String positionApplied) {
        this.positionApplied = positionApplied;
    }

    public Integer getAiScore() {
        return aiScore;
    }

    public void setAiScore(Integer aiScore) {
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
}