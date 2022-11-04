package com.example.ola.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE user SET DELETED_AT = NOW() where id = ?")
@Where(clause = "deleted_at is null")
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String name;
    private Long ageRange;
    private String homeGym;
    private UserRole role = UserRole.USER;
    @Column(name = "registered_at") private Timestamp registeredAt;
    @Column(name = "updated_at") private Timestamp updatedAt;
    @Column(name = "deleted_at") private Timestamp deletedAt;

    public User(String username, String password, String nickname, String name, Long ageRange, String homeGym) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.ageRange = ageRange;
        this.homeGym = homeGym;
    }

    public static User of(String username, String password, String nickname, String name, Long ageRange, String homeGym) {
        return new User(username, password, nickname, name, ageRange, homeGym);
    }

    @PrePersist void registeredAt() { this.registeredAt = Timestamp.from(Instant.now()); }
    @PreUpdate void updatedAt() { this.updatedAt = Timestamp.from(Instant.now()); }
}
