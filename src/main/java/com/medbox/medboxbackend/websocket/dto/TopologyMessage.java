package com.medbox.medboxbackend.websocket.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TopologyMessage {
    private Map<String, String> boxes;
}
