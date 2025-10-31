package se331.backend.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import se331.backend.entity.Comment;
import se331.backend.repository.CommentRepository;

@Repository
public class CommentDAOImpl implements CommentDAO {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Comment save(Comment comment) {
        System.out.println("=== DAO: Saving comment ===");
        System.out.println("Username: " + comment.getUsername());
        System.out.println("Vote: " + comment.getVote());
        System.out.println("News ID: " + (comment.getNews() != null ? comment.getNews().getId() : "null"));

        Comment saved = commentRepository.save(comment);

        System.out.println("=== DAO: Comment saved with ID: " + saved.getId() + " ===");
        return saved;
    }

    @Override
    public Page<Comment> findByNewsId(Long newsId, Pageable pageable) {
        return commentRepository.findByNewsId(newsId, pageable);
    }

    @Override
    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
    }
}