//Thêm, sửa, xóa, quản lý số lượng nhân sự
package service;

import entity.Candidate;
import entity.Employee;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.CandidateRepository;
import repository.EmployeeRepository;
import repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final FaceRecognitionService faceRecognitionService;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           CandidateRepository candidateRepository,
                           UserRepository userRepository,
                           FaceRecognitionService faceRecognitionService) {
        this.employeeRepository = employeeRepository;
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.faceRecognitionService = faceRecognitionService;
    }

    /**
     * 1. Tuyển dụng ứng viên trúng tuyển -> Tạo hồ sơ Employee & Nâng cấp Role User
     */
    @Transactional
    public Employee hireCandidateToEmployee(Long candidateId, Double baseSalary, Integer departmentId, Long managerId) {
        // Kiểm tra ứng viên đã được tuyển trước đó chưa (Repo method 3: findByCandidateId)
        Optional<Employee> existingEmp = employeeRepository.findByCandidateId(candidateId);
        if (existingEmp.isPresent()) {
            throw new RuntimeException("Ứng viên này đã có hồ sơ nhân viên chính thức!");
        }

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ứng viên có ID: " + candidateId));

        // Cập nhật trạng thái Candidate
        candidate.setStatus("HIRED");
        candidateRepository.save(candidate);

        // Khởi tạo hồ sơ Employee mới
        Employee employee = new Employee();
        employee.setCandidate(candidate);
        employee.setFullName(candidate.getFullName());
        employee.setEmail(candidate.getEmail());
        employee.setPhone(candidate.getPhone());
        employee.setPosition(candidate.getPositionApplied());
        employee.setYearsOfExperience(candidate.getYearsOfExperience());
        employee.setBaseSalary(baseSalary != null ? baseSalary : 0.0);
        employee.setDepartmentId(departmentId);

        // Gán Manager quản lý trực tiếp (nếu có)
        if (managerId != null) {
            employeeRepository.findById(managerId).ifPresent(employee::setManager);
        }

        // Nâng cấp quyền tài khoản User từ CANDIDATE -> EMPLOYEE
        if (candidate.getEmail() != null) {
            Optional<User> userOpt = userRepository.findByUsername(candidate.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setRole("EMPLOYEE");
                userRepository.save(user);
                employee.setUser(user);
            }
        }

        return employeeRepository.save(employee);
    }

    /**
     * 2. Lấy danh sách toàn bộ nhân viên
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * 3. Lấy chi tiết nhân viên theo Employee ID
     */
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên có ID: " + id));
    }

    /**
     * 4. Tìm nhân viên theo User ID (Repo method 1: findByUserId)
     */
    public Optional<Employee> getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserId(userId);
    }

    /**
     * 5. Tìm nhân viên theo Face ID phục vụ chấm công khuôn mặt (Repo method 2: findByFaceId)
     */
    public Optional<Employee> getEmployeeByFaceId(String faceId) {
        return employeeRepository.findByFaceId(faceId);
    }

    /**
     * 6. Tìm nhân viên theo Candidate ID gốc (Repo method 3: findByCandidateId)
     */
    public Optional<Employee> getEmployeeByCandidateId(Long candidateId) {
        return employeeRepository.findByCandidateId(candidateId);
    }

    /**
     * 7. Lấy danh sách nhân viên thuộc quyền quản lý của 1 Manager (Repo method 4: findByManagerId)
     */
    public List<Employee> getEmployeesByManagerId(Long managerId) {
        return employeeRepository.findByManagerId(managerId);
    }

    /**
     * 8. Tìm nhân viên theo Email (Repo method 5: findByEmail)
     */
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }


    /**
     * Nhân viên/HR chụp ảnh từ Webcam JavaFX -> Trích xuất mã Face ID và lưu vào DB
     */
    public Employee registerFaceId(Long employeeId, String imageBase64) {
        Employee employee = getEmployeeById(employeeId);

        // Chuyển ảnh Base64 thành chuỗi đặc trưng Face ID trước khi lưu
        String extractedFaceId = faceRecognitionService.extractFaceFeature(imageBase64);
        if (extractedFaceId == null) {
            throw new RuntimeException("Dữ liệu ảnh khuôn mặt không hợp lệ!");
        }

        employee.setFaceId(extractedFaceId);
        return employeeRepository.save(employee);
    }

    /**
     * 10. Cập nhật thông tin hồ sơ nhân viên (Lương, chức vụ, phòng ban, manager)
     */
    public Employee updateEmployee(Long id, Employee updatedInfo) {
        Employee existing = getEmployeeById(id);

        if (updatedInfo.getFullName() != null) existing.setFullName(updatedInfo.getFullName());
        if (updatedInfo.getPhone() != null) existing.setPhone(updatedInfo.getPhone());
        if (updatedInfo.getEmail() != null) existing.setEmail(updatedInfo.getEmail());
        if (updatedInfo.getPosition() != null) existing.setPosition(updatedInfo.getPosition());
        if (updatedInfo.getBaseSalary() != null) existing.setBaseSalary(updatedInfo.getBaseSalary());
        if (updatedInfo.getDepartmentId() != null) existing.setDepartmentId(updatedInfo.getDepartmentId());
        if (updatedInfo.getYearsOfExperience() != null) existing.setYearsOfExperience(updatedInfo.getYearsOfExperience());
        if (updatedInfo.getManager() != null) existing.setManager(updatedInfo.getManager());

        return employeeRepository.save(existing);
    }

    /**
     * 11. Xóa nhân viên
     */
    public boolean deleteEmployee(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
