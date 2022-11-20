package com.example.ola.utils;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

import java.io.Serializable;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyPageableImpl {
    int page;
    int size;

    public static MyPageableImpl of(int page, int size) {
        return new MyPageableImpl(page, size);
    }
}
