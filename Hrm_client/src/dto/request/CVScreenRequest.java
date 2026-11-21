package dto.request;

public class CVScreenRequest {

    private String fullName;
    private String email;
    private String phone;
    private String positionApplied;
    private Integer yearsOfExperience;
    private String cvText;
    private Double passingScore;

    public CVScreenRequest() {
    }

    public CVScreenRequest(String fullName, String email, String phone, String positionApplied, Integer yearsOfExperience, String cvText, Double passingScore) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.positionApplied = positionApplied;
        this.yearsOfExperience = yearsOfExperience;
        this.cvText = cvText;
        this.passingScore = passingScore;
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

    public String getCvText() {
        return cvText;
    }

    public void setCvText(String cvText) {
        this.cvText = cvText;
    }
    public Double getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(Double passingScore) {
        this.passingScore = passingScore;
    }
}