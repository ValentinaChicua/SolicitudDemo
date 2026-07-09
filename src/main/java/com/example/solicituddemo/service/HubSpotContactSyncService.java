package com.example.solicituddemo.service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.solicituddemo.api.ContactSyncRequest;

@Service
public class HubSpotContactSyncService {

    private static final Logger log = LoggerFactory.getLogger(HubSpotContactSyncService.class);
    private final HubSpotClient hubSpotClient;

    public HubSpotContactSyncService(HubSpotClient hubSpotClient) {
        this.hubSpotClient = hubSpotClient;
    }

    public SyncResult syncContact(ContactSyncRequest request) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("firstname", request.firstname());
        properties.put("lastname", request.lastname());
        properties.put("email", request.email());
        properties.put("company", request.company());

        Optional<String> existingContactId = hubSpotClient.findContactIdByEmail(request.email());
        String operation;
        String contactId;

        if (existingContactId.isPresent()) {
            contactId = existingContactId.get();
            hubSpotClient.updateContact(contactId, properties);
            operation = "updated";
        } else {
            contactId = hubSpotClient.createContact(properties);
            operation = "created";
        }

        Optional<String> companyId = ensureCompanyAssociation(request, contactId);
        SyncResult result = new SyncResult(operation, contactId, request.email(), companyId.orElse(null));
        log.info("HubSpot contact sync result: {}", result);
        System.out.println("Resultado de la operación: " + result);
        return result;
    }

    private Optional<String> ensureCompanyAssociation(ContactSyncRequest request, String contactId) {
        String domain = extractDomain(request.email());
        if (domain.isBlank()) {
            return Optional.empty();
        }

        Optional<String> companyId = hubSpotClient.findCompanyIdByDomain(domain);
        String resolvedCompanyId = companyId.orElseGet(() -> hubSpotClient.createCompany(request.company(), domain));
        hubSpotClient.associateContactToCompany(contactId, resolvedCompanyId);
        return Optional.of(resolvedCompanyId);
    }

    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex < 0 || atIndex == email.length() - 1) {
            return "";
        }
        return email.substring(atIndex + 1).toLowerCase(Locale.ROOT);
    }
}