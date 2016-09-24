package com.example.will.sfclippy;

import android.util.Log;

import com.example.will.sfclippy.models.BattleResult;
import com.example.will.sfclippy.models.CharacterPreference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by will on 24/09/2016.
 */

public class DatabaseHelper {
    private FirebaseDatabase mDatabase;
    private String mAccountId;
    private DatabaseReference mUserHome;
    private static final String TAG = "DatabaseHelper";
    private static final int DEFAULT_SCORE = 2;

    DatabaseHelper( FirebaseDatabase database, String accountId ) {
        this.mDatabase = database;
        this.mAccountId = accountId;
        this.mUserHome = database.getReference( "/users/" + accountId );
    }

    private DatabaseReference getPlayersDirReference( ) {
        return mUserHome.child( "players" );
    }

    public DatabaseReference getPreferencesDirReference( ) {
        return mUserHome.child( "preferences" );
    }

    public DatabaseReference getResultsDirReference( ) {
        return mUserHome.child( "results" );
    }

    public DatabaseReference getPlayerNameRef( String playerId ) {
        return getPlayersDirReference().child( playerId ).child( "playerName" );
    }

    public DatabaseReference getPlayerPrefsRef( String playerId ) {
        return getPreferencesDirReference().child( playerId );
    }

    public String getAccountId( ) {
        return mAccountId;
    }

    public interface PlayersCallback {
        void playersInitialised( String p1Id, String p2Id );
    };

    private void initialiseUser( DatabaseReference ref, String playerName ) {
        ref.setValue( playerName );
    }

    public void fetchOrInitialisePlayers( final PlayersCallback callback ) {
        final DatabaseReference players = getPlayersDirReference();
        players.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( null == dataSnapshot.getValue() ) {
                    Log.d( TAG, "Bootstrapping users");
                    DatabaseReference p1 = players.push();
                    initialiseUser( p1, "Red Panda");

                    DatabaseReference p2 = players.push();
                    initialiseUser( p2, "Blue Goose");

                    callback.playersInitialised( p1.getKey(), p2.getKey() );
                } else {
                    Log.d( TAG, "Fetching users");
                    players.removeEventListener(this);
                    List<String> playerIds = new ArrayList<>();
                    for ( DataSnapshot child : dataSnapshot.getChildren() ) {
                        playerIds.add( child.getKey() );
                    }

                    callback.playersInitialised( playerIds.get(0), playerIds.get(1) );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO
            }
        });
    }

    public void storeResult( String player1Id,
                             String p1Choice,
                             String player2Id,
                             String p2Choice,
                             String winnerId,
                             DatabaseReference.CompletionListener listener ) {
        DatabaseReference results = getResultsDirReference();
        DatabaseReference child = results.push();

        BattleResult result = new BattleResult( Calendar.getInstance().getTime(),
                child.getKey(),
                player1Id,
                p1Choice,
                player2Id,
                p2Choice,
                winnerId );
        child.setValue( result, listener );
    }

    private static String characterKey( String name ) {
        String key = name.toLowerCase().replaceAll( "[^\\p{Lower}]", "");
        return key;
    }

    private DatabaseReference getCharacterPreference( String playerId, String characterName ) {
        String charKey = characterKey(characterName);
        return getPlayerPrefsRef( playerId ).child(charKey);
    }

    public void updateCharacterScore( String playerId,
                                      String characterName,
                                      int newScore ) {
        DatabaseReference character = getCharacterPreference( playerId, characterName );
        DatabaseReference scoreRef = character.child("score");
        scoreRef.setValue(newScore);
    }

    private static void createCharacter( DatabaseReference preferences, String name ) {
        CharacterPreference pref = new CharacterPreference( name, DEFAULT_SCORE, 0, 0 );
        String key = characterKey(name);
        preferences.child(key).setValue( pref );
    }

    public void bootstrapCharacterPrefs( String playerId ) {
        DatabaseReference ref = getPlayerPrefsRef(playerId);
        String[] characters = new String[] {
                "Ryu", "Chun-Li", "Nash", "M.Bison",
                "Cammy", "Birdie", "Ken", "Necalli",
                "Vega", "R.Mika", "Rashid", "Karin",
                "Zangief", "Laura", "Dhalsim", "F.A.N.G.",
                "Alex", "Guile", "Ibuki", "Balrog", "Juri" };
        for ( String character : characters ) {

        }
    }

    public void addCharacterPref( String playerId, String characterName ) {
        DatabaseReference character = getCharacterPreference( playerId, characterName);
        createCharacter( character, characterName );
    }
}
