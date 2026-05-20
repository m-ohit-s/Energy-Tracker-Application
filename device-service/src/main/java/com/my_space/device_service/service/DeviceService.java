package com.my_space.device_service.service;

import com.my_space.device_service.dto.DeviceRequestDto;
import com.my_space.device_service.dto.DeviceResponseDto;
import com.my_space.device_service.entity.Device;
import com.my_space.device_service.exception.DeviceNotFoundException;
import com.my_space.device_service.repository.DeviceRepository;
import com.my_space.device_service.service.interfaces.IDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService implements IDeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    public DeviceResponseDto getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with given id"));
        return mapToDeviceResponseDto(device);
    }

    @Override
    public DeviceResponseDto createDevice(DeviceRequestDto deviceRequestDto) {
        final Device device = deviceRepository.save(mapToEntity(deviceRequestDto));
        return mapToDeviceResponseDto(device);
    }

    @Override
    public DeviceResponseDto updateDevice(Long id, DeviceRequestDto deviceRequestDto) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException("Device not found with given id"));
        device.setName(deviceRequestDto.getName());
        device.setId(device.getId());
        device.setLocation(deviceRequestDto.getLocation());
        device.setType(deviceRequestDto.getType());
        device.setUserId(deviceRequestDto.getUserId());
        Device updatedDevice = deviceRepository.save(device);
        return mapToDeviceResponseDto(updatedDevice);
    }

    @Override
    public DeviceResponseDto deleteDevice(Long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException("Device not found with given id"));
        deviceRepository.delete(device);
        return mapToDeviceResponseDto(device);
    }

    @Override
    public List<DeviceResponseDto> getAllDevicesByUserId(Long userId) {
        List<Device> devices = deviceRepository.findAllByUserId(userId);
        return devices.stream().map(this::mapToDeviceResponseDto).toList();
    }

    private DeviceResponseDto mapToDeviceResponseDto(Device device) {
        return DeviceResponseDto.builder()
                .id(device.getId())
                .userId(device.getUserId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .build();
    }

    private Device mapToEntity(DeviceRequestDto deviceRequestDto) {
        return Device.builder()
                .userId(deviceRequestDto.getUserId())
                .type(deviceRequestDto.getType())
                .name(deviceRequestDto.getName())
                .location(deviceRequestDto.getLocation())
                .build();
    }
}
