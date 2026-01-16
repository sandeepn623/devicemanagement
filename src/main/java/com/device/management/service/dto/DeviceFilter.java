package com.device.management.service.dto;

import com.device.management.state.DeviceState;

public record DeviceFilter(
        String brand,
        DeviceState state
) {}
