package com.my_space.device_service.service.interfaces;

import com.my_space.device_service.dto.DeviceRequestDto;
import com.my_space.device_service.dto.DeviceResponseDto;

import java.util.List;

public interface IDeviceService {
    DeviceResponseDto getDeviceById(Long id);

    DeviceResponseDto createDevice(DeviceRequestDto deviceRequestDto);

    DeviceResponseDto updateDevice(Long id, DeviceRequestDto deviceRequestDto);

    DeviceResponseDto deleteDevice(Long id);

    List<DeviceResponseDto> getAllDevicesByUserId(Long userId);
}
