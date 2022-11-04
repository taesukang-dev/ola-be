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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
@Entity
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User user;
    private String title;
    private String content;
    // TODO : COMMENT 추가할 것
    // TODO : Image 추가할 것
    @Column(name = "registered_at") private Timestamp registeredAt;
    @Column(name = "updated_at") private Timestamp updatedAt;
    @Column(name = "deleted_at") private Timestamp deletedAt;

    public Post (User user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
    }

    public static Post of(User user, String title, String content) {
        return new Post(user, title, content);
    }

    @PrePersist void registeredAt() { this.registeredAt = Timestamp.from(Instant.now()); }
    @PreUpdate void updatedAt() { this.updatedAt = Timestamp.from(Instant.now()); }
}
