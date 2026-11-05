//Danh mục và trạng thái hoàn thành KPI
package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kpi_evaluations")
public class KPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "month_year", nullable = false, length = 7)
    private String monthYear; // VD: "2026-11"

    @Column(name = "target_kpi", nullable = false)
    private Double targetKpi;

    @Column(name = "achieved_kpi", nullable = false)
    private Double achievedKpi;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(name = "manager_comments", columnDefinition = "TEXT")
    private String managerComments;

    @Column(name = "bonus_amount")
    private Double bonusAmount = 0.0;

    public KPI() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public Double getTargetKpi() {
        return targetKpi;
    }

    public void setTargetKpi(Double targetKpi) {
        this.targetKpi = targetKpi;
    }

    public Double getAchievedKpi() {
        return achievedKpi;
    }

    public void setAchievedKpi(Double achievedKpi) {
        this.achievedKpi = achievedKpi;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public String getManagerComments() {
        return managerComments;
    }

    public void setManagerComments(String managerComments) {
        this.managerComments = managerComments;
    }

    public Double getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(Double bonusAmount) {
        this.bonusAmount = bonusAmount;
    }
}