package com.device.management.mapper;

import com.device.management.application.DeviceCreateCommand;
import com.device.management.application.DeviceUpdateCommand;
import com.device.management.application.DeviceView;
import com.device.management.controller.request.DeviceRequest;
import com.device.management.controller.request.DeviceUpdateRequest;
import com.device.management.controller.response.DeviceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiMapper {
    DeviceCreateCommand toCreateCommand(DeviceRequest request);
    DeviceUpdateCommand toUpdateCommand(DeviceUpdateRequest request);
    DeviceResponse toResponse(DeviceView view);
}
