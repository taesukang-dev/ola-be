package com.example.ola.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyPageImpl<T>{
    T content;
    MyPageableImpl myPageable;
    int currentPage;
    int totalElements;

    public static <T> MyPageImpl of(T content, MyPageableImpl myPageable, int currentPage, int totalElements) {
        return new MyPageImpl(content, myPageable, currentPage, totalElements);
    }

    public List<Integer> getPageList() {
        int totalPage = (int) Math.ceil((float) totalElements / myPageable.getSize());

        int startNumber = Math.max(currentPage - (5 / 2), 0);
        int endNumber = Math.min(startNumber + 5, totalPage);
        return IntStream.range(startNumber, endNumber).boxed().collect(Collectors.toList());
    }
}
