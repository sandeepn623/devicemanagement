package com.device.management.service.dto;

import java.util.List;

public record PageRequest(
        int page,
        int size,
        List<SortOrder> sort
) {}
