package se331.backend.dao;

import se331.backend.entity.News;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsDao {
    List<News> findAll();
    Optional<News> findById(Long id);
    News save(News news);
    void deleteById(Long id);

    // users
    Page<News> searchByKeyword(String keyword, Pageable pageable);
    Page<News> findAllVisible(Pageable pageable);
    Page<News> searchByKeywordAndStatus(String keyword, String status, Pageable pageable);
    Page<News> findAllVisibleByStatus(String status, Pageable pageable);

    // admin
    Page<News> searchByKeywordIncludingRemoved(String keyword, Pageable pageable);
    Page<News> findAll(Pageable pageable);
    Page<News> searchByKeywordAndStatusIncludingRemoved(String keyword, String status, Pageable pageable);
    Page<News> findAllByStatus(String status, Pageable pageable);
}