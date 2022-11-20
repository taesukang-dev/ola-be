package com.example.ola.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Paging {
    private final static int PAGE_LIST_NUM = 5;
    public static List<Integer> getPageList(int elementCount, int postSize, Integer currentPage) {
        int total = (int) Math.ceil((float) elementCount / postSize);
        int startNumber = Math.max(currentPage - (PAGE_LIST_NUM / 2), 0);
        int endNumber = Math.min(startNumber + PAGE_LIST_NUM, total);
        return IntStream.range(startNumber, endNumber).boxed().collect(Collectors.toList());
    }
}
