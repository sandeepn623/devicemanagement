package com.device.management.application;

import java.util.UUID;

public interface DeviceUseCase {
    DeviceView create(DeviceCreateCommand deviceCreateCommand);
    DeviceView updateFull(UUID id, DeviceCreateCommand deviceCreateCommand);
    DeviceView updatePartial(UUID id, DeviceUpdateCommand deviceUpdateCommand);
    DeviceView get(UUID id);
    PageResult<DeviceView> list(DeviceFilter filter, PageRequest pageRequest);
    void delete(UUID id);
}
