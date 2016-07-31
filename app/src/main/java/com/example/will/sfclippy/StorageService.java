package com.example.will.sfclippy;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 24/07/2016.
 */
public class StorageService {
    private final Sheets service;
    private final String spreadsheetId;

    public StorageService(Sheets service, String spreadsheetId) {
        this.service = service;
        this.spreadsheetId = spreadsheetId;
    }

    public CharacterStatistics getStatistics() throws IOException {
        CharacterStatistics statistics = new CharacterStatistics();

        String range = "Character Preferences!A2:C";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.size() == 0) {
            System.out.println("No data found.");
        } else {
            System.out.println("Name, Major");
            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                String name = (String) row.get(0);
                String p1Score = (String) row.get(1);
                String p2Score = (String) row.get(2);

                statistics.addCharacter( name,
                        Integer.parseInt(p1Score), Integer.parseInt(p2Score) );
                System.out.printf("%s, %s, %s\n", row.get(0), row.get(1), row.get(2));
            }
        }

        return statistics;
    }

    public void recordBattle(String battleDate,
                             String ruaidhriCharacter,
                             String willCharacter,
                             String winner) throws IOException {
        String fullSheetRange = "Battle History!A2:D";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, fullSheetRange).execute();
        List<List<Object>> values = response.getValues();
        int qtyValues = 0;
        if (null != values) {
            qtyValues = values.size();
        }
        int insertRow = qtyValues + 2;

        String range = "Battle History!A" + insertRow
                + ":D" + insertRow;

        List<Object> row = new ArrayList<Object>();
        row.add(battleDate);
        row.add(ruaidhriCharacter);
        row.add(willCharacter);
        row.add(winner);

        List<List<Object>> rows = new ArrayList<List<Object>>();
        rows.add(row);

        ValueRange vr = new ValueRange();
        vr.setRange(range);
        vr.setValues(rows);

        Sheets.Spreadsheets.Values.Update bob = service.spreadsheets().values().update(spreadsheetId,
                range, vr);
        bob.setValueInputOption("RAW");

        bob.execute();
    }
}
