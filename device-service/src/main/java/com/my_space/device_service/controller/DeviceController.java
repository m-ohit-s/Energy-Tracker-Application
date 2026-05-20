package com.my_space.device_service.controller;

import com.my_space.device_service.dto.DeviceRequestDto;
import com.my_space.device_service.dto.DeviceResponseDto;
import com.my_space.device_service.service.interfaces.IDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/device")
@RequiredArgsConstructor
public class DeviceController {

    private final IDeviceService deviceService;

    @GetMapping("/{id}")
    ResponseEntity<DeviceResponseDto> getDeviceById(@PathVariable Long id) {
        DeviceResponseDto dto = deviceService.getDeviceById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    ResponseEntity<DeviceResponseDto> createDevice(@RequestBody DeviceRequestDto deviceRequestDto) {
        DeviceResponseDto response = deviceService.createDevice(deviceRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    ResponseEntity<DeviceResponseDto> updateDevice(@PathVariable Long id, @RequestBody DeviceRequestDto deviceRequestDto) {
        DeviceResponseDto updatedDevice = deviceService.updateDevice(id, deviceRequestDto);
        return ResponseEntity.ok(updatedDevice);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<DeviceResponseDto> deleteDevice(@PathVariable Long id) {
        DeviceResponseDto deletedDevice = deviceService.deleteDevice(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(deletedDevice);
    }

    @GetMapping("/user/{userId}")
    ResponseEntity<List<DeviceResponseDto>> getAllDevicesForUser(@PathVariable Long userId) {
        List<DeviceResponseDto> devices = deviceService.getAllDevicesByUserId(userId);
        return ResponseEntity.ok(devices);
    }
}
