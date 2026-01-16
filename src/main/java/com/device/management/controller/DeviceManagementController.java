package com.device.management.controller;

import com.device.management.application.DeviceUseCase;
import com.device.management.dto.DeviceRequest;
import com.device.management.dto.DeviceResponse;
import com.device.management.mapper.ApiMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/devices")
public class DeviceManagementController {

    private final DeviceUseCase useCase;
    private final ApiMapper apiMapper;

    public DeviceManagementController(DeviceUseCase useCase, ApiMapper apiMapper) {
        this.useCase = useCase;
        this.apiMapper = apiMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse create(@Valid @RequestBody DeviceRequest request) {
        var deviceCreateCommand = apiMapper.toCreateCommand(request);
        var deviceView = useCase.create(deviceCreateCommand);
        return apiMapper.toResponse(deviceView);
    }

    @GetMapping("/{id}")
    public DeviceResponse get(@PathVariable UUID id) {
        var deviceView = useCase.get(id);
        return apiMapper.toResponse(deviceView);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        useCase.delete(id);
    }
}
