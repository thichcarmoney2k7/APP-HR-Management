package service;

import dto.request.LoginRequest;
import dto.request.LogoutRequest;
import dto.request.RegisterRequest;
import dto.response.AuthResponse;
import entity.Candidate;
import entity.Employee;
import entity.User;
import enums.CandidateStatus;
import enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.CandidateRepository;
import repository.EmployeeRepository;
import repository.UserRepository;

import java.util.Optional;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CandidateRepository candidateRepository;

    @Autowired
    public LoginService(UserRepository userRepository,
                        EmployeeRepository employeeRepository,
                        CandidateRepository candidateRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.candidateRepository = candidateRepository;
    }

    /**
     * 1. Ứng viên tự đăng ký tài khoản tuyển dụng (Role: CANDIDATE)
     */
    @Transactional
    public AuthResponse registerCandidate(RegisterRequest request) {
        AuthResponse response = new AuthResponse();

        String email = (request.getEmail() != null && !request.getEmail().isBlank())
                ? request.getEmail().trim()
                : null;

        // Dùng email làm username để đồng bộ 100% với bước nâng quyền lên Employee sau này
        String username = (email != null)
                ? email
                : (request.getUsername() != null ? request.getUsername().trim() : "");

        if (username.isBlank()) {
            response.setSuccess(false);
            response.setMessage("Vui lòng nhập Email để đăng ký tài khoản!");
            return response;
        }

        if (userRepository.findByUsername(username).isPresent()) {
            response.setSuccess(false);
            response.setMessage("Email hoặc tên đăng nhập đã được sử dụng!");
            return response;
        }

        if (email != null && candidateRepository.findByEmail(email).isPresent()) {
            response.setSuccess(false);
            response.setMessage("Email này đã tồn tại trong hồ sơ ứng viên!");
            return response;
        }

        // 1. Tạo tài khoản User với vai trò mặc định là CANDIDATE
        User user = new User();
        user.setUsername(username);
        user.setPassword(request.getPassword());
        user.setRole(Role.CANDIDATE);
        User savedUser = userRepository.save(user);

        // 2. Tạo hồ sơ Candidate ở trạng thái chờ chấm/duyệt (PENDING)
        Candidate candidate = new Candidate();
        candidate.setFullName(request.getFullName());
        candidate.setEmail(email != null ? email : username);
        candidate.setPhone(request.getPhone());
        candidate.setPositionApplied(request.getPositionApplied());
        candidate.setYearsOfExperience(request.getYearsOfExperience());
        candidate.setCvText(request.getCvText());
        candidate.setStatus(CandidateStatus.PENDING);
        Candidate savedCandidate = candidateRepository.save(candidate);

        // 3. Trả về Response (chuyển Enum sang String bằng .name())
        response.setSuccess(true);
        response.setUserId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setRole(savedUser.getRole().name());
        response.setFullName(savedCandidate.getFullName());
        response.setMessage("Đăng ký tài khoản ứng viên thành công!");

        return response;
    }

    /**
     * 2. Xử lý Đăng nhập hệ thống (Dùng cho CANDIDATE, EMPLOYEE, MANAGER, HR, ADMIN)
     */
    public AuthResponse login(LoginRequest request) {
        AuthResponse response = new AuthResponse();

        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            response.setSuccess(false);
            response.setMessage("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return response;
        }

        String username = request.getUsername().trim();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Tài khoản không tồn tại!");
            return response;
        }

        User user = userOpt.get();

        if (!user.getPassword().equals(request.getPassword())) {
            response.setSuccess(false);
            response.setMessage("Mật khẩu không chính xác!");
            return response;
        }

        Long employeeId = null;
        String fullName = user.getUsername();

        // Trường hợp 1: Đã có hồ sơ Employee chính thức (EMPLOYEE, MANAGER, HR)
        Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            employeeId = emp.getId();
            fullName = emp.getFullName();
        } else if (user.getRole() == Role.CANDIDATE) {
            // Trường hợp 2: Đang là Ứng viên -> Lấy họ tên thật từ bảng Candidate
            Optional<Candidate> candOpt = candidateRepository.findByEmail(user.getUsername());
            if (candOpt.isPresent()) {
                fullName = candOpt.get().getFullName();
                // Nếu sau này AuthResponse có thêm candidateId thì mở comment dòng dưới:
                // response.setCandidateId(candOpt.get().getId());
            }
        }

        response.setSuccess(true);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole() != null ? user.getRole().name() : null);
        response.setEmployeeId(employeeId);
        response.setFullName(fullName);
        response.setMessage("Đăng nhập thành công!");

        return response;
    }
    /**
     * 3. Đăng xuất
     */
    public AuthResponse logout(LogoutRequest request) {
        AuthResponse response = new AuthResponse();
        response.setSuccess(true);
        response.setMessage("Đăng xuất thành công!");
        return response;
    }
}