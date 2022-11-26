package com.example.ola.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class EmitterRepository {

    private Map<String, SseEmitter> emitterMap = new HashMap<>();
    private final static Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter save(Long userId) {
        final String key = getKey(userId);
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterMap.put(key, sseEmitter);
        return sseEmitter;
    }

    public Optional<SseEmitter> get(Long userId) {
        final String key = getKey(userId);
        return Optional.ofNullable(emitterMap.get(key));
    }

    public void delete(Long userId) {
        emitterMap.remove(getKey(userId));
    }

    public String getKey(Long userId) {
        return "Emiiter:UID" + userId;
    }



}
