package com.medbox.medboxbackend.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerMessage {
    @JsonProperty("messageType")
    private int messageType;

    @JsonProperty("targetBoxMAC")
    private String targetBoxMAC;
    
    @JsonProperty("message")
    private Object message;
}
