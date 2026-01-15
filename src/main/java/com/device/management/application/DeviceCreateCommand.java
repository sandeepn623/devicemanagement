package com.device.management.application;

import com.device.management.state.DeviceState;

public record DeviceCreateCommand(
        String name,
        String brand,
        DeviceState state
) {}

