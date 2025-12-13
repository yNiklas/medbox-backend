package com.medbox.medboxbackend.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DispenseRequest {
    @JsonProperty("targetBoxMAC")
    private String targetBoxMAC;
    
    @JsonProperty("compartmentNumber")
    private int compartmentNumber;
    
    @JsonProperty("amountOfPillsToDispense")
    private int amountOfPillsToDispense;
}
