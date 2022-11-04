package com.example.ola.service;

import com.example.ola.domain.Post;
import com.example.ola.domain.TeamBuildingPost;
import com.example.ola.dto.PostDto;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.request.PostWriteRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.PostRepository;
import com.example.ola.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostDto write(PostWriteRequest postWriteRequest) {
        return PostDto.fromPost(
                postRepository.save(
                        Post.of(
                                userRepository.findByUsername(postWriteRequest.getUsername())
                                        .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND)),
                                postWriteRequest.getTitle(),
                                postWriteRequest.getContent()
                        ))
        );
    }

    public TeamPostDto createTeamPost(TeamPostWriteRequest teamPostWriteRequest) {
        return TeamPostDto.fromPost(
                postRepository.save(
                        TeamBuildingPost.of(
                                userRepository.findByUsername(teamPostWriteRequest.getUsername())
                                        .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND)),
                                teamPostWriteRequest.getTitle(),
                                teamPostWriteRequest.getContent(),
                                teamPostWriteRequest.getPlace(),
                                teamPostWriteRequest.getLimits()
                        )
                )
        );
    }

    public PostDto findById(Long postId) {
        return PostDto.fromPost(postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND)));
    }

    public TeamPostDto findTeamPostById(Long postId) {
        return TeamPostDto.fromPost(
                postRepository.findTeamPostById(postId)
                        .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND)));
    }

    public List<PostDto> findAllPostsWithPaging(int start) {
        return postRepository.findAllPostsWithPaging(start)
                .stream().map((e) -> PostDto.fromPost((Post) e))
                .collect(Collectors.toList());
    }

    public List<TeamPostDto> findAllTeamPostsWithPaging(int start) {
        return postRepository.findAllTeamPostsWithPaging(start)
                .stream().map((e) -> TeamPostDto.fromPost((TeamBuildingPost) e))
                .collect(Collectors.toList());
    }

    public void removePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getName().equals(username)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        postRepository.remove(post);
    }
    public void removeTeamPost(Long postId, String username) {
        TeamBuildingPost post = postRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getName().equals(username)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        postRepository.remove(post);
    }

}
