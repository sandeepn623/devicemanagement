package com.device.management.application;

import com.device.management.state.DeviceState;

public record DeviceUpdateCommand(
        String name,
        String brand,
        DeviceState state
) {}
