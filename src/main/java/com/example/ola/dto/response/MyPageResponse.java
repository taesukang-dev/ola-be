package com.example.ola.dto.response;

import java.util.List;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyPageResponse<T> {
    T contents;
    List<Integer> pageList;

    public static <T> MyPageResponse of(T result, List<Integer> pageList) {
        return new MyPageResponse(result, pageList);
    }
}
