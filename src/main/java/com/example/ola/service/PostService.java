package com.example.ola.service;

import com.example.ola.domain.Post;
import com.example.ola.domain.TeamBuildingPost;
import com.example.ola.dto.PostDto;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.request.PostUpdateRequest;
import com.example.ola.dto.request.PostWriteRequest;
import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.PostRepository;
import com.example.ola.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostDto write(PostWriteRequest postWriteRequest, String userPrincipalUsername) {
        if (!userPrincipalUsername.equals(postWriteRequest.getUsername())) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        return PostDto.fromPost(
                postRepository.save(
                        Post.of(
                                userRepository.findByUsername(postWriteRequest.getUsername())
                                        .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND)),
                                postWriteRequest.getTitle(),
                                postWriteRequest.getContent())));
    }

    @Transactional
    public TeamPostDto writeTeamPost(TeamPostWriteRequest teamPostWriteRequest, String userPrincipalUsername) {
        if (!userPrincipalUsername.equals(teamPostWriteRequest.getUsername())) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        return TeamPostDto.fromPost(
                postRepository.saveTeamPost(
                        TeamBuildingPost.of(
                                userRepository.findByUsername(teamPostWriteRequest.getUsername())
                                        .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND)),
                                teamPostWriteRequest.getTitle(),
                                teamPostWriteRequest.getContent(),
                                teamPostWriteRequest.getPlace(),
                                teamPostWriteRequest.getLimits())));
    }

    @Transactional
    public PostDto updatePost(PostUpdateRequest param, String userPrincipalUsername) {
        Post foundedPost = postRepository.findById(param.getId())
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!foundedPost.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        foundedPost.update(param.getContent(), param.getTitle());
        return PostDto.fromPost(foundedPost);
    }

    @Transactional
    public TeamPostDto updateTeamPost(TeamPostUpdateRequest param, String userPrincipalUsername) {
        TeamBuildingPost foundedPost = postRepository.findTeamPostById(param.getId())
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!foundedPost.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        foundedPost.update(param.getTitle(), param.getContent(), param.getPlace());
        return TeamPostDto.fromPost(foundedPost);
    }

    public PostDto findById(Long postId) {
        return PostDto.fromPost(postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND)));
    }

    public TeamPostDto findTeamPostById(Long postId) {
        return TeamPostDto.fromPost(postRepository.findTeamPostById(postId)
                        .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND)));
    }

    public List<PostDto> findAllPostsWithPaging(int start) {
        return postRepository.findAllPostsWithPaging(start)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(PostDto::fromPost)
                .collect(Collectors.toList());
    }

    public List<TeamPostDto> findAllTeamPostsWithPaging(int start) {
        return postRepository.findAllTeamPostsWithPaging(start)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(TeamPostDto::fromPost)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removePost(Long postId, String userPrincipalUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        postRepository.remove(post);
    }

    @Transactional
    public void removeTeamPost(Long postId, String userPrincipalUsername) {
        TeamBuildingPost post = postRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        postRepository.remove(post);
    }

}
