package com.my_space.usage_service.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import com.my_space.kafka.event.AlertingEvent;
import com.my_space.kafka.event.EnergyUsageEvent;
import com.my_space.usage_service.client.DeviceClient;
import com.my_space.usage_service.client.UserClient;
import com.my_space.usage_service.dto.DeviceDto;
import com.my_space.usage_service.dto.UsageDto;
import com.my_space.usage_service.dto.UserDto;
import com.my_space.usage_service.model.DeviceEnergy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsageService {

    private final InfluxDBClient influxDBClient;

    @Value("${influx.org}")
    private String influxOrg;

    @Value("${influx.bucket}")
    private String influxBucket;

    private final DeviceClient deviceClient;
    private final UserClient userClient;
    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    public UsageService(InfluxDBClient influxDBClient, DeviceClient deviceClient, UserClient userClient, KafkaTemplate<String, AlertingEvent> kafkaTemplate) {
        this.influxDBClient = influxDBClient;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent (EnergyUsageEvent energyUsageEvent) {
        log.info("Received energy usage event: {}", energyUsageEvent);
        Point point = Point.measurement("energy-usage")
                .addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);
        influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = Instant.now().minusSeconds(3600);

        String fluxQuery = String.format("""
        from(bucket: "%s")
        |> range(start: time(v: "%s"), stop: time(v: "%s"))
        |> filter(fn: (r) => r["_measurement"] == "energy-usage")
        |> filter(fn: (r) => r["_field"] == "energyConsumed")
        |> group(columns: ["deviceId"])
        |> sum(column: "_value")
        """, influxBucket, oneHourAgo, now);

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);

        List<DeviceEnergy> deviceEnergies = tables.stream().flatMap(fluxTable -> fluxTable.getRecords().stream().map(fluxRecord -> {
            String deviceIdStr = (String) fluxRecord.getValueByKey("deviceId");
            double energyConsumed = fluxRecord.getValueByKey("_value") instanceof Number ? ((Number) fluxRecord.getValueByKey("_value")).doubleValue() : 0.0;
            return DeviceEnergy.builder()
                    .deviceId(Long.valueOf(deviceIdStr))
                    .energyConsumed(energyConsumed)
                    .build();
        })).collect(Collectors.toList());

        log.info("Aggregate device energy of past hour: {}", deviceEnergies);

        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            final DeviceDto deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());
            if (deviceResponse == null || deviceResponse.getId() == null) {
                log.warn("Device with id {} has no device", deviceEnergy.getDeviceId());
                continue;
            }
            deviceEnergy.setUserId(deviceResponse.getUserId());
        }

        deviceEnergies.removeIf(de -> de.getUserId() == null);

        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap = deviceEnergies.stream().collect(Collectors.groupingBy(DeviceEnergy::getUserId));
        log.info("User Device Energy Map: {}", userDeviceEnergyMap);

        List<Long> userIDs = userDeviceEnergyMap.keySet().stream().toList();
        Map<Long, Double> userThresholdMap = new HashMap<>();
        Map<Long, String> userEmailMap = new HashMap<>();
        userIDs.forEach(userId -> {
            try {
                UserDto user = userClient.getUserById(userId);
                if (user == null || user.id() == null || !user.alerting()) {
                    log.warn("User with id {} has no alerting or does not exists", userId);
                } else  {
                    userThresholdMap.put(user.id(), user.energyAlertingThreshold());
                    userEmailMap.put(user.id(), user.email());

                    List<Long> alertedUsers = userThresholdMap.keySet().stream().toList();
                    alertedUsers.forEach(alertedUser -> {
                        final double userThreshold = userThresholdMap.get(alertedUser);
                        final List<DeviceEnergy> devices = userDeviceEnergyMap.get(alertedUser);

                        final double totalConsumption = devices.stream().mapToDouble(DeviceEnergy::getEnergyConsumed).sum();

                        if (totalConsumption > userThreshold) {
                            log.warn("User with id {} has reached threshold: {}: {}", alertedUser, totalConsumption, userThreshold);

                            final AlertingEvent alertingEvent = AlertingEvent.builder()
                                    .userId(alertedUser)
                                    .energyConsumed(totalConsumption)
                                    .threshold(userThreshold)
                                    .email(userEmailMap.get(alertedUser))
                                    .message("Energy Consumption Threshold exceeded")
                                    .build();
                            kafkaTemplate.send("energy-alert", alertingEvent);
                        } else {
                            log.info("User dis within threshold");
                        }
                    });
                }
            } catch (Exception e) {
                log.error("Something went wrong with {}: {}", userId, e.getMessage());
            }
        });

    }

    public UsageDto getXDaysUsageForUser(Long userId, int days) {
        log.info("Get {} Days usage for user {}",days, userId);
        final List<DeviceDto> devices = deviceClient.getAllDevicesForUser(userId);
        if (devices == null || devices.isEmpty()) {
            return UsageDto.builder()
                    .devices(List.of())
                    .userId(userId)
                    .build();
        }
        List<String> deviceIdStr = devices.stream()
                .map(DeviceDto::getId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();

        final Instant now = Instant.now();
        final Instant end = Instant.now().minusSeconds((long) days * 24 * 60 * 60);
        final String deviceFilter = deviceIdStr.stream()
                .map(idStr -> String.format("r[\"deviceId\"] == \"%s\"", idStr))
                .collect(Collectors.joining(" or "));

        String fluxQuery = String.format("""
        from(bucket: "%s")
            |> range(start: time(v: %s), stop: time(v: %s))
            |> filter(fn: (r) => r["_measurement"] == "energy-usage")
            |> filter(fn: (r) => r["_field"] == "energyConsumed")
            |> filter(fn: (r) => %s)
            |> group(columns: ["deviceId"])
            |> sum(column: "_value")
        """, influxBucket, end.toString(), now, deviceFilter);

        final Map<Long, Double> aggregatedMap = new HashMap<>();
        try{
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> fluxTables = queryApi.query(fluxQuery, influxOrg);
            fluxTables.forEach(fluxTable -> {
                fluxTable.getRecords().forEach(fluxRecord -> {
                    Object deviceIdObj = fluxRecord.getValueByKey("deviceId");
                    String deviceIdString = deviceIdObj != null ? deviceIdObj.toString() : null;
                    if (deviceIdString != null) {
                        double energyConsumed = fluxRecord.getValueByKey("_value") instanceof Number ? ((Number) fluxRecord.getValueByKey("_value")).doubleValue() : 0.0;
                        try {
                            Long deviceId = Long.valueOf(deviceIdString);
                            aggregatedMap.put(deviceId, aggregatedMap.getOrDefault(deviceId, 0.0) + energyConsumed);
                        } catch (NumberFormatException e) {
                            log.error("Failed to parse device id from flux record: {}", deviceIdString);
                        }
                    }
                });
            });
        } catch (Exception e) {
            log.error("Failed to query influx DB for user {} usage over {} days: {}",userId,days, e.getMessage());
            devices.forEach(device -> device.setEnergyConsumed(0.0));
            return UsageDto.builder()
                    .userId(userId)
                    .devices(devices)
                    .build();
        }

        List<DeviceDto> filteredDevices = devices.stream()
                .filter(deviceDto -> deviceDto != null && deviceDto.getId() != null)
                .map(deviceDto -> {
                    deviceDto.setEnergyConsumed(aggregatedMap.getOrDefault(deviceDto.getId(), 0.0));
                    return deviceDto;
                })
                .toList();

        log.info("Aggregated Energy Consumption for userId: {}: {}",userId, aggregatedMap);
        return UsageDto.builder()
                .userId(userId)
                .devices(filteredDevices)
                .build();
    }
}
