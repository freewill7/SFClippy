package com.example.will.sfclippy;

import android.util.Log;

import com.example.will.sfclippy.models.BattleCounter;
import com.example.will.sfclippy.models.BattleResult;
import com.example.will.sfclippy.models.CharacterPreference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by will on 24/09/2016.
 */

public class DatabaseHelper {
    private FirebaseDatabase mDatabase;
    private String mAccountId;
    private DatabaseReference mUserHome;
    private static final String TAG = "DatabaseHelper";
    private static final int DEFAULT_SCORE = 2;
    private static final String STATISTICS_MEMBER = "statistics";

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
        CharacterPreference pref = new CharacterPreference( name, DEFAULT_SCORE );
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

    private Map<String,BattleCounter> getPlayerMap( Map<String, Map<String,BattleCounter>> map,
                                                   String playerId ) {
        Map<String,BattleCounter> rv = map.get( playerId );
        if ( null == rv ) {
            rv = new HashMap<>();
            map.put( playerId, rv );
        }
        return rv;
    }

    private BattleCounter getCharacterResults( Map<String,BattleCounter> map, String character ) {
        BattleCounter rv = map.get(character);
        if ( null == rv ) {
            rv = new BattleCounter();
            map.put( character, rv );
        }
        return rv;
    }

    private void updateResults( BattleCounter p1Result, String p1Id,
                                BattleCounter p2Result, String p2Id,
                                BattleResult result ) {
        if ( result.winnerId.equals(p1Id)) {
            p1Result.recordWin( result.dateAsDate() );
            p2Result.recordLoss( result.dateAsDate() );
        } else {
            p1Result.recordLoss( result.dateAsDate() );
            p2Result.recordWin( result.dateAsDate() );
        }
    }

    public DatabaseReference getCharacterPreferenceWins( String playerId, String charName ) {
        return getCharacterPreference(playerId, charName).child( "battles_won" );
    }

    public DatabaseReference getCharacterPreferenceBattles( String playerId, String charName ) {
        return getCharacterPreference(playerId, charName).child( "battles_fought" );
    }

    public DatabaseReference getCharacterStatistics( String playerId, String charName ) {
        return getCharacterPreference(playerId, charName).child( STATISTICS_MEMBER );
    }

    private void doRegenerate( List<BattleResult> results, StatisticsCompleteListener listener ) {

        // analyse results
        Map<String, Map<String,BattleCounter>> playerCharacterCounter = new HashMap<>();
        for ( BattleResult result : results ) {
            Map<String,BattleCounter> p1Chars = getPlayerMap( playerCharacterCounter, result.p1Id );
            BattleCounter p1Results = getCharacterResults(p1Chars, result.p1Character);

            Map<String,BattleCounter> p2Chars = getPlayerMap( playerCharacterCounter, result.p2Id );;
            BattleCounter p2Results = getCharacterResults(p2Chars, result.p2Character);

            updateResults( p1Results, result.p1Id, p2Results, result.p2Id, result );
            listener.statisticsComplete();
        }

        // update database for each character
        for ( Map.Entry<String, Map<String,BattleCounter>> player : playerCharacterCounter.entrySet() ) {
            String playerId = player.getKey();

            for ( Map.Entry<String,BattleCounter> charResult : player.getValue().entrySet() ) {
                String charName = charResult.getKey();
                BattleCounter charCount = charResult.getValue();

                getCharacterStatistics( playerId, charName ).setValue( charCount );
            }
        }
    }

    public interface StatisticsCompleteListener {
        void statisticsComplete( ) ;
    }

    public void regenerateStatistics(final StatisticsCompleteListener listener ) {
        final Query results = getResultsDirReference().orderByChild("date");
        results.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                results.removeEventListener(this);

                if ( null != dataSnapshot ) {
                    ArrayList<BattleResult> results = new ArrayList<BattleResult>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        results.add(child.getValue(BattleResult.class));
                    }

                    doRegenerate( results, listener );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
