package dto.response;

public class EmployeeResponse {

    private Long id;
    private String fullName;
    private Integer yearsOfExperience;
    private Double baseSalary;
    private String faceId;
    private Integer departmentId;
    private String email;
    private String phone;
    private String position;
    private EmployeeResponse manager;

    public EmployeeResponse() {
    }

    public EmployeeResponse(Long id, String fullName, Integer yearsOfExperience, Double baseSalary,
                            String faceId, Integer departmentId, String email, String phone,
                            String position, EmployeeResponse manager) {
        this.id = id;
        this.fullName = fullName;
        this.yearsOfExperience = yearsOfExperience;
        this.baseSalary = baseSalary;
        this.faceId = faceId;
        this.departmentId = departmentId;
        this.email = email;
        this.phone = phone;
        this.position = position;
        this.manager = manager;
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

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public Double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(Double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public EmployeeResponse getManager() {
        return manager;
    }

    public void setManager(EmployeeResponse manager) {
        this.manager = manager;
    }

    @Override
    public String toString() {
        return fullName + (position != null && !position.isBlank() ? " - " + position : "");
    }
}