package com.emmanuel.development.application.image.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageResponse (
        @JsonProperty("name")
        String imageName,
        @JsonProperty("media_type")
        String mediaType,
        @JsonProperty("bytes")
        byte[] bytes
) {
}
