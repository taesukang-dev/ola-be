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

    /**
     * 게시글 작성
     * @param postWriteRequest
     * @param userPrincipalUsername
     * @return PostDto
     */
    @Transactional
    public PostDto write(PostWriteRequest postWriteRequest, String userPrincipalUsername) {
        if (!userPrincipalUsername.equals(postWriteRequest.getUsername())) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        return PostDto.fromPost(
                postRepository.save(
                        Post.of(
                                getUserByUsernameOrElseThrow(postWriteRequest.getUsername()),
                                postWriteRequest.getTitle(),
                                postWriteRequest.getContent(),
                                postWriteRequest.getImgUri())));
    }

    /**
     * 게시글 수정
     * @param param
     * @param userPrincipalUsername
     * @return PostDto
     */
    @Transactional
    public PostDto updatePost(PostUpdateRequest param, String userPrincipalUsername) {
        Post foundedPost = getPostByIdOrElseThrow(param.getId());
        if (!foundedPost.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        foundedPost.update(param.getTitle(), param.getContent(), param.getImgUri());
        return PostDto.fromPost(foundedPost);
    }

    /**
     * 페이징된 게시글 반환
     * @param start
     * @param keyword
     * @return MyPageResponse
     */
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

    /**
     * 키워드로 검색된 페이징 게시글 반환
     * @param keyword
     * @return MyPageResponse
     */
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


    /**
     * 내가 작성한 게시글 반환
     * @param userPrincipalUsername
     * @return List<PostDto>
     */
    public List<PostDto> findPostsByUsername(String userPrincipalUsername) {
        return postRepository.findPostsByUsername(userPrincipalUsername)
                .map(e -> e.stream().map(PostDto::fromPost)
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    /**
     * 게시글 삭제
     * @param postId
     * @param userPrincipalUsername
     */
    @Transactional
    public void delete(Long postId, String userPrincipalUsername) {
        Post post = getPostByIdOrElseThrow(postId);
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        postRepository.remove(post);
        commentRepository.deleteByPostId(postId);
        alarmRepository.deleteByPostId(postId);
    }

    private Post getPostByIdOrElseThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
    }

    private User getUserByUsernameOrElseThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
    }

    public PostDto findById(Long postId) {
        return PostDto.fromPost(getPostByIdOrElseThrow(postId));
    }
}
