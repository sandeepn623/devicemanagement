package controller;

import com.device.management.application.DeviceCreateCommand;
import com.device.management.application.DeviceUseCase;
import com.device.management.application.DeviceView;
import com.device.management.controller.DeviceManagementController;
import com.device.management.dto.DeviceRequest;
import com.device.management.dto.DeviceResponse;
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
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        String name = "iPhone 14";
        String brand = "Apple";
        DeviceState state = DeviceState.AVAILABLE;
        UUID id = UUID.fromString("11111111-1111-4111-8111-111111111111");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-15T04:20:31Z");

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
                "\"name\":\"" + name + "\"," +
                "\"brand\":\"" + brand + "\"," +
                "\"state\":\"" + state + "\"" +
                "}";

        // Act + Assert
        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.brand", is(brand)))
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
}
