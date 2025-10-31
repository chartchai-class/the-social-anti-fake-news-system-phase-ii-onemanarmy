package se331.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;

    @Column(length = 4000)
    private String shortDetail;

    @Lob
    private String fullDetail;
    private String image;
    private String reporter;
    private Instant dateTime;

    @Builder.Default
    @Column(nullable = false)
    private boolean removed = false;

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    private Integer realVotes = 0;

    @Builder.Default
    private Integer fakeVotes = 0;

    @Transient
    private Integer totalVotes;

    @Transient
    private Integer commentCount;

    // totalVotes (คำนวณจาก realVotes + fakeVotes)
    public Integer getTotalVotes() {
        if (totalVotes == null) {
            totalVotes = (realVotes != null ? realVotes : 0) +
                    (fakeVotes != null ? fakeVotes : 0);
        }
        return totalVotes;
    }

    // commentCount (นับจำนวน comments)
    public Integer getCommentCount() {
        if (commentCount == null) {
            commentCount = comments != null ? comments.size() : 0;
        }
        return commentCount;
    }

    // Helper method สำหรับเพิ่ม comment
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setNews(this);

        // อัปเดตคะแนนโหวต
        if (comment.getVote() == Vote.REAL) {
            realVotes++;
        } else if (comment.getVote() == Vote.FAKE) {
            fakeVotes++;
        }
    }

    // Helper method สำหรับลบ comment พร้อมปรับยอดโหวต
    public void removeComment(Comment comment) {
        if (comments.remove(comment)) {
            if (comment.getVote() == Vote.REAL && realVotes > 0) {
                realVotes--;
            } else if (comment.getVote() == Vote.FAKE && fakeVotes > 0) {
                fakeVotes--;
            }
            comment.setNews(null);
        }
    }
}
