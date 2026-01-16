package com.device.management.controller.response;


import com.device.management.state.DeviceState;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        String name,
        String brand,
        DeviceState state,
        OffsetDateTime creationTime
) {}
