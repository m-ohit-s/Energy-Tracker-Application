package com.my_space.device_service.dto;

import com.my_space.device_service.model.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceRequestDto {
    private String name;
    private DeviceType type;
    private String location;
    private Long userId;
}
