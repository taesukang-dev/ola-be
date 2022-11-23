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
@SQLDelete(sql = "UPDATE home_gym SET DELETED_AT = NOW() where id = ?")
@Where(clause = "deleted_at is null")
@Entity
public class HomeGym {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String placeName;
    private String roadAddressName;
    private String categoryName;
    private Double x;
    private Double y;

    public HomeGym(String placeName, String roadAddressName, String categoryName, Double x, Double y) {
        this.placeName = placeName;
        this.roadAddressName = roadAddressName;
        this.categoryName = categoryName;
        this.x = x;
        this.y = y;
    }

    public static HomeGym of(String placeName, String roadAddressName, String categoryName, Double x, Double y) {
        return new HomeGym(placeName, roadAddressName, categoryName, x, y);
    }

    @Column(name = "registered_at") private Timestamp registeredAt;
    @Column(name = "updated_at") private Timestamp updatedAt;
    @Column(name = "deleted_at") private Timestamp deletedAt;


    @PrePersist
    void registeredAt() { this.registeredAt = Timestamp.from(Instant.now()); }
    @PreUpdate
    void updatedAt() { this.updatedAt = Timestamp.from(Instant.now()); }
}
