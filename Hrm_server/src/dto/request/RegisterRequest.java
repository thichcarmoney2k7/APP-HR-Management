package dto.request;

public class RegisterRequest {

    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String positionApplied;
    private Integer yearsOfExperience;
    private String cvText;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password, String fullName, String email,
                           String phone, String positionApplied, Integer yearsOfExperience, String cvText) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.positionApplied = positionApplied;
        this.yearsOfExperience = yearsOfExperience;
        this.cvText = cvText;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPositionApplied() { return positionApplied; }
    public void setPositionApplied(String positionApplied) { this.positionApplied = positionApplied; }

    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getCvText() { return cvText; }
    public void setCvText(String cvText) { this.cvText = cvText; }
}