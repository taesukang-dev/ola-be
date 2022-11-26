package com.example.ola.service;

import com.example.ola.domain.*;
import com.example.ola.dto.AlarmDto;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.fixture.Fixture;
import com.example.ola.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
class AlarmServiceTest {
    @Autowired AlarmService alarmService;
    @MockBean AlarmRepository alarmRepository;
    @MockBean EmitterRepository emitterRepository;

    @Test
    void 알람_연결() throws Exception {
        // given
        SseEmitter emitter = mock(SseEmitter.class);
        // when
        when(emitterRepository.save(anyLong())).thenReturn(emitter);
        // then
        SseEmitter founded = alarmService.connectAlarm(1L);
        assertThat(emitter).isEqualTo(founded);
    }

    @Test
    void 알람_연결시_실패한_경우() throws Exception {
        // given
        SseEmitter emitter = mock(SseEmitter.class);
        when(emitterRepository.save(anyLong())).thenReturn(emitter);
        // when
        doThrow(OlaApplicationException.class)
                .when(emitter)
                .send(any());
        // then
        assertThatThrownBy(() -> alarmService.connectAlarm(1L))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 알람_전송() throws Exception {
        // given
        SseEmitter emitter = mock(SseEmitter.class);
        // when
        when(emitterRepository.get(1L)).thenReturn(Optional.of(emitter));
        // then
        alarmService.send(anyLong(), 1L);
        verify(emitter).send(any());
        verify(emitterRepository).get(anyLong());
    }

    @Test
    void 알림_전송_실패한_경우() throws Exception {
        // given
        SseEmitter emitter = mock(SseEmitter.class);
        // when
        when(emitterRepository.get(anyLong())).thenReturn(Optional.of(emitter));
        doThrow(OlaApplicationException.class)
                .when(emitter)
                .send(any());
        // then
        assertThatThrownBy(() -> alarmService.send(1L, anyLong()))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 알람_조회() throws Exception {
        // given
        List<Alarm> alarms = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Alarm alarm = Alarm.of(
                    Fixture.makeUserFixture("user" + i, "password1"),
                    AlarmArgs.of(1L, "user" + (i * 2 + 1)),
                    AlarmType.COMMENT);
            alarms.add(alarm);
        }
        // when
        when(alarmRepository.findByUsername(any())).thenReturn(Optional.of(alarms));
        // then
        List<AlarmDto> alarmDtos = alarmService.alarms("user1");
        assertThat(alarmDtos.size()).isEqualTo(10);
    }

    @Test
    void 알람_조회시_알람이_없는_경우() throws Exception {
        // given
        // when
        when(alarmRepository.findByUsername(any())).thenReturn(Optional.empty());
        // then
        List<AlarmDto> user1 = alarmService.alarms("user1");
        assertThat(user1.size()).isEqualTo(0);
    }

    @Test
    void 알람_삭제() throws Exception {
        // given
        Alarm alarm = Alarm.of(
                Fixture.makeUserFixture("user", "password1"),
                AlarmArgs.of(1L, "user"),
                AlarmType.COMMENT);
        // when
        when(alarmRepository.findById(anyLong())).thenReturn(Optional.of(alarm));
        // then
        alarmService.deleteAlarm(1L);
        verify(alarmRepository).remove(alarm);
    }

    @Test
    void 알람_삭제시_알람이_없는_경우() throws Exception {
        // given
        // when
        when(alarmRepository.findById(anyLong())).thenThrow(new OlaApplicationException(ErrorCode.ALARM_NOT_FOUND));
        // then
        assertThatThrownBy(() -> alarmService.deleteAlarm(1L))
                .isInstanceOf(OlaApplicationException.class);
    }
}