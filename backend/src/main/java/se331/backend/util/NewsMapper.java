package se331.backend.util;

import se331.backend.entity.*;
import org.springframework.stereotype.Component;
import se331.backend.security.user.User;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NewsMapper {

    public UserAuthDTO toUserAuthDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserAuthDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .roles(user.getRoles())
                .build();
    }

    public NewsDTO toNewsDTO(News news) {
        NewsDTO dto = new NewsDTO();
        dto.setId(news.getId());
        dto.setTopic(news.getTopic());
        dto.setShortDetail(news.getShortDetail());
        dto.setFullDetail(news.getFullDetail());
        dto.setImage(news.getImage());
        dto.setReporter(news.getReporter());
        dto.setDateTime(
                news.getDateTime() != null
                        ? news.getDateTime().toString()
                        : null
        );
        dto.setRemoved(news.isRemoved());

        List<Comment> comments = news.getComments();
        List<CommentDTO> commentDTOs = (comments == null ? List.<CommentDTO>of() : comments.stream()
                .map(this::toCommentDTO)
                .collect(Collectors.toList()));
        dto.setComments(commentDTOs);

        VoteSummaryDTO summary = new VoteSummaryDTO(news.getRealVotes(), news.getFakeVotes());
        dto.setVoteSummary(summary);
        dto.setTotalVotes(news.getRealVotes() + news.getFakeVotes());

        return dto;
    }

    public CommentDTO toCommentDTO(Comment comment) {
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

        // แปลง Vote เป็น lowercase
        dto.setVote(comment.getVote() != null ? comment.getVote().name().toLowerCase() : null);

        if (comment.getNews() != null) {
            dto.setNewsId(comment.getNews().getId());
        }

        return dto;
    }
}
