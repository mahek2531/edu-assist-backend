package com.project.Edu.Assist.Repository;

import com.project.Edu.Assist.Entity.Doubt;
import com.project.Edu.Assist.Entity.DoubtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoubtRepository extends JpaRepository<Doubt, Long> {

    List<Doubt> findByAsker_Id(Long id);
    List<Doubt> findByAsker_IdOrderByDoubtRaisedAtDesc(Long id);

    List<Doubt> findByAsker_IdAndStatus(Long askerId, DoubtStatus status);
    List<Doubt> findByAsker_IdAndStatusOrderByDoubtRaisedAtDesc(Long askerId, DoubtStatus status);

    List<Doubt> findBySolverIdAndStatus(Long solverId, DoubtStatus status);
    List<Doubt> findBySolverIdAndStatusOrderByDoubtRaisedAtDesc(Long solverId, DoubtStatus status);

    List<Doubt> findBySolver_Id(Long seniorId);
    List<Doubt> findBySolver_IdOrderByDoubtRaisedAtDesc(Long seniorId);

    List<Doubt> findByIsVerifiedTrue();
    List<Doubt> findByIsVerifiedTrueOrderByDoubtRaisedAtDesc();

    List<Doubt> findByIsVerifiedTrueAndStatus(DoubtStatus status);
    List<Doubt> findByIsVerifiedTrueAndStatusOrderByDoubtRaisedAtDesc(DoubtStatus status);
    List<Doubt> findByIsVerifiedTrueAndStatusAndSubjectIgnoreCaseOrderByDoubtRaisedAtDesc(DoubtStatus status, String subject);

    List<Doubt> findByStatus(DoubtStatus status);
    List<Doubt> findByStatusOrderByDoubtRaisedAtDesc(DoubtStatus status);

    List<Doubt> findBySolverId(Long seniorId);
    List<Doubt> findBySolverIdOrderByDoubtRaisedAtDesc(Long seniorId);

    List<Doubt> findAllByOrderByDoubtRaisedAtDesc();
}