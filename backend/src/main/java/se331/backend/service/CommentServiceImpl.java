package se331.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se331.backend.entity.Comment;
import se331.backend.dao.CommentDAO;
import se331.backend.repository.NewsRepository; // 🔥 เพิ่ม import
import se331.backend.entity.News; // 🔥 เพิ่ม import
import se331.backend.entity.Vote; // 🔥 เพิ่ม import

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private NewsRepository newsRepository;

    @Override
    @Transactional
    public Comment save(Comment comment) {
        return commentDAO.save(comment);
    }

    @Override
    public Page<Comment> getCommentsByNewsId(Long newsId, Pageable pageable) {
        return commentDAO.findByNewsId(newsId, pageable);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        // ดึง comment ก่อนลบเพื่อหา news
        Comment comment = commentDAO.findById(id);
        if (comment != null && comment.getNews() != null) {
            News news = comment.getNews();

            // ลบ comment
            commentDAO.deleteById(id);

            // อัพเดท vote count ใน news
            updateNewsVoteCount(news, comment.getVote(), false); // false = ลบ vote
        } else {
            commentDAO.deleteById(id);
        }
    }

    @Override
    public Comment getCommentById(Long id) {
        return commentDAO.findById(id);
    }

    // เพิ่ม method สำหรับอัพเดท vote count
    private void updateNewsVoteCount(News news, Vote vote, boolean isAdd) {
        if (vote == Vote.REAL) {
            int currentReal = news.getRealVotes();
            news.setRealVotes(isAdd ? currentReal + 1 : Math.max(0, currentReal - 1));
        } else if (vote == Vote.FAKE) {
            int currentFake = news.getFakeVotes();
            news.setFakeVotes(isAdd ? currentFake + 1 : Math.max(0, currentFake - 1));
        }

        // บันทึก news ที่อัพเดทแล้ว
        newsRepository.save(news);
    }
}