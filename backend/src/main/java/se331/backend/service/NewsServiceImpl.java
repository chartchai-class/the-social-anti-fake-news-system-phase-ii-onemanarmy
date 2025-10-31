package se331.backend.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import se331.backend.dao.NewsDao;
import se331.backend.entity.*;
import se331.backend.util.NewsMapper;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsDao newsDao;

    @Autowired
    private NewsMapper newsMapper;

    /**
     * ดึงข่าวทั้งหมด (user ปกติเห็นเฉพาะข่าวที่ไม่ถูกลบ)
     */
    @Override
    public List<NewsDTO> getAllNews() {
        List<News> allNews = newsDao.findAll();
        boolean isAdmin = isCurrentUserAdmin();

        // กรองข่าว: ถ้าไม่ใช่ admin จะไม่เห็นข่าวที่ถูกลบ
        return allNews.stream()
                .filter(news -> !news.isRemoved() || isAdmin)
                .map(newsMapper::toNewsDTO) // แปลง Entity → DTO
                .collect(Collectors.toList());
    }

    /**
     * ดึงเฉพาะข่าวที่ถูกลบ (สำหรับ admin)
     */
    @Override
    public List<NewsDTO> getRemovedNews() {
        // เดิมมี security check แต่ถูก comment ออก
        return newsDao.findAll().stream()
                .filter(News::isRemoved) // กรองเฉพาะข่าวที่ removed = true
                .map(newsMapper::toNewsDTO)
                .collect(Collectors.toList());
    }

    /**
     * ดึงข่าวตาม ID
     */
    @Override
    public NewsDTO getNewsById(Long id) {
        News news = newsDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("News not found with id: " + id));

        // ถ้าข่าวถูกลบและ user ไม่ใช่ admin = ห้ามดู
        if (news.isRemoved() && !isCurrentUserAdmin()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found with id: " + id);
        }

        return newsMapper.toNewsDTO(news);
    }

    /**
     * สร้างข่าวใหม่
     */
    @Override
    @Transactional
    public NewsDTO createNews(CreateNewsRequest request) {
        News news = new News();
        news.setTopic(request.getTopic());
        news.setShortDetail(request.getShortDetail());
        news.setFullDetail(request.getFullDetail());
        news.setImage(request.getImage());
        news.setReporter(request.getReporter());

        // จัดการ dateTime: ถ้าไม่ส่งมาให้ใช้เวลาปัจจุบัน
        String rawDateTime = request.getDateTime();
        Instant createdAt;
        if (rawDateTime == null || rawDateTime.isBlank()) {
            createdAt = Instant.now();
        } else {
            try {
                createdAt = Instant.parse(rawDateTime); // แปลง String → Instant
            } catch (DateTimeParseException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid dateTime format. Expecting ISO-8601, e.g. 2024-05-01T12:34:56Z",
                        ex
                );
            }
        }
        news.setDateTime(createdAt);
        news.setRemoved(false); // ข่าวใหม่ยังไม่ถูกลบ

        News savedNews = newsDao.save(news); // บันทึกลง database
        return newsMapper.toNewsDTO(savedNews);
    }

    /**
     * เพิ่ม comment และ vote ให้กับข่าว
     */
    @Override
    @Transactional
    public NewsDTO addCommentToNews(Long newsId, CreateCommentRequest request) {
        News news = newsDao.findById(newsId)
                .orElseThrow(() -> new EntityNotFoundException("News not found with id: " + newsId));

        // สร้าง comment object
        Comment comment = new Comment();
        comment.setUsername(request.getUsername());
        comment.setText(request.getText());
        comment.setImage(request.getImage());
        comment.setTime(Instant.now());
        comment.setVote(Vote.valueOf(request.getVote().toUpperCase())); // แปลง String → Enum

        news.addComment(comment); // เพิ่ม comment เข้าไปในข่าว

        News updatedNews = newsDao.save(news);
        return newsMapper.toNewsDTO(updatedNews);
    }

    /**
     * ลบข่าว
     */
    @Override
    @Transactional
    public void deleteNews(Long id) {
        News news = newsDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found with id: " + id));
        news.setRemoved(true); // ทำเครื่องหมายว่าถูกลบ (ไม่ลบจริงออกจาก DB)
        newsDao.save(news);
    }

    /**
     * ลบ comment ออกจากข่าว
     */
    @Override
    @Transactional
    public void deleteCommentFromNews(Long newsId, Long commentId) {
        News news = newsDao.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found with id: " + newsId));

        // หา comment ที่ต้องการลบ
        Comment targetComment = news.getComments().stream()
                .filter(comment -> comment.getId() != null && comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Comment not found with id: " + commentId + " for news id: " + newsId));

        news.removeComment(targetComment); // ลบ comment ออกจาก list
        newsDao.save(news);
    }

    /**
     * ค้นหาและกรองข่าว
     */
    @Override
    public Page<NewsDTO> getNews(String title, String status, Pageable pageable) {
        Page<News> newsPage;
        boolean isAdmin = isCurrentUserAdmin();

        // ตรวจสอบ: ถ้าไม่ใช่ admin ห้ามดูข่าวที่ถูกลบ
        if ("removed".equalsIgnoreCase(status) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can view removed news");
        }

        // กรณีที่มีทั้ง keyword และ status
        if (title != null && !title.isBlank() && status != null && !status.isBlank()) {
            if (isAdmin) {
                newsPage = newsDao.searchByKeywordAndStatusIncludingRemoved(title, status, pageable);
            } else {
                newsPage = newsDao.searchByKeywordAndStatus(title, status, pageable);
            }
        }
        // กรณีที่มี keyword อย่างเดียว
        else if (title != null && !title.isBlank()) {
            if (isAdmin) {
                newsPage = newsDao.searchByKeywordIncludingRemoved(title, pageable);
            } else {
                newsPage = newsDao.searchByKeyword(title, pageable);
            }
        }
        // กรณีที่มี status อย่างเดียว
        else if (status != null && !status.isBlank()) {
            if (isAdmin) {
                newsPage = newsDao.findAllByStatus(status, pageable);
            } else {
                newsPage = newsDao.findAllVisibleByStatus(status, pageable);
            }
        }
        // กรณีที่ไม่มีทั้ง keyword และ status (ดึงทั้งหมด)
        else {
            if (isAdmin) {
                newsPage = newsDao.findAll(pageable);
            } else {
                newsPage = newsDao.findAllVisible(pageable);
            }
        }

        // แปลง Page<News> → Page<NewsDTO>
        return newsPage.map(newsMapper::toNewsDTO);
    }

    private boolean isCurrentUserAdmin() {
        // ดึงข้อมูล authentication จาก Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // ตรวจสอบว่า login อยู่หรือไม่
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        // ตรวจสอบว่ามี role ROLE_ADMIN หรือไม่
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
    }
}