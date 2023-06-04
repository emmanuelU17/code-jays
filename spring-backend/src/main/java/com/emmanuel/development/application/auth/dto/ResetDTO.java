package com.emmanuel.development.application.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.json.Json;
import javax.json.JsonObject;

public record ResetDTO (@JsonProperty("password") String password){
    public JsonObject convertToJSON() {
        return Json.createObjectBuilder().add("password", password()).build();
    }
}
