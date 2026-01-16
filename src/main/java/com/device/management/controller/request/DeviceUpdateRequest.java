package com.device.management.controller.request;

import com.device.management.state.DeviceState;

public record DeviceUpdateRequest(
        String name,
        String brand,
        DeviceState state
) {}
