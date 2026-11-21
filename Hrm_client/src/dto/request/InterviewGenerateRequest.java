//Yêu cầu AI tạo câu hỏi phỏng vấn
package dto.request;

public class InterviewGenerateRequest {

    private Long candidateId;
    private String position;
    private String jobDescription; // Yêu cầu công việc (JD) để AI đọc và tạo câu hỏi

    public InterviewGenerateRequest() {
    }

    public InterviewGenerateRequest(Long candidateId, String position, String jobDescription) {
        this.candidateId = candidateId;
        this.position = position;
        this.jobDescription = jobDescription;
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

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}