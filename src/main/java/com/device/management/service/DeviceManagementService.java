package com.device.management.service;


import com.device.management.repository.entity.Device;
import com.device.management.mapper.DeviceMapper;
import com.device.management.repository.DeviceRepository;
import com.device.management.service.dto.*;
import com.device.management.state.DeviceState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class DeviceManagementService implements DeviceUseCase {
    private final DeviceRepository repository;
    private final DeviceMapper mapper;

    public DeviceManagementService(DeviceRepository repository, DeviceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public DeviceView create(DeviceCreateCommand deviceCreateCommand) {
        Device device = mapper.toEntity(deviceCreateCommand);
        Device saved = repository.saveAndFlush(device);
        return mapper.toView(saved);
    }

    @Override
    public DeviceView updateFull(UUID id, DeviceCreateCommand cmd) {
        return null;
    }

    @Override
    public DeviceView updatePartial(UUID id, DeviceUpdateCommand deviceUpdateCommand) {
        Device device = repository.findById(id).orElseThrow(() -> notFound(id));
        boolean wantsNameChange = deviceUpdateCommand.name() != null;
        boolean wantsBrandChange = deviceUpdateCommand.brand() != null;
        if (device.getState() == DeviceState.IN_USE && (wantsNameChange || wantsBrandChange)) {
            throw new IllegalStateException("Cannot update name/brand while device is IN_USE");
        }
        mapper.update(device, deviceUpdateCommand);
        return mapper.toView(device);
    }

    @Override
    public DeviceView get(UUID id) {
        return null;
    }

    @Override
    public PageResult<DeviceView> list(DeviceFilter filter, PageRequest pageRequest) {
        return null;
    }

    @Override
    public void delete(UUID id) {
        Device device = repository.findById(id).orElseThrow(() -> notFound(id));
        if (device.getState() == DeviceState.IN_USE) {
            throw new IllegalStateException("Cannot delete a device while it is IN_USE");
        }
        repository.deleteById(id);
    }

    private RuntimeException notFound(UUID id) {
        return new NoSuchElementException("Device not found: " + id);
    }
}
