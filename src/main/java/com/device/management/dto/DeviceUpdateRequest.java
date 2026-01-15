package com.device.management.dto;

import com.device.management.state.DeviceState;

public record DeviceUpdateRequest(
        String name,
        String brand,
        DeviceState state
) {}
