package com.device.management.service.dto;

public record SortOrder(String property, Direction direction) {
    public enum Direction { ASC, DESC }
}
