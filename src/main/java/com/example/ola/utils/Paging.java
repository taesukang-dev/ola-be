package com.example.ola.utils;

import com.example.ola.dto.request.PostType;
import com.example.ola.repository.PostRepository;
import com.example.ola.repository.TeamPostRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Paging {
    public static List<Integer> getPageList(PostRepository postRepository, PostType postType, Integer currentPage) {
        int result;
        int total;
        if (postType == PostType.POST) {
            result = postRepository.getPostCount("post").intValue();
            total = (int) Math.ceil((float) result / 10);
        } else {
            result = postRepository.getPostCount("T").intValue();
            total = (int) Math.ceil((float) result / 9);
        }
        int startNumber = Math.max(currentPage - (5 / 2), 0);
        int endNumber = Math.min(startNumber + 5, total);
        return IntStream.range(startNumber, endNumber).boxed().collect(Collectors.toList());
    }

    public static List<Integer> getPageList(TeamPostRepository postRepository, PostType postType, Integer currentPage) {
        int result;
        int total;
        if (postType == PostType.POST) {
            result = postRepository.getPostCount("post").intValue();
            total = (int) Math.ceil((float) result / 10);
        } else {
            result = postRepository.getPostCount("T").intValue();
            total = (int) Math.ceil((float) result / 9);
        }
        int startNumber = Math.max(currentPage - (5 / 2), 0);
        int endNumber = Math.min(startNumber + 5, total);
        return IntStream.range(startNumber, endNumber).boxed().collect(Collectors.toList());
    }
}
