package com.example.ola.dto;

import com.example.ola.domain.HomeGym;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeGymDto {
    private Long id;
    private String placeName;
    private String roadAddressName;
    private String categoryName;
    private Double x;
    private Double y;

    public static HomeGymDto fromHomeGym(HomeGym homeGym) {
        return new HomeGymDto(
                homeGym.getId(),
                homeGym.getPlaceName(),
                homeGym.getRoadAddressName(),
                homeGym.getCategoryName(),
                homeGym.getX(),
                homeGym.getY());
    }
}
