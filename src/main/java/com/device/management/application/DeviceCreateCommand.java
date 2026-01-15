package com.device.management.application;

import com.device.management.state.DeviceState;

public record DeviceCreateCommand(
        String name,
        String brand,
        DeviceState state
) {

    public static class Builder {
        private String name;
        private String brand;
        private DeviceState state;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder state(DeviceState state) {
            this.state = state;
            return this;
        }

        public DeviceCreateCommand build() {
            return new DeviceCreateCommand(name, brand, state);
        }
    }
}

