package com.medbox.medboxbackend.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangeFunnelSpotRequest {
    @JsonProperty("targetBoxMAC")
    private String targetBoxMAC;

    @JsonProperty("targetCompartmentNumber")
    private int targetCompartmentNumber;
}
