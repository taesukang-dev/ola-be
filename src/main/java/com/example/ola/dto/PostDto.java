package com.example.ola.dto;

import com.example.ola.domain.Post;
import com.example.ola.domain.User;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDto {
    private Long id;
    private UserDto userDto;
    private String title;
    private String content;
    private Timestamp registeredAt;

    public static PostDto fromPost(Post post) {
        return new PostDto(
                post.getId(),
                UserDto.fromUser(post.getUser()),
                post.getTitle(),
                post.getContent(),
                post.getRegisteredAt()
        );
    }
}
