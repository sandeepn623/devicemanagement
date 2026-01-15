package com.device.management.application;

import javax.swing.*;
import java.util.List;

public record PageRequest(
        int page,
        int size,
        List<SortOrder> sort
) {}
