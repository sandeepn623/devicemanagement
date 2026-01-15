package com.device.management.service;

import com.device.management.application.DeviceCreateCommand;
import com.device.management.application.DeviceView;
import com.device.management.entity.Device;
import com.device.management.mapper.DeviceMapper;
import com.device.management.repository.DeviceRepository;
import com.device.management.state.DeviceState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.device.management.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceManagementServiceTest {

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
                .name(DEVICE_NAME)
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
        UUID generatedId = UUID.fromString(DEVICE_ID);
        OffsetDateTime createdAt = OffsetDateTime.parse(CREATION_TIME);
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

        // Verify
        verify(mapper).toEntity(deviceCreateCommand);
        ArgumentCaptor<Device> savedCaptor = ArgumentCaptor.forClass(Device.class);
        verify(repository).saveAndFlush(savedCaptor.capture());
        assertEquals(DEVICE_NAME, savedCaptor.getValue().getName());
        assertEquals(DEVICE_BRAND, savedCaptor.getValue().getBrand());
        assertEquals(DeviceState.AVAILABLE, savedCaptor.getValue().getState());
        verify(mapper).toView(saved);
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    @DisplayName("delete() deletes device when not IN_USE (happy path)")
    void delete_success_service() {
        UUID id = UUID.fromString(DEVICE_ID);
        Device device = new Device();
        device.setName(DEVICE_NAME);
        device.setBrand(DEVICE_BRAND);
        device.setState(DeviceState.AVAILABLE);

        when(repository.findById(id)).thenReturn(Optional.of(device));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).deleteById(id);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("delete() throws NoSuchElementException when device not found")
    void delete_notFound_service() {
        UUID id = UUID.fromString(DEVICE_ID);

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> service.delete(id));
        verify(repository).findById(id);
        verify(repository, never()).deleteById(any());
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("delete() throws IllegalStateException when device is IN_USE")
    void delete_inUse_service() {
        UUID id = UUID.fromString(DEVICE_ID);
        Device device = new Device();
        device.setName(DEVICE_NAME);
        device.setBrand(DEVICE_BRAND);
        device.setState(DeviceState.IN_USE);

        when(repository.findById(id)).thenReturn(Optional.of(device));

        assertThrows(IllegalStateException.class, () -> service.delete(id));
        verify(repository).findById(id);
        verify(repository, never()).deleteById(any());
        verifyNoMoreInteractions(repository);
    }
}
