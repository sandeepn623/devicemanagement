package com.device.management.controller;


import com.device.management.application.DeviceCreateCommand;
import com.device.management.application.DeviceUseCase;
import com.device.management.application.DeviceView;
import com.device.management.controller.request.DeviceRequest;
import com.device.management.controller.response.DeviceResponse;
import com.device.management.exception.GlobalExceptionHandler;
import com.device.management.mapper.ApiMapper;
import com.device.management.state.DeviceState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.device.management.TestConstants.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeviceManagementControllerTest {

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
                .build();
    }

    @Test
    @DisplayName("POST /devices returns 201 with created device body")
    void createDevice_http_success() throws Exception {
        // Arrange
        DeviceState state = DeviceState.AVAILABLE;
        UUID id = UUID.fromString(DEVICE_ID);
        OffsetDateTime createdAt = OffsetDateTime.parse(CREATION_TIME);

        when(apiMapper.toCreateCommand(ArgumentMatchers.any(DeviceRequest.class)))
                .thenAnswer(mock -> {
                    DeviceRequest request = mock.getArgument(0);
                    return new DeviceCreateCommand(request.name(), request.brand(), request.state());
                });
        when(useCase.create(ArgumentMatchers.any(DeviceCreateCommand.class)))
                .thenAnswer(mock -> {
                    DeviceCreateCommand command = mock.getArgument(0);
                    return new DeviceView(id, command.name(), command.brand(), command.state(), createdAt);
                });
        when(apiMapper.toResponse(ArgumentMatchers.any(DeviceView.class)))
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
        when(apiMapper.toResponse(ArgumentMatchers.any(DeviceView.class)))
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
}
