package com.device.management.service.dto;

import com.device.management.state.DeviceState;

public record DeviceUpdateCommand(
        String name,
        String brand,
        DeviceState state
) {}
