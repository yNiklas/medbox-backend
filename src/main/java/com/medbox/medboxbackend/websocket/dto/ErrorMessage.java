package com.medbox.medboxbackend.websocket.dto;

import lombok.Data;

@Data
public class ErrorMessage {
    private int error;
    private String content;
}
