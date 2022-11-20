package com.example.ola.service;

import com.example.ola.domain.*;
import com.example.ola.dto.PostDto;
import com.example.ola.dto.request.*;
import com.example.ola.dto.response.MyPageResponse;
import com.example.ola.dto.response.PostResponse;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.AlarmRepository;
import com.example.ola.repository.CommentRepository;
import com.example.ola.repository.PostRepository;
import com.example.ola.repository.UserRepository;
import com.example.ola.utils.Paging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AlarmRepository alarmRepository;
    private static final int POST_SIZE = 10;

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
    public PostDto updatePost(PostUpdateRequest param, String userPrincipalUsername) {
        Post foundedPost = postRepository.findById(param.getId())
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!foundedPost.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        foundedPost.update(param.getTitle(), param.getContent());
        return PostDto.fromPost(foundedPost);
    }

    public PostDto findById(Long postId) {
        return PostDto.fromPost(postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND)));
    }

    public MyPageResponse findAllPostsWithPaging(int start, String keyword) {
        if (StringUtils.hasText(keyword)) {
            return findAllPostsByKeyword(keyword);
        }
        List<PostResponse> postList = postRepository.findAllPostsWithPaging(start)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(PostDto::fromPost)
                .map(PostResponse::fromPostDto)
                .collect(Collectors.toList());
        List<Integer> pageList = Paging.getPageList(postRepository.getPostCount("post").intValue(), POST_SIZE, start);
        return MyPageResponse.of(postList, pageList);
    }

    public MyPageResponse findAllPostsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new OlaApplicationException(ErrorCode.INVALID_KEYWORD);
        }
        List<PostResponse> postList = postRepository.findAllPostsByKeyword(keyword)
                .map(e -> e.stream().map(PostDto::fromPost)
                        .collect(Collectors.toList()))
                .orElseGet(List::of)
                .stream().map(PostResponse::fromPostDto)
                .collect(Collectors.toList());
        return MyPageResponse.of(postList, List.of());
    }

    // 없을 때에는 빈 list 반환
    public List<PostDto> findPostsByUsername(String userPrincipalUsername) {
        return postRepository.findPostsByUsername(userPrincipalUsername)
                .map(e -> e.stream().map(PostDto::fromPost)
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    @Transactional
    public void delete(Long postId, String userPrincipalUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        postRepository.remove(post);
        commentRepository.deleteByPostId(postId);
        alarmRepository.deleteByPostId(postId);
    }
}
