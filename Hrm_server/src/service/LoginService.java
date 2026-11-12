package service;

import dto.request.LoginRequest;
import dto.request.LogoutRequest;
import dto.request.RegisterRequest;
import dto.response.AuthResponse;
import entity.Candidate;
import entity.Employee;
import entity.User;
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

        // Nếu không truyền username riêng thì lấy luôn email làm username
        String username = (request.getUsername() != null && !request.getUsername().isBlank())
                ? request.getUsername().trim()
                : request.getEmail().trim();

        if (userRepository.findByUsername(username).isPresent()) {
            response.setSuccess(false);
            response.setMessage("Tên đăng nhập hoặc Email đã được sử dụng!");
            return response;
        }

        if (request.getEmail() != null && candidateRepository.findByEmail(request.getEmail()).isPresent()) {
            response.setSuccess(false);
            response.setMessage("Email này đã tồn tại trong hồ sơ ứng viên!");
            return response;
        }

        // Tạo User account
        User user = new User();
        user.setUsername(username);
        user.setPassword(request.getPassword());
        user.setRole("CANDIDATE");
        User savedUser = userRepository.save(user);

        // Tạo Hồ sơ Candidate
        Candidate candidate = new Candidate();
        candidate.setFullName(request.getFullName());
        candidate.setEmail(request.getEmail() != null ? request.getEmail().trim() : username);
        candidate.setPhone(request.getPhone());
        candidate.setPositionApplied(request.getPositionApplied());
        candidate.setYearsOfExperience(request.getYearsOfExperience());
        candidate.setCvText(request.getCvText());
        candidate.setStatus("PENDING");
        Candidate savedCandidate = candidateRepository.save(candidate);

        response.setSuccess(true);
        response.setUserId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setRole(savedUser.getRole());
        response.setFullName(savedCandidate.getFullName());
        // Nếu AuthResponse của bạn đã thêm trường candidateId thì mở comment dòng dưới:
        // response.setCandidateId(savedCandidate.getId());
        response.setMessage("Đăng ký tài khoản ứng viên thành công!");

        return response;
    }

    /**
     * 2. Xử lý Đăng nhập hệ thống (Dùng cho CANDIDATE, EMPLOYEE, MANAGER, HR, ADMIN)
     */
    public AuthResponse login(LoginRequest request) {
        AuthResponse response = new AuthResponse();

        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
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

        // Trường hợp 1: Đã là Employee chính thức
        Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            employeeId = emp.getId();
            fullName = emp.getFullName();
        } else if ("CANDIDATE".equalsIgnoreCase(user.getRole())) {
            // Trường hợp 2: Đang là Ứng viên -> Lấy họ tên thật từ bảng Candidate
            Optional<Candidate> candOpt = candidateRepository.findByEmail(user.getUsername());
            if (candOpt.isPresent()) {
                fullName = candOpt.get().getFullName();
                // response.setCandidateId(candOpt.get().getId());
            }
        }

        response.setSuccess(true);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
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