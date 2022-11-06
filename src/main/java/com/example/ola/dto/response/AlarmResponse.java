package com.example.ola.dto.response;

import com.example.ola.dto.AlarmDto;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlarmResponse {
    private Long id;
    private String receivedUsername;
    private String fromUsername;
    private Long postId;

    public static AlarmResponse fromAlarmDto(AlarmDto alarmDto) {
        return new AlarmResponse(
                alarmDto.getId(),
                alarmDto.getReceivedUsername(),
                alarmDto.getFromUsername(),
                alarmDto.getId()
        );
    }
}
