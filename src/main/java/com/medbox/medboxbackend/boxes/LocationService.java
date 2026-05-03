package com.medbox.medboxbackend.boxes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class LocationService {
    private final Map<String, Map<String, Integer>> locations;

    public LocationService() {
        this.locations = loadLocationsFromJson();
    }

    private Map<String, Map<String, Integer>> loadLocationsFromJson() {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("rooms.json");
            if (inputStream != null) {
                Map<String, Map<String, Integer>> data = objectMapper.readValue(inputStream, Map.class);
                result.putAll(data);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load rooms.json", e);
        }
        return result;
    }

    public List<String> getAvailableLocations() {
        return new ArrayList<>(locations.keySet());
    }

    public boolean isValidLocation(String location) {
        return location != null && locations.containsKey(location);
    }

    public Map<String, Integer> getLocationCoordinates(String location) {
        return locations.get(location);
    }
}
