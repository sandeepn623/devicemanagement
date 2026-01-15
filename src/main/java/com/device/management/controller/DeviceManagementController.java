package com.device.management.controller;

import com.device.management.application.DeviceUseCase;
import com.device.management.dto.DeviceRequest;
import com.device.management.dto.DeviceResponse;
import com.device.management.mapper.ApiMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
        var cmd = apiMapper.toCreateCommand(request);
        var view = useCase.create(cmd);
        return apiMapper.toResponse(view);
    }
}
