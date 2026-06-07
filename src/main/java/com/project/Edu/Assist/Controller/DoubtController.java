package com.project.Edu.Assist.Controller;

import com.project.Edu.Assist.DTO.DoubtDTO;
import com.project.Edu.Assist.DTO.DoubtResponseDTO;
import com.project.Edu.Assist.DTO.DoubtsDTO;
import com.project.Edu.Assist.DTO.RemarksDTO;
import com.project.Edu.Assist.DTO.SubmitSolutionDTO;
import com.project.Edu.Assist.Entity.Doubt;
import com.project.Edu.Assist.Entity.JuniorStudent;
import com.project.Edu.Assist.Repository.JuniorStudentRepository;
import com.project.Edu.Assist.Service.imp.DoubtService;
import com.project.Edu.Assist.Service.imp.JuniorStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doubt")
public class DoubtController {

    @Autowired
    private DoubtService doubtService;

    @Autowired
    private JuniorStudentRepository juniorStudentRepository;

    @Autowired
    private JuniorStudentService juniorStudentService;

    @Autowired
    private com.project.Edu.Assist.Service.CloudinaryService cloudinaryService;

    @PostMapping("/post/{student_id}")
    public ResponseEntity<?> postDoubt(@PathVariable Long student_id,
                                       @ModelAttribute DoubtDTO studentDoubtDTO) {

        HashMap<String, Object> response = new HashMap<>();

        try {
            JuniorStudent student = juniorStudentRepository.findById(student_id)
                    .orElseThrow(() -> new RuntimeException("student id not found."));

            String image = null;
            MultipartFile doubtPic = studentDoubtDTO.getDoubtPic();

            if (doubtPic != null && !doubtPic.isEmpty()) {
                image = cloudinaryService.uploadFile(doubtPic);
            }

            Doubt doubt = new Doubt();
            doubt.setAsker(student);
            doubt.setDoubtRaisedAt(LocalDateTime.now());
            doubt.setDoubtPic(image);
            doubt.setDescription(studentDoubtDTO.getDescription());
            doubt.setSubject(studentDoubtDTO.getSubject());

            Doubt savedDoubt = juniorStudentService.postDoubt(doubt, student_id);

            Long currentCount = student.getDoubtCount() == null ? 0L : student.getDoubtCount();
            student.setDoubtCount(currentCount + 1);
            juniorStudentRepository.save(student);

            response.put("status", true);
            response.put("doubt", savedDoubt);
            response.put("message", "Student posted doubt successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/posted")
    public ResponseEntity<?> getPostedDoubts() {
        HashMap<String, Object> response = new HashMap<>();

        try {
            List<DoubtsDTO> doubts = doubtService.getPostedDoubts();
            response.put("status", true);
            response.put("Doubts", doubts);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to fetch doubts.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify/{doubtId}")
    public ResponseEntity<?> verifyDoubt(@PathVariable Long doubtId) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            Doubt updatedDoubt = doubtService.verifyDoubt(doubtId);
            response.put("status", true);
            response.put("doubt", updatedDoubt);
            response.put("message", "Doubt verified successfully.");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/approve-all")
    public ResponseEntity<?> approveAllPendingDoubts() {
        HashMap<String, Object> response = new HashMap<>();

        try {
            List<Doubt> approvedDoubts = doubtService.approveAllPendingDoubts();
            response.put("status", true);
            response.put("doubts", approvedDoubts);
            response.put("message", "All pending doubts approved successfully.");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reject/{doubtId}")
    public ResponseEntity<?> rejectDoubt(@PathVariable Long doubtId) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            Doubt updatedDoubt = doubtService.rejectDoubt(doubtId);
            response.put("status", true);
            response.put("doubt", updatedDoubt);
            response.put("message", "Doubt rejected successfully.");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/accept/{doubtId}/{seniorId}")
    public ResponseEntity<?> acceptDoubt(@PathVariable Long doubtId,
                                         @PathVariable Long seniorId) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            DoubtResponseDTO updatedDoubt = doubtService.acceptDoubt(doubtId, seniorId);
            response.put("status", true);
            response.put("doubt", updatedDoubt);
            response.put("message", "You have accepted the doubt. Chat is now open.");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/submit-solution/{doubtId}/{seniorId}")
    public ResponseEntity<?> submitSolution(@PathVariable Long doubtId,
                                            @PathVariable Long seniorId,
                                            @RequestParam(defaultValue = "false") boolean closeChat,
                                            @ModelAttribute SubmitSolutionDTO dto) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            Doubt savedDoubt = doubtService.submitSolution(doubtId, seniorId, dto, closeChat);
            response.put("status", true);
            response.put("doubt", savedDoubt);
            response.put("message", closeChat
                    ? "Solution submitted successfully and chat closed."
                    : "Solution submitted successfully.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/close-chat/{doubtId}/{userId}/{role}")
    public ResponseEntity<?> closeChat(@PathVariable Long doubtId,
                                       @PathVariable Long userId,
                                       @PathVariable String role) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            Doubt updated = doubtService.closeChat(doubtId, userId, role);
            response.put("status", true);
            response.put("doubt", updated);
            response.put("message", "Chat closed successfully.");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/remarks/{doubtId}/{juniorId}")
    public ResponseEntity<?> addCommentsAndRating(@PathVariable Long doubtId,
                                                  @PathVariable Long juniorId,
                                                  @RequestBody RemarksDTO dto) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            Doubt updatedDoubt = doubtService.addCommentsAndRatingToSolvedDoubt(
                    doubtId, juniorId, dto.getComments(), dto.getRating());

            response.put("success", true);
            response.put("message", "Comments and rating added successfully!");
            response.put("studentId", juniorId);
            response.put("doubt", updatedDoubt);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}