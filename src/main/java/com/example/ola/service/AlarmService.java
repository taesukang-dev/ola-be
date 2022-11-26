package com.example.ola.service;

import com.example.ola.domain.Alarm;
import com.example.ola.dto.AlarmDto;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.AlarmRepository;
import com.example.ola.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AlarmService {

    private final EmitterRepository emitterRepository;
    private final AlarmRepository alarmRepository;
    private final static String ALARM_NAME = "alarm";

    /**
     * SseEmitter 연결 : userId를 key 로 연결
     * @param userId
     * @return sseEmitter
     */
    public SseEmitter connectAlarm(Long userId) {
        SseEmitter sseEmitter = emitterRepository.save(userId);
        sseEmitter.onCompletion(() -> emitterRepository.delete(userId));
        sseEmitter.onTimeout(() -> emitterRepository.delete(userId));
        try {
            sseEmitter.send(SseEmitter.event().id("").name("open").data("connect completed"));
        } catch (IOException e) {
            throw new OlaApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
        }
        return sseEmitter;
    }

    /**
     * SseEmitter 를 통한 알람 전송
     * @param alarmId
     * @param userId
     */
    public void send(Long alarmId, Long userId) {
        emitterRepository.get(userId).ifPresentOrElse(sseEmitter -> {
            try {
                sseEmitter.send(SseEmitter.event().id(alarmId.toString()).name(ALARM_NAME).data("new alarm"));
            } catch (IOException e) {
                emitterRepository.delete(userId);
                throw new OlaApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
            }
        }, () -> log.info("No Emiiter found"));
    }

    /**
     * 알람 조회
     * @param username
     * @return List<AlarmDto>
     */
    @Transactional
    public List<AlarmDto> alarms(String username) {
        return alarmRepository.findByUsername(username)
                .map(alarms -> alarms.stream().map(AlarmDto::fromAlarm)
                        .collect(Collectors.toList())).orElseGet(List::of);
    }

    /**
     * 알람 삭제
     * @param alarmId
     */
    @Transactional
    public void deleteAlarm(Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.ALARM_NOT_FOUND));
        alarmRepository.remove(alarm);
    }
}
