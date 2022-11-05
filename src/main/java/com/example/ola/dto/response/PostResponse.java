package com.example.ola.dto.response;

import com.example.ola.domain.Post;
import com.example.ola.dto.PostDto;
import com.example.ola.dto.UserDto;
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
    private UserJoinResponse userDto;
    private String title;
    private String content;
    private Timestamp registeredAt;

    public static PostResponse fromPostDto(PostDto post) {
        return new PostResponse(
                post.getId(),
                UserJoinResponse.fromUserDto(post.getUserDto()),
                post.getTitle(),
                post.getContent(),
                post.getRegisteredAt()
        );
    }
}
