package com.example.will.sfclippy;

import com.google.firebase.database.DatabaseReference;

import java.io.IOException;

/**
 * Created by will on 10/08/2016.
 */
public class DataProvider {
    private DatabaseReference usersRef;
    private DatabaseReference preferencesRef;
    private DatabaseReference resultsRef;
    private static String TAG = "DataProvider";

    public DataProvider(DatabaseReference usersRef,
                        DatabaseReference preferencesRef,
                        DatabaseReference resultsRef ) {
        this.usersRef = usersRef;
        this.preferencesRef = preferencesRef;
        this.resultsRef = resultsRef;
    }

    public DatabaseReference getUser( String playerId ) {
        return FirebaseHelper.getUser( usersRef, playerId ).getReference();
    }

    public DatabaseReference getUsername( String playerId ) {
        return FirebaseHelper.getUser( usersRef, playerId ).getUsername();
    }

    public DatabaseReference getPreferences( String playerId ) {
        return FirebaseHelper.getPreferences( preferencesRef, playerId );
    }

    public DatabaseReference getResults( ) {
        return resultsRef;
    }

    public HistoricalTrends getHistoricalTrends( ) {
        //return new HistoricalTrends( battleResults );
        return null;
    }

    public void saveResults( ) throws IOException {
    }
}
