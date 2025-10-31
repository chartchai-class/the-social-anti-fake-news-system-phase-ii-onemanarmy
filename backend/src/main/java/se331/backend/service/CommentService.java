package se331.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import se331.backend.entity.Comment;

public interface CommentService {
    Comment save(Comment comment);
    Page<Comment> getCommentsByNewsId(Long newsId, Pageable pageable);
    void deleteComment(Long id);
    Comment getCommentById(Long id); // เพิ่มบรรทัดนี้
}