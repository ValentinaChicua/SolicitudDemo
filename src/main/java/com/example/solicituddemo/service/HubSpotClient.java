package com.example.solicituddemo.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.solicituddemo.config.HubSpotProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HubSpotClient {

    private static final String BASE_URL = "https://api.hubapi.com";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HubSpotProperties properties;

    public HubSpotClient(HttpClient httpClient, ObjectMapper objectMapper, HubSpotProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public Optional<String> findContactIdByEmail(String email) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("filterGroups", List.of(Map.of("filters", List.of(Map.of(
                "propertyName", "email",
                "operator", "EQ",
                "value", email
        )))));
        payload.put("properties", List.of("email", "firstname", "lastname", "company"));

        JsonNode response = postJson("/crm/v3/objects/contacts/search", payload);
        JsonNode results = response.path("results");
        if (results.isArray() && !results.isEmpty()) {
            return Optional.ofNullable(results.get(0).path("id").asText(null));
        }
        return Optional.empty();
    }

    public String createContact(Map<String, Object> properties) {
        JsonNode response = postJson("/crm/v3/objects/contacts", Map.of("properties", properties));
        return response.path("id").asText();
    }

    public void updateContact(String contactId, Map<String, Object> properties) {
        sendJson("PATCH", "/crm/v3/objects/contacts/" + contactId, Map.of("properties", properties));
    }

    public Optional<String> findCompanyIdByDomain(String domain) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("filterGroups", List.of(Map.of("filters", List.of(Map.of(
                "propertyName", "domain",
                "operator", "EQ",
                "value", domain
        )))));
        payload.put("properties", List.of("name", "domain"));

        JsonNode response = postJson("/crm/v3/objects/companies/search", payload);
        JsonNode results = response.path("results");
        if (results.isArray() && !results.isEmpty()) {
            return Optional.ofNullable(results.get(0).path("id").asText(null));
        }
        return Optional.empty();
    }

    public String createCompany(String companyName, String domain) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", companyName);
        properties.put("domain", domain);

        JsonNode response = postJson("/crm/v3/objects/companies", Map.of("properties", properties));
        return response.path("id").asText();
    }

    public void associateContactToCompany(String contactId, String companyId) {
        sendJson("PUT", "/crm/v3/objects/contacts/" + contactId + "/associations/companies/" + companyId + "/contact_to_company", Map.of());
    }

    public Optional<String> getApiToken() {
        return Optional.ofNullable(properties.apiToken()).filter(token -> !token.isBlank());
    }

    private JsonNode postJson(String path, Map<String, Object> payload) {
        return sendJson("POST", path, payload);
    }

    private JsonNode sendJson(String method, String path, Map<String, Object> payload) {
        ensureTokenConfigured();
        try {
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .header("Authorization", "Bearer " + properties.apiToken())
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("HubSpot API error " + response.statusCode() + ": " + response.body());
            }
            if (response.body() == null || response.body().isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(response.body());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar la solicitud a HubSpot", e);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la respuesta de HubSpot", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La llamada a HubSpot fue interrumpida", e);
        }
    }

    private void ensureTokenConfigured() {
        if (properties.apiToken() == null || properties.apiToken().isBlank()) {
            throw new IllegalStateException("Configura la propiedad hubspot.api-token o la variable HUBSPOT_API_TOKEN");
        }
    }
}