package com.device.management.controller;


import com.device.management.controller.request.DeviceRequest;
import com.device.management.controller.request.DeviceUpdateRequest;
import com.device.management.controller.response.DeviceResponse;
import com.device.management.exception.GlobalExceptionHandler;
import com.device.management.mapper.ApiMapper;
import com.device.management.service.DeviceUseCase;
import com.device.management.service.dto.*;
import com.device.management.state.DeviceState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.device.management.TestConstants.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DeviceManagementControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private DeviceUseCase useCase;
    private ApiMapper apiMapper;

    @BeforeEach
    void setUp() {
        useCase = Mockito.mock(DeviceUseCase.class);
        apiMapper = Mockito.mock(ApiMapper.class);
        DeviceManagementController controller = new DeviceManagementController(useCase, apiMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("POST /devices returns 201 with created device body")
    void createDevice_http_success() throws Exception {
        // Arrange
        DeviceState state = DeviceState.AVAILABLE;
        UUID id = UUID.fromString(DEVICE_ID);
        OffsetDateTime createdAt = OffsetDateTime.parse(CREATION_TIME);

        when(apiMapper.toCreateCommand(any(DeviceRequest.class)))
                .thenAnswer(mock -> {
                    DeviceRequest request = mock.getArgument(0);
                    return new DeviceCreateCommand(request.name(), request.brand(), request.state());
                });
        when(useCase.create(any(DeviceCreateCommand.class)))
                .thenAnswer(mock -> {
                    DeviceCreateCommand command = mock.getArgument(0);
                    return new DeviceView(id, command.name(), command.brand(), command.state(), createdAt);
                });
        when(apiMapper.toResponse(any(DeviceView.class)))
                .thenAnswer(mock -> {
                    DeviceView view = mock.getArgument(0);
                    return new DeviceResponse(view.id(), view.name(), view.brand(), view.state(), view.creationTime());
                });

        String body = "{" +
                "\"name\":\"" + DEVICE_NAME + "\"," +
                "\"brand\":\"" + DEVICE_BRAND + "\"," +
                "\"state\":\"" + state + "\"" +
                "}";

        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.name", is(DEVICE_NAME)))
                .andExpect(jsonPath("$.brand", is(DEVICE_BRAND)))
                .andExpect(jsonPath("$.state", is(state.name())))
                .andExpect(jsonPath("$.creationTime", notNullValue()));
    }

    @Test
    @DisplayName("POST /devices with missing fields returns 400 Bad Request")
    void createDevice_http_validation_error() throws Exception {
        // missing brand and state
        String body = "{" +
                "\"name\":\"iPhone 14\"" +
                "}";

        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /devices/{id} returns 204 No Content when deleted")
    void deleteDevice_http_success() throws Exception {
        UUID id = UUID.fromString(DEVICE_ID);
        // No stubbing needed; default is do nothing for void

        mockMvc.perform(delete("/devices/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /devices/{id} returns 404 when device not found")
    void deleteDevice_http_notFound() throws Exception {
        UUID id = UUID.fromString(DEVICE_ID);
        doThrow(new NoSuchElementException("Device not found"))
                .when(useCase).delete(id);

        mockMvc.perform(delete("/devices/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /devices/{id} returns 409 when device IN_USE state.")
    void deleteDevice_http_conflict() throws Exception {
        UUID id = UUID.fromString(DEVICE_ID);
        doThrow(new IllegalStateException("Cannot delete a device while it is IN_USE"))
            .when(useCase).delete(id);

        mockMvc.perform(delete("/devices/{id}", id))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /devices/{id} returns 200 with device body")
    void getDevice_http_success() throws Exception {
        UUID id = UUID.fromString(DEVICE_ID);
        String name = DEVICE_NAME;
        String brand = DEVICE_BRAND;
        DeviceState state = DeviceState.AVAILABLE;
        OffsetDateTime createdAt = OffsetDateTime.parse(CREATION_TIME);

        when(useCase.get(id))
                .thenReturn(new DeviceView(id, name, brand, state, createdAt));
        when(apiMapper.toResponse(any(DeviceView.class)))
                .thenAnswer(mock -> {
                    DeviceView deviceView = mock.getArgument(0);
                    return new DeviceResponse(
                            deviceView.id(),
                            deviceView.name(),
                            deviceView.brand(),
                            deviceView.state(),
                            deviceView.creationTime());
                });

        mockMvc.perform(get("/devices/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.brand", is(brand)))
                .andExpect(jsonPath("$.state", is(state.name())))
                .andExpect(jsonPath("$.creationTime", notNullValue()));
    }

    @Test
    @DisplayName("GET /devices/{id} returns 404 when device not found")
    void getDevice_http_notFound() throws Exception {
        UUID id = UUID.fromString(DEVICE_ID);
        when(useCase.get(id)).thenThrow(new NoSuchElementException("Device not found"));

        mockMvc.perform(get("/devices/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /devices/{id} returns 400 with invalid request body")
    void updateFull_invalidRequest_returnsBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        DeviceRequest request = new DeviceRequest("", DEVICE_BRAND, DeviceState.AVAILABLE);

        mockMvc.perform(put("/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /devices/{id} returns 409 device state is already IN_USE")
    void updateFull_serviceThrowsException_returnsError() throws Exception {
        UUID id = UUID.randomUUID();
        DeviceRequest request = new DeviceRequest(DEVICE_NAME, DEVICE_BRAND, DeviceState.AVAILABLE);

        DeviceCreateCommand command = new DeviceCreateCommand(DEVICE_NAME, DEVICE_BRAND, DeviceState.AVAILABLE);
        Mockito.when(apiMapper.toCreateCommand(request)).thenReturn(command);

        Mockito.when(useCase.updateFull(eq(id), eq(command)))
                .thenThrow(new IllegalStateException("Cannot update while IN_USE"));

        mockMvc.perform(put("/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Cannot update while IN_USE"));
    }

    @Test
    @DisplayName("PUT /devices/{id} returns 200 ok")
    void updateFull_success() throws Exception {
        UUID id = UUID.randomUUID();
        DeviceRequest request = new DeviceRequest(NEW_DEVICE_NAME, NEW_DEVICE_BRAND, DeviceState.AVAILABLE);

        DeviceCreateCommand command = new DeviceCreateCommand(NEW_DEVICE_NAME, NEW_DEVICE_BRAND, DeviceState.AVAILABLE);
        Mockito.when(apiMapper.toCreateCommand(request)).thenReturn(command);

        DeviceView view = new DeviceView(
                UUID.fromString(NEW_DEVICE_ID),
                NEW_DEVICE_NAME,
                NEW_DEVICE_BRAND,
                DeviceState.AVAILABLE,
                OffsetDateTime.parse(NEW_CREATION_TIME));
        Mockito.when(useCase.updateFull(eq(id), eq(command))).thenReturn(view);

        DeviceResponse response = new DeviceResponse(
                UUID.fromString(NEW_DEVICE_ID),
                NEW_DEVICE_NAME,
                NEW_DEVICE_BRAND,
                DeviceState.AVAILABLE,
                OffsetDateTime.parse(NEW_CREATION_TIME));
        Mockito.when(apiMapper.toResponse(view)).thenReturn(response);

        mockMvc.perform(put("/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(NEW_DEVICE_ID))
                .andExpect(jsonPath("$.name").value(NEW_DEVICE_NAME))
                .andExpect(jsonPath("$.brand").value(NEW_DEVICE_BRAND))
                .andExpect(jsonPath("$.state").value(DeviceState.AVAILABLE.name()))
                .andExpect(jsonPath("$.creationTime").value(NEW_CREATION_TIME));
    }

    @Test
    void updatePartial_success() throws Exception {
        UUID id = UUID.randomUUID();
        DeviceUpdateRequest request = new DeviceUpdateRequest(NEW_DEVICE_NAME, null, null); // only name changed

        // Mock mapper to command
        DeviceUpdateCommand command = new DeviceUpdateCommand(NEW_DEVICE_NAME, null, null);
        Mockito.when(apiMapper.toUpdateCommand(request)).thenReturn(command);

        // Mock service response
        DeviceView view = new DeviceView(
                UUID.fromString(DEVICE_ID),
                NEW_DEVICE_NAME,
                DEVICE_BRAND,
                DeviceState.AVAILABLE,
                OffsetDateTime.parse(CREATION_TIME));
        Mockito.when(useCase.updatePartial(eq(id), eq(command))).thenReturn(view);

        // Mock mapper to response
        DeviceResponse response = new DeviceResponse(
                UUID.fromString(DEVICE_ID),
                NEW_DEVICE_NAME,
                DEVICE_BRAND,
                DeviceState.AVAILABLE,
                OffsetDateTime.parse(CREATION_TIME));
        Mockito.when(apiMapper.toResponse(view)).thenReturn(response);

        // Perform PATCH request
        mockMvc.perform(patch("/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(NEW_DEVICE_NAME))
                .andExpect(jsonPath("$.brand").value(DEVICE_BRAND))
                .andExpect(jsonPath("$.state").value(DeviceState.AVAILABLE.name()));
    }

    @Test
    void updatePartial_serviceThrowsException_returnsError() throws Exception {
        UUID id = UUID.randomUUID();
        DeviceUpdateRequest request = new DeviceUpdateRequest(NEW_DEVICE_NAME, null, null);

        DeviceUpdateCommand command = new DeviceUpdateCommand(NEW_DEVICE_NAME, null, null);
        Mockito.when(apiMapper.toUpdateCommand(request)).thenReturn(command);

        Mockito.when(useCase.updatePartial(eq(id), eq(command)))
                .thenThrow(new IllegalStateException("Cannot update name/brand while device is IN_USE"));

        mockMvc.perform(patch("/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Cannot update name/brand while device is IN_USE"));
    }

    @Test
    void updatePartial_noChanges_success() throws Exception {
        UUID id = UUID.randomUUID();
        DeviceUpdateRequest request = new DeviceUpdateRequest(null, null, null); // no change

        DeviceUpdateCommand command = new DeviceUpdateCommand(null, null, null);
        Mockito.when(apiMapper.toUpdateCommand(request)).thenReturn(command);

        DeviceView view = new DeviceView(
                UUID.fromString(DEVICE_ID),
                DEVICE_NAME,
                DEVICE_BRAND,
                DeviceState.AVAILABLE,
                OffsetDateTime.parse(CREATION_TIME));
        Mockito.when(useCase.updatePartial(eq(id), eq(command))).thenReturn(view);

        DeviceResponse response = new DeviceResponse(
                UUID.fromString(DEVICE_ID),
                DEVICE_NAME,
                DEVICE_BRAND,
                DeviceState.AVAILABLE,
                OffsetDateTime.parse(CREATION_TIME));
        Mockito.when(apiMapper.toResponse(view)).thenReturn(response);

        mockMvc.perform(patch("/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(DEVICE_NAME))
                .andExpect(jsonPath("$.brand").value(DEVICE_BRAND))
                .andExpect(jsonPath("$.state").value(DeviceState.AVAILABLE.name()));
    }

    @Test
    void list_noFilter_success() throws Exception {
        // Mock service response
        DeviceView view1 = new DeviceView(
                UUID.fromString(DEVICE_ID),
                DEVICE_NAME,
                DEVICE_BRAND,
                DeviceState.AVAILABLE,
                OffsetDateTime.parse(CREATION_TIME));
        DeviceView view2 = new DeviceView(
                UUID.fromString(NEW_DEVICE_ID),
                NEW_DEVICE_NAME,
                NEW_DEVICE_BRAND,
                DeviceState.AVAILABLE,
                OffsetDateTime.parse(NEW_CREATION_TIME));

        PageResult<DeviceView> result = new PageResult<>(
                List.of(view1, view2),
                0,
                20,
                2,
                1,
                true,
                true
        );

        when(useCase.list(any(DeviceFilter.class), any()))
                .thenReturn(result);

        // Mock mapper
        Mockito.when(apiMapper.toResponse(view1))
                .thenReturn(new DeviceResponse(
                        UUID.fromString(DEVICE_ID),
                        DEVICE_NAME,
                        DEVICE_BRAND,
                        DeviceState.AVAILABLE,
                        OffsetDateTime.parse(CREATION_TIME)));
        Mockito.when(apiMapper.toResponse(view2))
                .thenReturn(new DeviceResponse(
                        UUID.fromString(NEW_DEVICE_ID),
                        NEW_DEVICE_NAME,
                        NEW_DEVICE_BRAND,
                        DeviceState.AVAILABLE,
                        OffsetDateTime.parse(NEW_CREATION_TIME)));

        mockMvc.perform(get("/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value(DEVICE_NAME))
                .andExpect(jsonPath("$.content[0].brand").value(DEVICE_BRAND))
                .andExpect(jsonPath("$.content[0].state").value(DeviceState.AVAILABLE.name()))
                .andExpect(jsonPath("$.content[1].name").value(NEW_DEVICE_NAME))
                .andExpect(jsonPath("$.content[1].brand").value(NEW_DEVICE_BRAND))
                .andExpect(jsonPath("$.content[1].state").value((DeviceState.AVAILABLE.name())));
    }

    @Test
    void list_pagination_success() throws Exception {
        DeviceView view = new DeviceView(
                UUID.fromString(NEW_DEVICE_ID),
                NEW_DEVICE_NAME,
                NEW_DEVICE_BRAND,
                DeviceState.INACTIVE,
                OffsetDateTime.parse(NEW_CREATION_TIME));
        PageResult<DeviceView> result = new PageResult<>(
                List.of(view),
                1,
                5,
                6,
                2,
                false,
                false);

        Mockito.when(useCase.list(eq(new DeviceFilter(null, null)), any())).thenReturn(result);
        Mockito.when(apiMapper.toResponse(view))
                .thenReturn(new DeviceResponse(
                        UUID.fromString(NEW_DEVICE_ID),
                        NEW_DEVICE_NAME,
                        NEW_DEVICE_BRAND,
                        DeviceState.INACTIVE,
                        OffsetDateTime.parse(NEW_CREATION_TIME)));

        mockMvc.perform(get("/devices")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "creationTime,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value(NEW_DEVICE_NAME))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2));
    }
}
