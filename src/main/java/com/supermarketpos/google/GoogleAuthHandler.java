package com.supermarketpos.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;

public class GoogleAuthHandler {

    private static final String TOKENS_DIR = "tokens";
    private static final String CREDENTIALS_FILE = "credentials.json"; // OAuth client secret from Google Cloud Console

    private Credential credential;

    public boolean isAuthenticated() {
        return credential != null;
    }

    public Credential login() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = GsonFactory.getDefaultInstance();

        GoogleClientSecrets clientSecrets;
        try (var reader = new InputStreamReader(new FileInputStream(CREDENTIALS_FILE))) {
            clientSecrets = GoogleClientSecrets.load(jsonFactory, reader);
        }

        var flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets,
                Collections.singletonList(SheetsScopes.SPREADSHEETS))
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIR)))
                .setAccessType("offline")
                .build();

        credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    public void logout() {
        credential = null;
        // Also delete stored token files from TOKENS_DIR if a full logout is required
        File tokenDir = new File(TOKENS_DIR);
        File[] files = tokenDir.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
    }

    public Credential getCredential() {
        return credential;
    }
}