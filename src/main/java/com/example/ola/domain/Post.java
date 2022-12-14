package com.example.ola.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLDeleteAll;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE post SET DELETED_AT = NOW() where id = ?")
@Where(clause = "deleted_at is null")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
@Entity
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String title;
    private String content;
    private String imgUri;
    
    @Column(name = "registered_at") private Timestamp registeredAt;
    @Column(name = "updated_at") private Timestamp updatedAt;
    @Column(name = "deleted_at") private Timestamp deletedAt;

    public Post (User user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
    }
    
    public Post (User user, String title, String content, String imgUri) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.imgUri = imgUri;
    }

    public static Post of(User user, String title, String content, String imgUri) {
        return new Post(user, title, content, imgUri);
    }

    public void update(String title, String content, String imgUri) {
        this.title = title;
        this.content = content;
        this.imgUri = imgUri;
    }

    @PrePersist void registeredAt() { this.registeredAt = Timestamp.from(Instant.now()); }
    @PreUpdate void updatedAt() { this.updatedAt = Timestamp.from(Instant.now()); }
}
