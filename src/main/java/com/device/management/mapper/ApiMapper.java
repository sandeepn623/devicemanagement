package com.device.management.mapper;

import com.device.management.application.DeviceCreateCommand;
import com.device.management.application.DeviceUpdateCommand;
import com.device.management.application.DeviceView;
import com.device.management.dto.DeviceRequest;
import com.device.management.dto.DeviceResponse;
import com.device.management.dto.DeviceUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiMapper {
    DeviceCreateCommand toCreateCommand(DeviceRequest request);
    DeviceUpdateCommand toUpdateCommand(DeviceUpdateRequest request);
    DeviceResponse toResponse(DeviceView view);
}
