package com.example.solicituddemo.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.solicituddemo.service.HubSpotContactSyncService;
import com.example.solicituddemo.service.SyncResult;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/hubspot")
public class ContactSyncController {

    private final HubSpotContactSyncService hubSpotContactSyncService;

    public ContactSyncController(HubSpotContactSyncService hubSpotContactSyncService) {
        this.hubSpotContactSyncService = hubSpotContactSyncService;
    }

    @PostMapping("/contacts/sync")
    public ResponseEntity<SyncResult> syncContact(@Valid @RequestBody ContactSyncRequest request) {
        SyncResult result = hubSpotContactSyncService.syncContact(request);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}