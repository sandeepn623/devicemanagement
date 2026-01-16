package com.device.management.service.dto;

import com.device.management.state.DeviceState;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeviceView(
        UUID id,
        String name,
        String brand,
        DeviceState state,
        OffsetDateTime creationTime
) {}
