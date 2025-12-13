package com.medbox.medboxbackend.websocket.dto;

import lombok.Data;

@Data
public class KeepaliveMessage {
    private String status;
}
