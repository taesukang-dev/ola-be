package com.example.ola.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeGymRequest {
    private String placeName;
    private String roadAddressName;
    private String categoryName;
    double x;
    double y;
}
