package com.emmanuel.development.application.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse (
        @JsonProperty("principal")
        String principal
) {}
