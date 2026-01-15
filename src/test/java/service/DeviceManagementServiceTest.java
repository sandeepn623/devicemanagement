package service;

import com.device.management.application.DeviceCreateCommand;
import com.device.management.application.DeviceView;
import com.device.management.entity.Device;
import com.device.management.mapper.DeviceMapper;
import com.device.management.repository.DeviceRepository;
import com.device.management.service.DeviceManagementService;
import com.device.management.state.DeviceState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceManagementServiceTest {

    public static final String DEVICE_NAME = "iPhone 14";
    public static final String DEVICE_BRAND = "Apple";
    @Mock
    private DeviceRepository repository;

    @Mock
    private DeviceMapper mapper;

    @InjectMocks
    private DeviceManagementService service;

    @Test
    @DisplayName("create() maps command -> entity, saves, and returns mapped view")
    void create_success() {
        // Arrange
        var deviceCreateCommand = new DeviceCreateCommand.Builder()
                .name("iPhone 14")
                .brand(DEVICE_BRAND)
                .state(DeviceState.AVAILABLE)
                .build();

        // Entity produced by mapper before save
        Device toSave = new Device();
        toSave.setName(DEVICE_NAME);
        toSave.setBrand(DEVICE_BRAND);
        toSave.setState(DeviceState.AVAILABLE);

        // Entity returned by repository after save
        Device saved = new Device();
        saved.setName(DEVICE_NAME);
        saved.setBrand(DEVICE_BRAND);
        saved.setState(DeviceState.AVAILABLE);

        // Expected view mapped from the saved entity
        UUID generatedId = UUID.fromString("11111111-2222-4333-8444-555555555555");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-15T07:00:00Z");
        DeviceView expectedView = new DeviceView(
                generatedId,
                DEVICE_NAME,
                DEVICE_BRAND,
                DeviceState.AVAILABLE,
                createdAt
        );

        when(mapper.toEntity(deviceCreateCommand)).thenReturn(toSave);
        when(repository.saveAndFlush(toSave)).thenReturn(saved);
        when(mapper.toView(saved)).thenReturn(expectedView);

        // Act
        DeviceView result = service.create(deviceCreateCommand);

        // Assert
        assertNotNull(result);
        assertEquals(expectedView, result);

        // Verify interactions and inputs
        verify(mapper).toEntity(deviceCreateCommand);
        ArgumentCaptor<Device> savedCaptor = ArgumentCaptor.forClass(Device.class);
        verify(repository).saveAndFlush(savedCaptor.capture());
        assertEquals(DEVICE_NAME, savedCaptor.getValue().getName());
        assertEquals(DEVICE_BRAND, savedCaptor.getValue().getBrand());
        assertEquals(DeviceState.AVAILABLE, savedCaptor.getValue().getState());
        verify(mapper).toView(saved);
        verifyNoMoreInteractions(mapper, repository);
    }
}
