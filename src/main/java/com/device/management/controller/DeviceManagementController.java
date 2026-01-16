package com.device.management.controller;

import com.device.management.controller.request.DeviceRequest;
import com.device.management.controller.request.DeviceUpdateRequest;
import com.device.management.controller.response.DeviceResponse;
import com.device.management.mapper.ApiMapper;
import com.device.management.service.DeviceUseCase;
import com.device.management.service.dto.*;
import com.device.management.state.DeviceState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Create a device",
            description = "Creates a new device with name, brand and state"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse create(@Valid @RequestBody DeviceRequest request) {
        var deviceCreateCommand = apiMapper.toCreateCommand(request);
        var deviceView = useCase.create(deviceCreateCommand);
        return apiMapper.toResponse(deviceView);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Fully replace a device",
            description = """
            Fully replaces a device.
            Rules:
            - Either all fields change or none change
            - Name and brand cannot be changed while device is IN_USE
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device replaced"),
            @ApiResponse(responseCode = "400", description = "Invalid update"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "Illegal state transition")
    })
    public DeviceResponse updateFull(@PathVariable UUID id, @Valid @RequestBody DeviceRequest request) {
        var deviceCreateCommand = apiMapper.toCreateCommand(request);
        var deviceView = useCase.updateFull(id, deviceCreateCommand);
        return apiMapper.toResponse(deviceView);
    }
    @PatchMapping("/{id}")
    @Operation(
            summary = "Partially update a device",
            description = """
            Updates only the provided fields.
            Rules:
            - Name and brand cannot be updated while device is IN_USE
            - State-only changes are allowed
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device updated"),
            @ApiResponse(responseCode = "400", description = "Invalid update"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "Illegal state transition")
    })
    public DeviceResponse updatePartial(@PathVariable UUID id, @RequestBody DeviceUpdateRequest request) {
        var deviceCreateCommand = apiMapper.toUpdateCommand(request);
        var deviceView = useCase.updatePartial(id, deviceCreateCommand);
        return apiMapper.toResponse(deviceView);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get device by ID",
            description = "Returns a single device by its UUID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public DeviceResponse get(@PathVariable UUID id) {
        var deviceView = useCase.get(id);
        return apiMapper.toResponse(deviceView);
    }

    @GetMapping
    @Operation(
            summary = "List devices",
            description = """
            Returns a paginated list of devices.
            Optional filters:
            - brand (case-insensitive)
            - state (AVAILABLE, IN_USE, INACTIVE)
            """
    )
    public Page<DeviceResponse> list(
            @Parameter(
                    description = "Filter devices by brand (case-insensitive)",
                    example = "Samsung",
                    required = false
            )
            @RequestParam(required = false) String brand,

            @Parameter(
                    description = "Filter devices by state",
                    schema = @Schema(implementation = DeviceState.class),
                    example = "AVAILABLE",
                    required = false
            )
            @RequestParam(required = false) DeviceState state,

            @Parameter(hidden = true)
            @PageableDefault(
                    size = 20,
                    sort = "creationTime",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {
        validateSort(pageable);
        DeviceFilter filter = new DeviceFilter(brand, state);
        PageRequest pageRequest = toPageRequest(pageable);
        PageResult<DeviceView> result = useCase.list(filter, pageRequest);
        var items = result.items().stream().map(apiMapper::toResponse).toList();
        return new PageImpl<>(items, pageable, result.totalItems());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a device",
            description = "Deletes a device by ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Device deleted"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
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
