package repository;
import entity.Candidate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByEmail(String email);

    // 2. Lấy danh sách ứng viên theo vị trí ứng tuyển
    List<Candidate> findByPositionApplied(String positionApplied);

    // 3. Lọc ứng viên theo trạng thái (PENDING, APPROVED, REJECTED)
    List<Candidate> findByStatus(String status);

    // 4. Lọc danh sách ứng viên có điểm AI đánh giá cao (sắp xếp giảm dần)
    List<Candidate> findByAiScoreGreaterThanEqualOrderByAiScoreDesc(Double minScore);

}