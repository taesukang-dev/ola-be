package com.example.ola.dto;

import com.example.ola.domain.Comment;
import com.example.ola.domain.Post;
import com.example.ola.domain.User;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private UserDto user;
    private PostDto post;
    private String content;
    private List<CommentDto> child;

    public static CommentDto of(Long id, UserDto user, PostDto post, String content, List<CommentDto> child) {
        return new CommentDto(id, user, post, content, child);
    }

    public static CommentDto fromComment(Comment comment) {
        return new CommentDto(
                comment.getId(),
                UserDto.fromUser(comment.getUser()),
                PostDto.fromPost(comment.getPost()),
                comment.getContent(),
                comment.getChild()
                        .stream().map(CommentDto::fromComment)
                        .collect(Collectors.toList())
        );
    }
}
