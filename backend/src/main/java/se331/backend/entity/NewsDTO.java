package se331.backend.entity;

import java.util.List;

public class NewsDTO {
    private Long id;
    private String topic;
    private String shortDetail;
    private String fullDetail;
    private String image;
    private String reporter;
    private String dateTime;
    private VoteSummaryDTO voteSummary;
    private int totalVotes;
    private List<CommentDTO> comments;
    private String status;
    private boolean removed;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getShortDetail() { return shortDetail; }
    public void setShortDetail(String shortDetail) { this.shortDetail = shortDetail; }
    public String getFullDetail() { return fullDetail; }
    public void setFullDetail(String fullDetail) { this.fullDetail = fullDetail; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public VoteSummaryDTO getVoteSummary() { return voteSummary; }
    public void setVoteSummary(VoteSummaryDTO voteSummary) { this.voteSummary = voteSummary; }
    public int getTotalVotes() { return totalVotes; }
    public void setTotalVotes(int totalVotes) { this.totalVotes = totalVotes; }
    public List<CommentDTO> getComments() { return comments; }
    public void setComments(List<CommentDTO> comments) { this.comments = comments; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isRemoved() { return removed; }
    public void setRemoved(boolean removed) { this.removed = removed; }
}
