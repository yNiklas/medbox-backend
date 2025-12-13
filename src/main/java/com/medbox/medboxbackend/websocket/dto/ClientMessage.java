package com.medbox.medboxbackend.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClientMessage {
    @JsonProperty("messageType")
    private int messageType;
    
    @JsonProperty("message")
    private Object message;
}
