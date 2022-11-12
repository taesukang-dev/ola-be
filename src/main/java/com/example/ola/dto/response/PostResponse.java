package com.example.ola.dto.response;

import com.example.ola.dto.PostDto;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostResponse {
    private Long id;
    private UserResponse user;
    private String title;
    private String content;
    private Timestamp registeredAt;

    public static PostResponse fromPostDto(PostDto post) {
        return new PostResponse(
                post.getId(),
                UserResponse.fromUserDto(post.getUserDto()),
                post.getTitle(),
                post.getContent(),
                post.getRegisteredAt()
        );
    }
}
