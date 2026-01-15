package com.device.management.service;

import com.device.management.application.*;
import com.device.management.entity.Device;
import com.device.management.mapper.DeviceMapper;
import com.device.management.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Device saved = repository.save(device);
        return mapper.toView(saved);
    }

    @Override
    public DeviceView updateFull(UUID id, DeviceCreateCommand cmd) {
        return null;
    }

    @Override
    public DeviceView updatePartial(UUID id, DeviceUpdateCommand cmd) {
        return null;
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

    }
}
