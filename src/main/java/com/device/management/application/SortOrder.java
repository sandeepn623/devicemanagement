package com.device.management.application;

public record SortOrder(String property, Direction direction) {
    public enum Direction { ASC, DESC }
}
