package com.supermarketpos.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.sun.net.httpserver.Request;

import java.util.Collections;
import java.util.List;

public class SheetsApiClient {

    private final Sheets sheetsService;
    private static final String APP_NAME = "SmartMart POS";

    public SheetsApiClient(GoogleAuthHandler authHandler) throws Exception {
        this.sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                authHandler.getCredential())
                .setApplicationName(APP_NAME)
                .build();
    }

    public String createSpreadsheet(String title) throws Exception {
        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties().setTitle(title));

        List<String> sheetNames = List.of(
                "Products", "Customers", "Purchases", "Sales", "Inventory", "Settings", "Sync Log");

        var sheetsList = new java.util.ArrayList<Sheet>();
        for (String name : sheetNames) {
            sheetsList.add(new Sheet().setProperties(new SheetProperties().setTitle(name)));
        }
        spreadsheet.setSheets(sheetsList);

        Spreadsheet created = sheetsService.spreadsheets().create(spreadsheet).execute();
        return created.getSpreadsheetId();
    }

    public void ensureWorksheetsExist(String spreadsheetId) throws Exception {
        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
        List<String> existing = spreadsheet.getSheets().stream()
                .map(s -> s.getProperties().getTitle())
                .toList();

        List<String> required = List.of(
                "Products", "Customers", "Purchases", "Sales", "Inventory", "Settings", "Sync Log");

        List<Request> requests = new java.util.ArrayList<>();
        for (String name : required) {
            if (!existing.contains(name)) {
                requests.add(new Request().setAddSheet(
                        new AddSheetRequest().setProperties(new SheetProperties().setTitle(name))));
            }
        }
        if (!requests.isEmpty()) {
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            sheetsService.spreadsheets().batchUpdate(spreadsheetId, body).execute();
        }
    }

    /** Appends rows only. Never deletes existing data, per sync rules. */
    public void appendRows(String spreadsheetId, String sheetName, List<List<Object>> rows) throws Exception {
        if (rows.isEmpty()) return;
        ValueRange body = new ValueRange().setValues(rows);
        sheetsService.spreadsheets().values()
                .append(spreadsheetId, sheetName + "!A1", body)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }

    /** Overwrites a specific range - used for update-in-place sync of existing rows only. */
    public void updateRange(String spreadsheetId, String range, List<List<Object>> rows) throws Exception {
        ValueRange body = new ValueRange().setValues(rows);
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public List<List<Object>> readRange(String spreadsheetId, String range) throws Exception {
        ValueRange response = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute();
        return response.getValues() != null ? response.getValues() : Collections.emptyList();
    }
}