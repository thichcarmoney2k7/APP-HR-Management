package entity;//Dữ liệu ứng viên & kết quả trích xuất CV
import jakarta.persistence.*;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "age_of_experience")
    private Integer ageOfExperience;

    @Column(name = "position_applied", length = 50)
    private String positionApplied;

    @Column(name = "cv_text", columnDefinition = "TEXT")
    private String cvText;

    @Column(name = "ai_score")
    private Integer aiScore = 0;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(length = 30)
    private String status = "PENDING_AI"; // PENDING_AI, WAITING_APPROVAL, PASSED, REJECTED

    public Candidate() {
    }

    public Candidate(Long id, String fullName, String email, String phone, Integer ageOfExperience,
                     String positionApplied, String cvText, Integer aiScore, String aiFeedback, String status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.ageOfExperience = ageOfExperience;
        this.positionApplied = positionApplied;
        this.cvText = cvText;
        this.aiScore = aiScore;
        this.aiFeedback = aiFeedback;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getAgeOfExperience() {
        return ageOfExperience;
    }

    public void setAgeOfExperience(Integer ageOfExperience) {
        this.ageOfExperience = ageOfExperience;
    }

    public String getPositionApplied() {
        return positionApplied;
    }

    public void setPositionApplied(String positionApplied) {
        this.positionApplied = positionApplied;
    }

    public String getCvText() {
        return cvText;
    }

    public void setCvText(String cvText) {
        this.cvText = cvText;
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