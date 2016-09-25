package com.example.will.sfclippy;

import android.util.Log;
import android.util.Pair;

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
import java.util.Date;
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
    private static final String PLAYER_VS_PLAYER_DIR = "player_vs_player";
    private static final String PVP_OVERALL = "overall";
    private static final String PVP_CHARACTERS = "characters";
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

    private void incrementWin( final DatabaseReference ref, final Date date ) {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ref.removeEventListener(this);
                BattleCounter counter = new BattleCounter();

                if ( null != dataSnapshot ) {
                    BattleCounter tmp = dataSnapshot.getValue(BattleCounter.class);
                    if ( null != tmp ) {
                        counter = tmp;
                    }
                }

                counter.recordWin(date);
                ref.setValue(counter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e( "recordWin", "incrementWin", databaseError.toException() );
            }
        });
    }

    private void incrementLoss( final DatabaseReference ref, final Date date ) {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ref.removeEventListener(this);
                BattleCounter counter = new BattleCounter();

                if ( null != dataSnapshot ) {
                    BattleCounter tmp = dataSnapshot.getValue(BattleCounter.class);
                    if ( null != tmp ) {
                        counter = tmp;
                    }
                }

                counter.recordLoss(date);
                ref.setValue(counter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e( "recordLoss", "incrementLoss", databaseError.toException() );
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

        // record in results manifest (important_
        Date date = Calendar.getInstance().getTime();
        BattleResult result = new BattleResult( date,
                child.getKey(),
                player1Id,
                p1Choice,
                player2Id,
                p2Choice,
                winnerId );
        child.setValue( result, listener );

        // update statistics
        DatabaseReference p1Char = getCharacterStatistics(player1Id, p1Choice);
        DatabaseReference p2Char = getCharacterStatistics(player2Id, p2Choice);
        DatabaseReference p1p2 = getPlayerVsPlayerRef(player1Id, player2Id);
        DatabaseReference p2p1 = getPlayerVsPlayerRef(player2Id, player1Id);
        DatabaseReference p1p2Chars = getPvpCvcRef( player1Id, player2Id, p1Choice, p2Choice );
        DatabaseReference p2p1Chars = getPvpCvcRef( player2Id, player1Id, p2Choice, p1Choice );

        if ( player1Id.equals(winnerId) ) {
            incrementWin( p1Char, date);
            incrementWin( p1p2, date );
            incrementWin( p1p2Chars, date );

            incrementLoss( p2Char, date);
            incrementLoss( p2p1, date );
            incrementLoss( p2p1Chars, date );
        } else {
            incrementWin( p2Char, date);
            incrementWin( p2p1, date );
            incrementWin( p2p1Chars, date );

            incrementLoss( p1Char, date);
            incrementLoss( p1p2, date );
            incrementLoss( p1p2Chars, date );
        }
    }

    private static String characterKey( String name ) {
        String key = name.toLowerCase().replaceAll( "[^\\p{Lower}]", "");
        return key;
    }

    private DatabaseReference getCharacterPreference( String playerId, String characterName ) {
        String charKey = characterKey(characterName);
        return getPlayerPrefsRef( playerId ).child(charKey);
    }

    public DatabaseReference getPlayerVsPlayerRef( String player1, String player2 ) {
        return mUserHome.child(PLAYER_VS_PLAYER_DIR).child( player1 ).child( player2 ).child(PVP_OVERALL);
    }

    public DatabaseReference getPvpCvcRef( String p1, String p2, String c1, String c2 ) {
        return mUserHome.child(PLAYER_VS_PLAYER_DIR)
                .child(p1)
                .child(p2)
                .child(PVP_CHARACTERS)
                .child( characterKey(c1) )
                .child( characterKey(c2) );
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
            createCharacter( ref, character );
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

    private interface Factory<V> {
        V createInstance( );
    }

    private <K,V> V fetchOrInitialize( Map<K, V> map, K key, Factory<V> factory ) {
        V rv = map.get(key);
        if ( null == rv ) {
            rv = factory.createInstance();
            map.put( key, rv );
        }
        return rv;
    }

    private BattleCounter getOpponentResult( Map<String,BattleCounter> map, String opponent ) {
        BattleCounter rv = map.get(opponent);
        if ( null == rv ) {
            rv = new BattleCounter();
            map.put( opponent, rv );
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

    public DatabaseReference getCharacterStatistics( String playerId, String charName ) {
        return getCharacterPreference(playerId, charName).child( STATISTICS_MEMBER );
    }

    private void doRegenerate( List<BattleResult> results, StatisticsCompleteListener listener ) {

        // analyse results
        Map<Pair<String,String>, BattleCounter> playerCharacterCounter = new HashMap<>();
        Map<Pair<String,String>, BattleCounter> playerToPlayerCounter = new HashMap<>();
        Map<Pair<String,String>, Map<Pair<String,String>, BattleCounter>> playerCharToPlayerChar
                = new HashMap<>();

        for ( BattleResult result : results ) {
            Pair<String,String> p1Char = new Pair<>( result.p1Id, result.p1Character );
            Pair<String,String> p2Char = new Pair<>( result.p2Id, result.p2Character );
            Pair<String,String> p1p2 = new Pair<>( result.p1Id, result.p2Id );
            Pair<String,String> p2p1 = new Pair<>( result.p2Id, result.p1Id );
            Pair<String,String> c1c2 = new Pair<>( result.p1Character, result.p2Character );
            Pair<String,String> c2c1 = new Pair<>( result.p2Character, result.p1Character );

            Factory<BattleCounter> bcF = new Factory<BattleCounter>() {
                @Override
                public BattleCounter createInstance() {
                    return new BattleCounter();
                }
            };
            Factory<Map<Pair<String,String>,BattleCounter>> mbcF =
                    new Factory<Map<Pair<String,String>, BattleCounter>>() {
                        @Override
                        public Map<Pair<String,String>, BattleCounter> createInstance() {
                            return new HashMap<>();
                        }
                    };

            BattleCounter p1Results = fetchOrInitialize( playerCharacterCounter, p1Char, bcF );
            BattleCounter p2Results = fetchOrInitialize( playerCharacterCounter, p2Char, bcF );
            updateResults( p1Results, result.p1Id, p2Results, result.p2Id, result );

            BattleCounter p1Opponent = fetchOrInitialize( playerToPlayerCounter, p1p2, bcF );
            BattleCounter p2Opponent = fetchOrInitialize( playerToPlayerCounter, p2p1, bcF );
            updateResults( p1Opponent, result.p1Id, p2Opponent, result.p2Id, result );

            Map<Pair<String,String>, BattleCounter> p1p2Chars =
                    fetchOrInitialize( playerCharToPlayerChar, p1p2, mbcF );
            Map<Pair<String,String>, BattleCounter> p2p1Chars =
                    fetchOrInitialize( playerCharToPlayerChar, p2p1, mbcF );
            BattleCounter p1Chars = fetchOrInitialize(p1p2Chars, c1c2, bcF );
            BattleCounter p2Chars = fetchOrInitialize(p2p1Chars, c2c1, bcF );
            updateResults( p1Chars, result.p1Id, p2Chars, result.p2Id, result );
        }

        // update database for each character
        Log.d( TAG, "Player to character statistics " + playerCharacterCounter.size());
        for ( Map.Entry<Pair<String,String>,BattleCounter> it : playerCharacterCounter.entrySet() ) {
            Pair<String,String> playerChar = it.getKey();
            String playerId = playerChar.first;
            String charName = playerChar.second;
            BattleCounter result = it.getValue();

            getCharacterStatistics( playerId, charName ).setValue( result );
        }

        // update database for each player vs player combo
        Log.d( TAG, "Player to player statistics" + playerToPlayerCounter.size());
        for ( Map.Entry<Pair<String,String>,BattleCounter> it : playerToPlayerCounter.entrySet() ) {
            String firstPlayer = it.getKey().first;
            String secondPlayer = it.getKey().second;
            BattleCounter result = it.getValue();

            DatabaseReference ref = getPlayerVsPlayerRef( firstPlayer, secondPlayer );
            ref.setValue( result );
        }

        // update database for each player vs player, char vs char combo
        Log.d( TAG, "PvP and CvC" + playerCharToPlayerChar.size());
        for ( Map.Entry<Pair<String,String>,Map<Pair<String,String>,BattleCounter>> it
                : playerCharToPlayerChar.entrySet() ) {
            String p1 = it.getKey().first;
            String p2 = it.getKey().second;

            for ( Map.Entry<Pair<String,String>,BattleCounter> jit : it.getValue().entrySet() ) {
                String c1 = jit.getKey().first;
                String c2 = jit.getKey().second;
                BattleCounter counter = jit.getValue();

                DatabaseReference ref = getPvpCvcRef( p1, p2, c1, c2 );
                ref.setValue(counter);
            }
        }

        listener.statisticsComplete();
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
