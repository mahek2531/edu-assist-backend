package com.project.Edu.Assist.Service.imp;

import com.project.Edu.Assist.DTO.*;
import com.project.Edu.Assist.Entity.*;
import com.project.Edu.Assist.Repository.DoubtRepository;
import com.project.Edu.Assist.Repository.JuniorStudentRepository;
import com.project.Edu.Assist.Repository.SeniorStudentRepository;
import com.project.Edu.Assist.Service.IDoubtService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoubtService implements IDoubtService {

    @Autowired
    private DoubtRepository doubtRepository;

    @Autowired
    private SeniorStudentRepository seniorStudentRepository;

    @Autowired
    private JuniorStudentRepository juniorStudentRepository;

    @Override
    public Doubt verifyDoubt(Long doubtId) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new RuntimeException("Doubt not found"));

        if (doubt.isVerified() && doubt.getStatus() == DoubtStatus.PENDING) {
            doubt.setStatus(DoubtStatus.APPROVED);
            return doubtRepository.save(doubt);
        }

        if (doubt.isVerified() && doubt.getStatus() == DoubtStatus.APPROVED) {
            return doubt;
        }

        if (doubt.getStatus() != DoubtStatus.PENDING) {
            throw new RuntimeException("Only PENDING doubts can be verified.");
        }

        doubt.setVerified(true);
        doubt.setStatus(DoubtStatus.APPROVED);
        return doubtRepository.save(doubt);
    }

    @Override
    public List<DoubtsDTO> getPostedDoubts() {
        List<Doubt> doubts = doubtRepository.findAllByOrderByDoubtRaisedAtDesc();

        return doubts.stream()
                .map(doubt -> new DoubtsDTO(
                        doubt.getId(),
                        doubt.getSubject(),
                        doubt.getDescription(),
                        doubt.getDoubtPic(),
                        doubt.getAsker() != null ? doubt.getAsker().getEmail() : null,
                        doubt.getDoubtRaisedAt(),
                        doubt.getStatus()
                ))
                .toList();
    }

    @Transactional
    @Override
    public DoubtResponseDTO acceptDoubt(Long doubtId, Long seniorId) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new RuntimeException("Doubt not found"));

        if (!doubt.isVerified()) {
            throw new RuntimeException("This doubt is not verified yet by admin.");
        }

        if (doubt.getStatus() != DoubtStatus.APPROVED) {
            throw new RuntimeException("This doubt is no longer available.");
        }

        if (doubt.getSolver() != null) {
            throw new RuntimeException("This doubt has already been accepted by another student.");
        }

        SeniorStudent senior = seniorStudentRepository.findById(seniorId)
                .orElseThrow(() -> new RuntimeException("Senior student not found"));

        doubt.setSolver(senior);
        doubt.setStatus(DoubtStatus.SOLVING);
        doubt.setChatEnabled(true);
        doubt.setChatClosed(false);
        doubt.setChatClosedAt(null);

        Doubt saved = doubtRepository.save(doubt);

        return DoubtResponseDTO.builder()
                .id(saved.getId())
                .studentId(saved.getAsker() != null ? saved.getAsker().getId() : null)
                .studentName(saved.getAsker() != null ? saved.getAsker().getName() : null)
                .subject(saved.getSubject())
                .doubtPic(saved.getDoubtPic())
                .description(saved.getDescription())
                .doubtRaisedAt(saved.getDoubtRaisedAt())
                .status(saved.getStatus())
                .solverId(senior.getId())
                .solverName(senior.getName())
                .isVerified(saved.isVerified())
                .build();
    }

    @Transactional
    @Override
    public Doubt submitSolution(Long doubtId, Long seniorId, SubmitSolutionDTO dto, boolean closeChat) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new RuntimeException("Doubt not found"));

        SeniorStudent senior = seniorStudentRepository.findById(seniorId)
                .orElseThrow(() -> new RuntimeException("Student ID not found."));

        if (doubt.getSolver() == null || !doubt.getSolver().getId().equals(seniorId)) {
            throw new RuntimeException("You are not assigned to this doubt.");
        }

        if (doubt.getStatus() != DoubtStatus.SOLVING) {
            throw new RuntimeException("Cannot submit solution. Current status: " + doubt.getStatus());
        }

        if (dto.getSolutionText() == null || dto.getSolutionText().trim().isEmpty()) {
            throw new RuntimeException("Solution text is required.");
        }

        doubt.setSolutionText(dto.getSolutionText().trim());
        doubt.setSolutionSubmittedAt(LocalDateTime.now());

        MultipartFile solutionPic = dto.getSolutionPic();
        if (solutionPic != null && !solutionPic.isEmpty()) {
            String savedSolutionPic = saveOptimizedFile(solutionPic);
            doubt.setSolutionPic(savedSolutionPic);
        }

        doubt.setStatus(DoubtStatus.SOLVED);

        if (closeChat) {
            doubt.setChatClosed(true);
            doubt.setChatClosedAt(LocalDateTime.now());
        }

        if (senior.getSolvedCount() == null) {
            senior.setSolvedCount(0L);
        }

        senior.setSolvedCount(senior.getSolvedCount() + 1);
        seniorStudentRepository.save(senior);

        return doubtRepository.save(doubt);
    }

    @Override
    public Doubt addCommentsAndRatingToSolvedDoubt(Long doubtId, Long juniorId, String comments, double rating) {
        juniorStudentRepository.findById(juniorId)
                .orElseThrow(() -> new RuntimeException("Student id not found"));

        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new RuntimeException("Doubt not found"));

        if (doubt.getStatus() != DoubtStatus.SOLVED) {
            throw new RuntimeException("This doubt is not solved yet.");
        }

        if (doubt.getAsker() == null || !doubt.getAsker().getId().equals(juniorId)) {
            throw new RuntimeException("You are not the student who posted this doubt, so you cannot add comments or rating.");
        }

        doubt.setComments(comments);
        doubt.setRating(rating);

        return doubtRepository.save(doubt);
    }

    @Override
    public List<Doubt> getMyDoubts(Long id) {
        return doubtRepository.findByAsker_IdOrderByDoubtRaisedAtDesc(id);
    }

    @Override
    public List<Doubt> getPendingDoubts(Long id) {
        return doubtRepository.findByAsker_IdAndStatusOrderByDoubtRaisedAtDesc(id, DoubtStatus.PENDING);
    }

    @Override
    public List<Long> getSolvedDoubtIdsByStudentId(Long studentId) {
        return doubtRepository.findByAsker_IdAndStatusOrderByDoubtRaisedAtDesc(studentId, DoubtStatus.SOLVED)
                .stream()
                .map(Doubt::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Doubt getSolutionForMyDoubt(Long studentId, Long doubtId) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new RuntimeException("Doubt not found"));

        if (doubt.getAsker() == null || !doubt.getAsker().getId().equals(studentId)) {
            throw new RuntimeException("You are not allowed to view this solution.");
        }

        if (doubt.getStatus() != DoubtStatus.SOLVED) {
            throw new RuntimeException("Solution is not available yet.");
        }

        return doubt;
    }

    @Override
    public List<VerifiedDoubtDTO> getVerifiedDoubts() {
        List<Doubt> availableDoubts = doubtRepository.findByIsVerifiedTrueAndStatusOrderByDoubtRaisedAtDesc(DoubtStatus.APPROVED);
        return mapToVerifiedDoubtDTOList(availableDoubts);
    }

    public List<VerifiedDoubtDTO> getVerifiedDoubtsForSenior(Long seniorId) {
        SeniorStudent senior = seniorStudentRepository.findById(seniorId)
                .orElseThrow(() -> new RuntimeException("Senior student not found"));

        Set<String> expertiseSubjects = parseSubjects(senior.getExpertiseSubjects());

        if (expertiseSubjects.isEmpty()) {
            return new ArrayList<>();
        }

        List<Doubt> availableDoubts = doubtRepository.findByIsVerifiedTrueAndStatusOrderByDoubtRaisedAtDesc(DoubtStatus.APPROVED);

        List<Doubt> matchedDoubts = availableDoubts.stream()
                .filter(doubt -> expertiseSubjects.contains(normalizeSubject(doubt.getSubject())))
                .collect(Collectors.toList());

        return mapToVerifiedDoubtDTOList(matchedDoubts);
    }

    public List<VerifiedDoubtDTO> getOpenVerifiedDoubts() {
        List<Doubt> availableDoubts = doubtRepository.findByIsVerifiedTrueAndStatusOrderByDoubtRaisedAtDesc(DoubtStatus.APPROVED);
        List<SeniorStudent> seniors = seniorStudentRepository.findAll();

        List<Doubt> openDoubts = availableDoubts.stream()
                .filter(doubt -> !hasMatchingSeniorForSubject(doubt.getSubject(), seniors))
                .collect(Collectors.toList());

        return mapToVerifiedDoubtDTOList(openDoubts);
    }

    private List<VerifiedDoubtDTO> mapToVerifiedDoubtDTOList(List<Doubt> doubts) {
        List<VerifiedDoubtDTO> result = new ArrayList<>();

        for (Doubt doubt : doubts) {
            VerifiedDoubtDTO dto = new VerifiedDoubtDTO();
            dto.setId(doubt.getId());
            dto.setSubject(doubt.getSubject());
            dto.setDescription(doubt.getDescription());
            dto.setDoubtPic(doubt.getDoubtPic());
            dto.setStudentName(doubt.getAsker() != null ? doubt.getAsker().getName() : null);
            dto.setStudentEmail(doubt.getAsker() != null ? doubt.getAsker().getEmail() : null);
            dto.setDoubtRaisedAt(doubt.getDoubtRaisedAt());
            dto.setStatus(doubt.getStatus());
            result.add(dto);
        }

        return result;
    }

    private boolean hasMatchingSeniorForSubject(String subject, List<SeniorStudent> seniors) {
        String normalizedSubject = normalizeSubject(subject);

        if (normalizedSubject.isBlank()) {
            return false;
        }

        for (SeniorStudent senior : seniors) {
            if (parseSubjects(senior.getExpertiseSubjects()).contains(normalizedSubject)) {
                return true;
            }
        }

        return false;
    }

    private Set<String> parseSubjects(String subjects) {
        if (subjects == null || subjects.trim().isBlank()) {
            return new HashSet<>();
        }

        return Arrays.stream(subjects.split(","))
                .map(this::normalizeSubject)
                .filter(subject -> !subject.isBlank())
                .collect(Collectors.toSet());
    }

    private String normalizeSubject(String subject) {
        return subject == null ? "" : subject.trim().toLowerCase();
    }

    @Override
    public Doubt readyToSolveDoubt(Long doubtId, Long collegeId) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new RuntimeException("Doubt not found"));

        if (!doubt.isVerified()) {
            throw new RuntimeException("This doubt is not verified yet.");
        }

        if (doubt.getStatus() != DoubtStatus.APPROVED) {
            throw new RuntimeException("This doubt is no longer available.");
        }

        SeniorStudent solver = seniorStudentRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("College student not found with ID: " + collegeId));

        doubt.setSolver(solver);
        doubt.setStatus(DoubtStatus.SOLVING);
        doubt.setChatEnabled(true);
        doubt.setChatClosed(false);
        doubt.setChatClosedAt(null);

        return doubtRepository.save(doubt);
    }

    @Override
    public List<Doubt> getPostedSolutionsByCollegeId(Long solverId) {
        return doubtRepository.findBySolverIdAndStatusOrderByDoubtRaisedAtDesc(solverId, DoubtStatus.SOLVED);
    }

    @Override
    public List<LeaderBoardDTO> getLeaderboard() {
        List<Doubt> solvedDoubts = doubtRepository.findByStatusOrderByDoubtRaisedAtDesc(DoubtStatus.SOLVED);

        if (solvedDoubts == null || solvedDoubts.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, LeaderBoardDTO> leaderboardMap = new LinkedHashMap<>();

        for (Doubt doubt : solvedDoubts) {
            SeniorStudent solver = doubt.getSolver();
            if (solver == null) continue;

            Long seniorId = solver.getId();

            LeaderBoardDTO entry = leaderboardMap.computeIfAbsent(seniorId, id -> {
                LeaderBoardDTO leaderboardDTO = new LeaderBoardDTO();
                leaderboardDTO.setSeniorId(solver.getId());
                leaderboardDTO.setSeniorName(solver.getName());
                leaderboardDTO.setSeniorEmail(solver.getEmail());
                leaderboardDTO.setPhoto(solver.getPhoto());
                leaderboardDTO.setDoubtsSolved(0);
                leaderboardDTO.setTotalPoints(0);
                return leaderboardDTO;
            });

            entry.setDoubtsSolved(entry.getDoubtsSolved() + 1);

            Double rating = doubt.getRating();
            if (rating != null && rating >= 1.0 && rating <= 5.0) {
                entry.setTotalPoints(entry.getTotalPoints() + rating.intValue());
            }
        }

        List<LeaderBoardDTO> leaderboard = new ArrayList<>(leaderboardMap.values());
        leaderboard.sort(
                Comparator.comparingInt(LeaderBoardDTO::getTotalPoints).reversed()
                        .thenComparingInt(LeaderBoardDTO::getDoubtsSolved).reversed()
                        .thenComparing(dto -> dto.getSeniorEmail() != null ? dto.getSeniorEmail() : "", String.CASE_INSENSITIVE_ORDER)
        );

        return leaderboard;
    }

    @Override
    public List<Doubt> getRemarksBySeniorId(Long seniorId) {
        return doubtRepository.findBySolverIdOrderByDoubtRaisedAtDesc(seniorId);
    }

    @Override
    public Doubt rejectDoubt(Long doubtId) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new RuntimeException("Doubt not found"));

        if (doubt.getStatus() != DoubtStatus.PENDING) {
            throw new RuntimeException("Only pending doubts can be rejected.");
        }

        doubt.setStatus(DoubtStatus.REJECTED);
        return doubtRepository.save(doubt);
    }

    @Override
    public List<Doubt> approveAllPendingDoubts() {
        List<Doubt> pendingDoubts = doubtRepository.findByStatus(DoubtStatus.PENDING);

        if (pendingDoubts.isEmpty()) {
            throw new RuntimeException("No pending doubts found.");
        }

        for (Doubt doubt : pendingDoubts) {
            doubt.setVerified(true);
            doubt.setStatus(DoubtStatus.APPROVED);
        }

        return doubtRepository.saveAll(pendingDoubts);
    }

    @Override
    public Doubt closeChat(Long doubtId, Long userId, String role) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new RuntimeException("Doubt not found"));

        String normalizedRole = role == null ? "" : role.trim().toUpperCase();

        if ("JUNIOR".equals(normalizedRole)) {
            if (doubt.getAsker() == null || !doubt.getAsker().getId().equals(userId)) {
                throw new RuntimeException("You are not allowed to close this chat");
            }
        } else if ("SENIOR".equals(normalizedRole)) {
            if (doubt.getSolver() == null || !doubt.getSolver().getId().equals(userId)) {
                throw new RuntimeException("You are not allowed to close this chat");
            }
        } else {
            throw new RuntimeException("Invalid role");
        }

        if (!doubt.isChatEnabled()) {
            throw new RuntimeException("Chat is not enabled for this doubt");
        }

        if (doubt.isChatClosed()) {
            return doubt;
        }

        doubt.setChatClosed(true);
        doubt.setChatClosedAt(LocalDateTime.now());
        return doubtRepository.save(doubt);
    }

    private String saveOptimizedFile(MultipartFile file) {
        try {
            String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
            String originalFileName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();

            String baseName = UUID.randomUUID().toString();
            Path uploadDir = Paths.get("uploads", "images").toAbsolutePath().normalize();

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            if (contentType.startsWith("image/")) {
                BufferedImage originalImage;
                try (InputStream inputStream = file.getInputStream()) {
                    originalImage = ImageIO.read(inputStream);
                }

                if (originalImage == null) {
                    String fallbackName = baseName + "_" + originalFileName;
                    Files.copy(file.getInputStream(), uploadDir.resolve(fallbackName), StandardCopyOption.REPLACE_EXISTING);
                    return fallbackName;
                }

                BufferedImage resized = resizeImage(originalImage, 1280, 1280);

                if (contentType.contains("png")) {
                    String fileName = baseName + ".png";
                    ImageIO.write(resized, "png", uploadDir.resolve(fileName).toFile());
                    return fileName;
                } else {
                    String fileName = baseName + ".jpg";
                    writeJpeg(resized, uploadDir.resolve(fileName), 0.72f);
                    return fileName;
                }
            }

            String fallbackName = baseName + "_" + originalFileName;
            Files.copy(file.getInputStream(), uploadDir.resolve(fallbackName), StandardCopyOption.REPLACE_EXISTING);
            return fallbackName;

        } catch (IOException e) {
            throw new RuntimeException("Error while saving image: " + e.getMessage(), e);
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = copy.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();
            return copy;
        }

        double widthRatio = (double) maxWidth / width;
        double heightRatio = (double) maxHeight / height;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resized;
    }

    private void writeJpeg(BufferedImage image, Path outputPath, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ImageIO.write(image, "jpg", outputPath.toFile());
            return;
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputPath.toFile())) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();

            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }
}