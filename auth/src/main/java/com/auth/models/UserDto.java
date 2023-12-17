package com.auth.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDto(String email,
                      @JsonProperty("first_name") String firstName,
                      @JsonProperty("second_name") String secondName,
                      char[] password) {}