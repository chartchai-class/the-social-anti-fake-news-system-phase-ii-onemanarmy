package se331.backend.entity;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private String username;
    private String text;
    private String image;
    private String vote;
    private Long newsId;
}