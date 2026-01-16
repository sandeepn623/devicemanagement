package com.device.management.service;


import com.device.management.repository.entity.Device;
import com.device.management.mapper.DeviceMapper;
import com.device.management.repository.DeviceRepository;
import com.device.management.service.dto.*;
import com.device.management.state.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class DeviceManagementService implements DeviceUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceManagementService.class);

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
        Device device = repository.findById(id).orElseThrow(() -> notFound(id));

        boolean nameChanged = !cmd.name().equals(device.getName());
        boolean brandChanged = !cmd.brand().equals(device.getBrand());
        boolean stateChanged = !cmd.state().equals(device.getState());

        int changedCount = 0;
        if (nameChanged) changedCount++;
        if (brandChanged) changedCount++;
        if (stateChanged) changedCount++;

        boolean allUnchanged = changedCount == 0;
        boolean allChanged = changedCount == 3;
        LOGGER.info("deviceId: {}, {} changed and {} all unchanged", id, allUnchanged, allChanged);
        if (device.getState() == DeviceState.IN_USE && (nameChanged || brandChanged)) {
            throw new IllegalStateException("Cannot update name/brand while device is IN_USE");
        }

        if (!(allUnchanged || allChanged)) {
            throw new IllegalStateException(
                    "PUT must either replace the entire resource or make no changes at all"
            );
        }

        device.setName(cmd.name());
        device.setBrand(cmd.brand());
        device.setState(cmd.state());
        return mapper.toView(device);
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
    @Transactional(readOnly = true)
    public DeviceView get(UUID id) {
        Device device = repository.findById(id).orElseThrow(() -> notFound(id));
        return mapper.toView(device);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<DeviceView> list(DeviceFilter filter, PageRequest pageRequest) {
        Pageable pageable = toSpringPageable(pageRequest);
        Page<Device> page;
        String brand = filter != null ? filter.brand() : null;
        DeviceState state = filter != null ? filter.state() : null;
        if (brand != null && state != null) {
            page = repository.findByBrandIgnoreCaseAndState(brand, state, pageable);
        } else if (brand != null) {
            page = repository.findByBrandIgnoreCase(brand, pageable);
        } else if (state != null) {
            page = repository.findByState(state, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        var items = page.map(mapper::toView).getContent();
        return new PageResult<>(items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }

    @Override
    public void delete(UUID id) {
        Device device = repository.findById(id).orElseThrow(() -> notFound(id));
        if (device.getState() == DeviceState.IN_USE) {
            throw new IllegalStateException("Cannot delete a device while it is IN_USE");
        }
        repository.deleteById(id);
    }

    private Pageable toSpringPageable(PageRequest pageRequest) {
        if (pageRequest == null) {
            return Pageable.unpaged();
        }
        Sort sort = Sort.unsorted();
        if (pageRequest.sort() != null && !pageRequest.sort().isEmpty()) {
            java.util.List<Sort.Order> orders = new ArrayList<>();
            for (SortOrder sortOrder : pageRequest.sort()) {
                Sort.Direction direction =
                        (sortOrder.direction() == SortOrder.Direction.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
                orders.add(new Sort.Order(direction, sortOrder.property()));
            }
            sort = Sort.by(orders);
        }
        return org.springframework.data.domain.PageRequest
                .of(Math.max(pageRequest.page(), 0), Math.max(pageRequest.size(), 1), sort);
    }

    private RuntimeException notFound(UUID id) {
        return new NoSuchElementException("Device not found: " + id);
    }
}
