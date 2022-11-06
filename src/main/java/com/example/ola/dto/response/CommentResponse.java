package com.example.ola.dto.response;

import com.example.ola.dto.CommentDto;
import com.example.ola.dto.PostDto;
import com.example.ola.dto.UserDto;
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
public class CommentResponse {
    private Long id;
    private String username;
    private Long postId;
    private String content;
    private List<CommentResponse> child;

    public static CommentResponse of(Long id, String username, Long postId, String content, List<CommentResponse> child) {
        return new CommentResponse(id, username, postId, content, child);
    }

    public static CommentResponse fromCommentDto(CommentDto commentDto) {
        return new CommentResponse(
                commentDto.getId(),
                commentDto.getUser().getUsername(),
                commentDto.getPost().getId(),
                commentDto.getContent(),
                commentDto.getChild()
                        .stream().map(CommentResponse::fromCommentDto)
                        .collect(Collectors.toList())
        );
    }
}
