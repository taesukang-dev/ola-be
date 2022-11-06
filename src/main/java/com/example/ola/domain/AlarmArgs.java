package com.example.ola.domain;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class AlarmArgs {
    private Long postId;
    private String fromUser;

    public static AlarmArgs of(Long postId, String fromUser) {
        return new AlarmArgs(postId, fromUser);
    }
}
