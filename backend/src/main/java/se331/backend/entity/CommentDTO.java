package se331.backend.entity;

public class CommentDTO {
    private Long id;
    private String username;
    private String text;
    private String image;
    private String time;
    private String vote;
    private Long newsId;

    public CommentDTO() {}

    public CommentDTO(Long id, String username, String text, String image, String time, String vote, Long newsId) {
        this.id = id;
        this.username = username;
        this.text = text;
        this.image = image;
        this.time = time;
        this.vote = vote;
        this.newsId = newsId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getVote() { return vote; }
    public void setVote(String vote) { this.vote = vote; }
    public Long getNewsId() { return newsId; }
    public void setNewsId(Long newsId) { this.newsId = newsId; }
}