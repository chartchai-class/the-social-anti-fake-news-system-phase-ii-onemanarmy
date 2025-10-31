package se331.backend.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se331.backend.entity.CommentDTO;
import se331.backend.entity.CreateCommentRequest;
import se331.backend.entity.Comment;
import se331.backend.entity.News;
import se331.backend.entity.Vote;
import se331.backend.repository.NewsRepository;
import se331.backend.service.CommentService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private NewsRepository newsRepository;

    /**
     * ดึงคอมเมนต์ทั้งหมดของข่าว (มี Pagination)
     */
    @GetMapping("/news/{newsId}")
    public ResponseEntity<Page<CommentDTO>> getCommentsByNews(
            @PathVariable Long newsId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Comment> comments = commentService.getCommentsByNewsId(
                newsId,
                PageRequest.of(page, size, Sort.by("time").descending())
        );

        Page<CommentDTO> commentDTOs = comments.map(this::convertToDTO);
        return ResponseEntity.ok(commentDTOs);
    }

    /**
     * สร้างคอมเมนต์ใหม่
     */
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(@RequestBody CreateCommentRequest request) {

        // Validation
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getVote() == null || request.getNewsId() == null) {
            return ResponseEntity.badRequest().build();
        }

        // ตรวจสอบว่าข่าวมีอยู่จริง
        News news = newsRepository.findById(request.getNewsId())
                .orElseThrow(() -> new RuntimeException("News not found with id: " + request.getNewsId()));

        // แปลง String vote เป็น Enum
        Vote voteEnum;
        try {
            voteEnum = Vote.valueOf(request.getVote().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        // สร้าง Comment Entity
        Comment comment = Comment.builder()
                .username(request.getUsername())
                .text(request.getText())
                .image(request.getImage())
                .vote(voteEnum)
                .time(Instant.now())
                .news(news)
                .build();

        // บันทึก comment
        Comment savedComment = commentService.save(comment);

        // เพิ่ม comment ไปยัง news และบันทึก
        news.addComment(savedComment);
        newsRepository.save(news);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToDTO(savedComment));
    }

    /**
     * ลบคอมเมนต์
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }

    /**
     * ดึงสรุปคะแนนโหวต
     */
    @GetMapping("/news/{newsId}/summary")
    public ResponseEntity<?> getCommentSummary(@PathVariable Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found"));

        return ResponseEntity.ok(new Object() {
            public final Long newsId = news.getId();
            public final int real = news.getRealVotes(); // real
            public final int fake = news.getFakeVotes(); // fake
            public final int totalComments = news.getComments().size();
        });
    }

    /**
     * แปลง Comment Entity เป็น CommentDTO
     */
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setUsername(comment.getUsername());
        dto.setText(comment.getText());
        dto.setImage(comment.getImage());

        // แปลง Instant เป็น String
        if (comment.getTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
            dto.setTime(formatter.format(comment.getTime()));
        }

        // แปลง Vote Enum เป็น String lowercase
        dto.setVote(comment.getVote() != null ? comment.getVote().name().toLowerCase() : null);

        if (comment.getNews() != null) {
            dto.setNewsId(comment.getNews().getId());
        }

        return dto;
    }

}