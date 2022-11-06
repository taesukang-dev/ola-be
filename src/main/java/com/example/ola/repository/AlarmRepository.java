package com.example.ola.repository;

import com.example.ola.domain.Alarm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class AlarmRepository {
    private final EntityManager em;

    public void save(Alarm alarm) {
        em.persist(alarm);
    }

    public Optional<Alarm> findById(Long alarmId) {
        return Optional.ofNullable(em.find(Alarm.class, alarmId));
    }

    public Optional<List<Alarm>> findByUsername(String username) {
        return Optional.ofNullable(em.createQuery("select a from Alarm a" +
                                " join fetch a.user" +
                                " where a.user.username=:username")
                        .setParameter("username", username)
                        .getResultList());
    }

    public void remove(Alarm alarm) {
        em.remove(alarm);
    }
}
