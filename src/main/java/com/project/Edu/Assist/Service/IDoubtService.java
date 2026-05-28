package com.project.Edu.Assist.Service;

import com.project.Edu.Assist.DTO.*;
import com.project.Edu.Assist.Entity.Doubt;
import jakarta.transaction.Transactional;

import java.util.List;

public interface IDoubtService {

    Doubt verifyDoubt(Long doubtId);

    List<DoubtsDTO> getPostedDoubts();

    DoubtResponseDTO acceptDoubt(Long doubtId, Long seniorId);

    @Transactional
    Doubt submitSolution(Long doubtId, Long seniorId, SubmitSolutionDTO dto, boolean closeChat);

    Doubt addCommentsAndRatingToSolvedDoubt(Long doubtId, Long juniorId, String comments, double rating);

    List<Doubt> getMyDoubts(Long id);

    List<Doubt> getPendingDoubts(Long id);

    List<Long> getSolvedDoubtIdsByStudentId(Long studentId);

    Doubt getSolutionForMyDoubt(Long studentId, Long doubtId);

    List<VerifiedDoubtDTO> getVerifiedDoubts();

    Doubt readyToSolveDoubt(Long doubtId, Long collegeId);

    List<Doubt> getPostedSolutionsByCollegeId(Long collegeId);

    List<LeaderBoardDTO> getLeaderboard();

    List<Doubt> getRemarksBySeniorId(Long seniorId);

    Doubt rejectDoubt(Long doubtId);

    List<Doubt> approveAllPendingDoubts();

    Doubt closeChat(Long doubtId, Long userId, String role);
}