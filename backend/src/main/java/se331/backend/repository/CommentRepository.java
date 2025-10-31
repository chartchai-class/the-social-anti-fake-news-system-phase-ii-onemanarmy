package se331.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import se331.backend.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByNewsId(Long newsId, Pageable pageable);
}