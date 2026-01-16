package com.device.management.controller;

import com.device.management.controller.request.DeviceRequest;
import com.device.management.controller.request.DeviceUpdateRequest;
import com.device.management.controller.response.DeviceResponse;
import com.device.management.mapper.ApiMapper;
import com.device.management.service.DeviceUseCase;
import com.device.management.service.dto.*;
import com.device.management.state.DeviceState;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
public class DeviceManagementController {
    private static final Set<String> ALLOWED_SORTS = Set.of("name", "brand", "state", "creationTime");

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

    @PutMapping("/{id}")
    public DeviceResponse updateFull(@PathVariable UUID id, @Valid @RequestBody DeviceRequest request) {
        var deviceCreateCommand = apiMapper.toCreateCommand(request);
        var deviceView = useCase.updateFull(id, deviceCreateCommand);
        return apiMapper.toResponse(deviceView);
    }

    @PatchMapping("/{id}")
    public DeviceResponse updatePartial(@PathVariable UUID id, @RequestBody DeviceUpdateRequest request) {
        var deviceCreateCommand = apiMapper.toUpdateCommand(request);
        var deviceView = useCase.updatePartial(id, deviceCreateCommand);
        return apiMapper.toResponse(deviceView);
    }

    @GetMapping("/{id}")
    public DeviceResponse get(@PathVariable UUID id) {
        var deviceView = useCase.get(id);
        return apiMapper.toResponse(deviceView);
    }

    @GetMapping
    public Page<DeviceResponse> list(@RequestParam(required = false) String brand,
                                     @RequestParam(required = false) DeviceState state,
                                     @PageableDefault(
                                             size = 20, sort = "creationTime", direction = Sort.Direction.DESC) Pageable pageable) {
        validateSort(pageable);
        DeviceFilter filter = new DeviceFilter(brand, state);
        PageRequest pageRequest = toPageRequest(pageable);
        PageResult<DeviceView> result = useCase.list(filter, pageRequest);
        var items = result.items().stream().map(apiMapper::toResponse).toList();
        return new PageImpl<>(items, pageable, result.totalItems());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        useCase.delete(id);
    }

    private void validateSort(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!ALLOWED_SORTS.contains(order.getProperty())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Unsupported sort property: " + order.getProperty());
            }
        }
    }

    private PageRequest toPageRequest(Pageable pageable) {
        var orders = pageable.getSort().stream()
                .map(order -> new SortOrder(
                        order.getProperty(), order.getDirection().isDescending()
                        ? SortOrder.Direction.DESC
                        : SortOrder.Direction.ASC))
                .toList();
        return new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), orders);
    }
}
