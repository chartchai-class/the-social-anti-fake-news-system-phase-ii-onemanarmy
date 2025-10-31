package se331.backend.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import se331.backend.entity.Comment;

public interface CommentDAO {
    Comment save(Comment comment);
    Page<Comment> findByNewsId(Long newsId, Pageable pageable);
    void deleteById(Long id);
    Comment findById(Long id);
}