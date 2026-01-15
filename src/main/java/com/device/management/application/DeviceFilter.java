package com.device.management.application;

import com.device.management.state.DeviceState;

public record DeviceFilter(
        String brand,
        DeviceState state
) {}
