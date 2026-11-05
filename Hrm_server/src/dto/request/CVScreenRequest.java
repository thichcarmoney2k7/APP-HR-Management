//Gửi file PDF CV để phân tích
package dto.request;

public class CVScreenRequest {

    private Long candidateId;
    private String cvText;
    private String jobPosition;

    public CVScreenRequest() {
    }

    public CVScreenRequest(Long candidateId, String cvText, String jobPosition) {
        this.candidateId = candidateId;
        this.cvText = cvText;
        this.jobPosition = jobPosition;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getCvText() {
        return cvText;
    }

    public void setCvText(String cvText) {
        this.cvText = cvText;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }
}