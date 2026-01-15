package com.device.management.application;

import java.util.UUID;

public interface DeviceUseCase {
    DeviceView create(DeviceCreateCommand cmd);
    DeviceView updateFull(UUID id, DeviceCreateCommand cmd);
    DeviceView updatePartial(UUID id, DeviceUpdateCommand cmd);
    DeviceView get(UUID id);
    PageResult<DeviceView> list(DeviceFilter filter, PageRequest pageRequest);
    void delete(UUID id);
}
