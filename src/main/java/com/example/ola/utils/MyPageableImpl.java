package com.example.ola.utils;

import lombok.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyPageableImpl implements MyPageable{
    int page;
    int size;

    public static MyPageableImpl of(int page, int size) {
        return new MyPageableImpl(page, size);
    }
}
