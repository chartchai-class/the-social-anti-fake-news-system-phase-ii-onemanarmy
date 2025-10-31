package se331.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import se331.backend.entity.CreateCommentRequest;
import se331.backend.entity.CreateNewsRequest;
import se331.backend.entity.NewsDTO;

import java.util.List;

public interface NewsService {
    List<NewsDTO> getAllNews();
    List<NewsDTO> getRemovedNews();
    NewsDTO getNewsById(Long id);
    NewsDTO createNews(CreateNewsRequest request);
    NewsDTO addCommentToNews(Long newsId, CreateCommentRequest request);
    void deleteNews(Long id);
    void deleteCommentFromNews(Long newsId, Long commentId);

    // *** อัปเดต: เพิ่ม parameter status ***
    Page<NewsDTO> getNews(String title, String status, Pageable pageable);
}