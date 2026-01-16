package com.device.management.repository;

import com.device.management.repository.entity.Device;
import com.device.management.state.DeviceState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    // Pageable variants
    Page<Device> findAll(Pageable pageable);
    Page<Device> findByBrandIgnoreCase(String brand, Pageable pageable);
    Page<Device> findByState(DeviceState state, Pageable pageable);
    Page<Device> findByBrandIgnoreCaseAndState(String brand, DeviceState state, Pageable pageable);
}

