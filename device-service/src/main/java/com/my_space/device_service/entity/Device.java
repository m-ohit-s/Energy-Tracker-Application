package com.my_space.device_service.entity;

import com.my_space.device_service.model.DeviceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "device")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    private String location;
    private Long userId;

}
