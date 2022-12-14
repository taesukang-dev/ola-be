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
    private String imgUri;
    private String password;
    private String nickname;
    private String name;
    private Long ageRange;
    @ManyToOne(fetch = FetchType.LAZY)
    private HomeGym homeGym;

    @Enumerated(EnumType.STRING) private UserGender userGender;
    @Enumerated(EnumType.STRING) private UserRole role = UserRole.USER;
    @Column(name = "registered_at") private Timestamp registeredAt;
    @Column(name = "updated_at") private Timestamp updatedAt;
    @Column(name = "deleted_at") private Timestamp deletedAt;

    public void updateUser(String name, String nickname, HomeGym homeGym, String imgUri) {
        this.name = name;
        this.nickname = nickname;
        this.homeGym = homeGym;
        this.imgUri = imgUri;
    }

    public User(String username, String imgUri, String password, String nickname, String name, Long ageRange, HomeGym homeGym, String userGender) {
        this.username = username;
        this.imgUri = imgUri;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.ageRange = ageRange;
        this.homeGym = homeGym;
        if (userGender.equals("male")) {
            this.userGender = UserGender.M;
        } else {
            this.userGender = UserGender.F;
        }
    }

    public static User of(String username, String imgUri, String password, String nickname, String name, Long ageRange, HomeGym homeGym, String userGender) {
        return new User(username, imgUri, password, nickname, name, ageRange, homeGym, userGender);
    }

    @PrePersist void registeredAt() { this.registeredAt = Timestamp.from(Instant.now()); }
    @PreUpdate void updatedAt() { this.updatedAt = Timestamp.from(Instant.now()); }
}
