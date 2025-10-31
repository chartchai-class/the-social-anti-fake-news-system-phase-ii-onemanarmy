package se331.backend.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import se331.backend.entity.News;
import se331.backend.repository.NewsRepository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class NewsDaoImpl implements NewsDao {

    @Autowired
    private NewsRepository newsRepository;

    @Override
    public List<News> findAll() {
        return newsRepository.findAll();
    }

    @Override
    public Optional<News> findById(Long id) {
        return newsRepository.findById(id);
    }

    @Override
    public News save(News news) {
        return newsRepository.save(news);
    }

    @Override
    public void deleteById(Long id) {
        newsRepository.deleteById(id);
    }

    @Override
    public Page<News> findAll(Pageable pageable) {
        List<News> allNews = newsRepository.findAll();
        return createPageFromList(allNews, pageable);
    }

    @Override
    public Page<News> findAllVisible(Pageable pageable) {
        List<News> allVisible = newsRepository.findByRemovedFalse(Pageable.unpaged()).getContent();
        return createPageFromList(allVisible, pageable);
    }

    @Override
    public Page<News> findAllByStatus(String status, Pageable pageable) {
        List<News> allNews = newsRepository.findAll(Pageable.unpaged()).getContent();
        List<News> filteredByStatus = filterByStatusIncludingRemoved(allNews, status);
        return createPageFromList(filteredByStatus, pageable);
    }

    @Override
    public Page<News> findAllVisibleByStatus(String status, Pageable pageable) {
        List<News> allVisible = newsRepository.findByRemovedFalse(Pageable.unpaged()).getContent();
        List<News> filteredByStatus = filterByStatus(allVisible, status);
        return createPageFromList(filteredByStatus, pageable);
    }


    @Override
    public Page<News> searchByKeyword(String keyword, Pageable pageable) {

        Set<News> results = new LinkedHashSet<>();

        List<News> topicResults = newsRepository.findByRemovedFalseAndTopicContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(topicResults);

        List<News> shortDetailResults = newsRepository.findByRemovedFalseAndShortDetailContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(shortDetailResults);

        List<News> reporterResults = newsRepository.findByRemovedFalseAndReporterContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(reporterResults);

        return createPageFromList(new ArrayList<>(results), pageable);
    }

    @Override
    public Page<News> searchByKeywordIncludingRemoved(String keyword, Pageable pageable) {
        Set<News> results = new LinkedHashSet<>();

        List<News> topicResults = newsRepository.findByTopicContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(topicResults);

        List<News> shortDetailResults = newsRepository.findByShortDetailContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(shortDetailResults);

        List<News> reporterResults = newsRepository.findByReporterContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(reporterResults);

        return createPageFromList(new ArrayList<>(results), pageable);
    }

    @Override
    public Page<News> searchByKeywordAndStatus(String keyword, String status, Pageable pageable) {
        Set<News> results = new LinkedHashSet<>();

        List<News> topicResults = newsRepository.findByRemovedFalseAndTopicContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(topicResults);

        List<News> shortDetailResults = newsRepository.findByRemovedFalseAndShortDetailContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(shortDetailResults);

        List<News> reporterResults = newsRepository.findByRemovedFalseAndReporterContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(reporterResults);

        List<News> filteredByStatus = filterByStatus(new ArrayList<>(results), status);

        return createPageFromList(filteredByStatus, pageable);
    }

    @Override
    public Page<News> searchByKeywordAndStatusIncludingRemoved(String keyword, String status, Pageable pageable) {
        Set<News> results = new LinkedHashSet<>();

        List<News> topicResults = newsRepository.findByTopicContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(topicResults);

        List<News> shortDetailResults = newsRepository.findByShortDetailContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(shortDetailResults);

        List<News> reporterResults = newsRepository.findByReporterContainingIgnoreCase(keyword, Pageable.unpaged()).getContent();
        results.addAll(reporterResults);

        List<News> filteredByStatus = filterByStatusIncludingRemoved(new ArrayList<>(results), status);
        return createPageFromList(filteredByStatus, pageable);
    }

    private List<News> filterByStatus(List<News> newsList, String status) {
        if (status == null || status.equalsIgnoreCase("all")) {
            return newsList;
        }

        return newsList.stream()
                .filter(news -> {
                    String newsStatus = calculateStatus(news);
                    return newsStatus.equalsIgnoreCase(status);
                })
                .collect(Collectors.toList());
    }

    private List<News> filterByStatusIncludingRemoved(List<News> newsList, String status) {
        if (status == null || status.equalsIgnoreCase("all")) {
            return newsList; // ส่งกลับทั้งหมด
        }

        if (status.equalsIgnoreCase("removed")) {
            return newsList.stream()
                    .filter(News::isRemoved)
                    .collect(Collectors.toList());
        }

        return newsList.stream()
                .filter(news -> !news.isRemoved())
                .filter(news -> {
                    String newsStatus = calculateStatus(news);
                    return newsStatus.equalsIgnoreCase(status);
                })
                .collect(Collectors.toList());
    }

    private String calculateStatus(News news) {
        int realVotes = news.getRealVotes() != null ? news.getRealVotes() : 0;
        int fakeVotes = news.getFakeVotes() != null ? news.getFakeVotes() : 0;

        if (realVotes > fakeVotes) {
            return "real";
        } else if (realVotes < fakeVotes) {
            return "fake";
        } else {
            return "equal";
        }
    }

    private Page<News> createPageFromList(List<News> list, Pageable pageable) {
        List<News> sortedList = sortNewsList(list, pageable.getSort());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedList.size());

        List<News> pageContent = start >= sortedList.size() ? List.of() : sortedList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, sortedList.size());
    }


    private List<News> sortNewsList(List<News> list, Sort sort) {
        if (sort.isUnsorted()) {
            return list;
        }

        List<News> sortedList = new ArrayList<>(list);

        for (Sort.Order order : sort) {
            String property = order.getProperty();
            boolean isAsc = order.isAscending();

            sortedList.sort((a, b) -> {
                int comparison = 0;

                switch (property) {
                    case "dateTime":
                        // Sort by dateTime
                        comparison = a.getDateTime().compareTo(b.getDateTime());
                        break;

                    case "totalVotes":
                        // Sort by total votes (real + fake)
                        comparison = Integer.compare(a.getTotalVotes(), b.getTotalVotes());
                        break;

                    case "commentCount":
                        // Sort by number of comments
                        comparison = Integer.compare(a.getCommentCount(), b.getCommentCount());
                        break;

                    default:
                        // other field no sort
                        comparison = 0;
                }

                // descending?
                return isAsc ? comparison : -comparison;
            });
        }

        return sortedList;
    }
}