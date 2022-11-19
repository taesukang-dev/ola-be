package com.example.ola.controller;

import com.example.ola.dto.response.AlarmResponse;
import com.example.ola.dto.response.Response;
import com.example.ola.dto.security.UserPrincipal;
import com.example.ola.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RequestMapping("/api/v1/alarms")
@RestController
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public Response<List<AlarmResponse>> alarmList(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(alarmService.alarms(userPrincipal.getUsername())
                .stream().map(AlarmResponse::fromAlarmDto)
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/{alarmId}")
    public Response<Void> deleteAlarm(@PathVariable Long alarmId) {
        alarmService.deleteAlarm(alarmId);
        return Response.success();
    }
}
