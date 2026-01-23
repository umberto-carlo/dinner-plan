package it.ucdm.leisure.dinnerplan.features.geocode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class GeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${geocoding.url}")
    private String photonUrl;

    @Value("${geocoding.arcgis.url}")
    private String arcgisUrl;

    public Coordinates getCoordinates(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        // Strategy 1: Photon (Primary - Open Data)
        Coordinates coordinates = tryPhoton(address);
        if (coordinates != null) return coordinates;

        // Strategy 2: Photon with relaxed address
        String relaxedAddress = stripHouseNumber(address);
        if (!relaxedAddress.equals(address)) {
            coordinates = tryPhoton(relaxedAddress);
            if (coordinates != null) return coordinates;
        }

        // Strategy 3: ArcGIS (Secondary - Commercial but free tier) - Very robust for streets
        logger.info("Photon failed, failing over to ArcGIS for address: '{}'", address);
        coordinates = tryArcGIS(address);
        if (coordinates != null) return coordinates;

        // Strategy 4: ArcGIS with relaxed address
        if (!relaxedAddress.equals(address)) {
             coordinates = tryArcGIS(relaxedAddress);
             if (coordinates != null) return coordinates;
        }

        return null;
    }

    private String stripHouseNumber(String address) {
        String[] parts = address.split(",", 2);
        if (parts.length > 0) {
            String streetPart = parts[0];
            String cleanedStreet = streetPart.replaceAll("\\s+\\d+$", "").trim();
            if (parts.length > 1) {
                return cleanedStreet + "," + parts[1];
            } else {
                return cleanedStreet;
            }
        }
        return address;
    }

    private Coordinates tryPhoton(String query) {
        try {
            Thread.sleep(200);
            String fullQuery = query;
            if (!query.toLowerCase().contains("italy") && !query.toLowerCase().contains("italia")) {
                 fullQuery += ", Italy";
            }

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(photonUrl)
                    .queryParam("q", fullQuery)
                    .queryParam("limit", 1);

            logger.info("Attempting geocoding with Photon: '{}'", fullQuery);
            return executeRequest(uriBuilder.toUriString(), "Photon", true);
        } catch (Exception e) {
            logger.error("Photon error", e);
        }
        return null;
    }

    private Coordinates tryArcGIS(String query) {
        try {
            Thread.sleep(200);
            
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(arcgisUrl)
                    .queryParam("SingleLine", query)
                    .queryParam("f", "json")
                    .queryParam("maxLocations", 1);

            logger.info("Attempting geocoding with ArcGIS: '{}'", query);
            return executeRequest(uriBuilder.toUriString(), "ArcGIS", false);
        } catch (Exception e) {
            logger.error("ArcGIS error", e);
        }
        return null;
    }

    private Coordinates executeRequest(String url, String provider, boolean isPhoton) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, "DinnerPlanApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();
            
            if (responseBody == null) return null;
            JsonNode root = objectMapper.readTree(responseBody);

            if (isPhoton) {
                // Photon Format (GeoJSON)
                JsonNode features = root.get("features");
                if (features != null && features.isArray() && !features.isEmpty()) {
                    JsonNode coordinates = features.get(0).get("geometry").get("coordinates");
                    double lon = coordinates.get(0).asDouble();
                    double lat = coordinates.get(1).asDouble();
                    logger.info("{} success: {}, {}", provider, lat, lon);
                    return new Coordinates(lat, lon);
                }
            } else {
                // ArcGIS Format
                JsonNode candidates = root.get("candidates");
                if (candidates != null && candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode location = candidates.get(0).get("location");
                    double lat = location.get("y").asDouble();
                    double lon = location.get("x").asDouble();
                    logger.info("{} success: {}, {}", provider, lat, lon);
                    return new Coordinates(lat, lon);
                }
            }
        } catch (Exception e) {
            logger.warn("{} request failed: {}", provider, e.getMessage());
        }
        return null;
    }
}
