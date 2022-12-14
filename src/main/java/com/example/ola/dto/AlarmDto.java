package com.example.ola.dto;

import com.example.ola.domain.Alarm;
import com.example.ola.domain.AlarmType;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlarmDto {
    private Long id;
    private String receivedUsername;
    private String fromUsername;
    private Long postId;
    private AlarmType alarmType;

    public static AlarmDto of(Long id, String receivedUsername, String fromUsername, Long postId, AlarmType alarmType) {
        return new AlarmDto(id, receivedUsername, fromUsername, postId, alarmType);
    }

    public static AlarmDto fromAlarm(Alarm alarm) {
        return new AlarmDto(
                alarm.getId(),
                alarm.getUser().getUsername(),
                alarm.getArgs().getFromUser(),
                alarm.getArgs().getPostId(),
                alarm.getAlarmType()
        );
    }
}
