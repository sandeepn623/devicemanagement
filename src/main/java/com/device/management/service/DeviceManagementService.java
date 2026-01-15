package com.device.management.service;

import com.device.management.mapper.DeviceMapper;
import com.device.management.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeviceManagementService {
    private final DeviceRepository repository;
    private final DeviceMapper mapper;

    public DeviceManagementService(DeviceRepository repository, DeviceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
}
