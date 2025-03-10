package sg.edu.nus.iss.login_service.entity;  // Or your appropriate package

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProfileType {
    CUSTOMER,
    MERCHANT,
    DELIVERY;

    @JsonCreator
    public static ProfileType fromValue(String value) {
        return ProfileType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name().toUpperCase();
    }
}