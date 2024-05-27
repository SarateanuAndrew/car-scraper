package com.example.webscraping.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Source {
    AUTOBID("autobid.de");
    public String url;

    Source(String url) {
        this.url = url;
    }

    @JsonValue
    public String getUrl() {
        return url;
    }

    @JsonCreator
    public static Source fromValue(String value) {
        for (Source type : Source.values()) {
            if (type.url.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException();
    }
}
