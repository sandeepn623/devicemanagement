package com.device.management.mapper;


import com.device.management.service.dto.DeviceCreateCommand;
import com.device.management.service.dto.DeviceUpdateCommand;
import com.device.management.service.dto.DeviceView;
import com.device.management.entity.Device;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeviceMapper {

    Device toEntity(DeviceCreateCommand cmd);

    DeviceView toView(Device device);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Device target, DeviceUpdateCommand update);
}
