package com.example.will.sfclippy;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.List;

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

    public DatabaseReference getUsername( String playerId ) {
        return FirebaseHelper.getUser( usersRef, playerId ).getUsername();
    }

    public DatabaseReference getPreferences( String playerId ) {
        return FirebaseHelper.getPreferences( preferencesRef, playerId );
    }

    public DatabaseReference getResults( ) {
        return resultsRef;
    }


    public static class CharacterPreference {
        private String characterName;
        private int score;

        public CharacterPreference( String characterName, int score ) {
            this.characterName = characterName;
            this.score = score;
        }

        /**
         * Copy constructor.
         * @param toCopy The preference to copy.
         */
        public CharacterPreference( CharacterPreference toCopy ) {
            characterName = toCopy.characterName;
            score = toCopy.score;
        }

        public String getCharacterName( ) {
            return characterName;
        }

        public int getScore( ) {
            return score;
        }

        public void cycleScore( ) {
            score = (score + 1) % 3;
        }
    }

    /**
     * Class representing a possible player.
     */
    public static class PlayerInfo {
        private final String playerId;
        private final String playerName;

        public PlayerInfo( String playerId, String playerName ) {
            this.playerId = playerId;
            this.playerName = playerName;
        }

        public String getPlayerId( ) {
            return playerId;
        }

        public String getPlayerName( ) {
            return playerName;
        }
    }

    /**
     * Class representing current state.
     */
    public static class CurrentState {
        private final String player1Id;
        private final String player2Id;

        public CurrentState( String player1Id, String player2Id ) {
            this.player1Id = player1Id;
            this.player2Id = player2Id;

            Log.d( getClass().getName(), "CurrentState (" + player1Id + "," + player2Id + ")");
        }

        public String getPlayer1Id( ) {
            return player1Id;
        }

        public String getPlayer2Id( ) {
            return player2Id;
        }
    }

    PlayerInfo getPlayerById( String id ) {
        return null;
    }

    public String getPlayer1Id( ) {
        return null;
    }

    public String getPlayer2Id( ) {
        return null;
    }

    public String getPlayer1Name( ) {
        return null;
    }

    public String getPlayer2Name( ) {
        return null;
    }

    public HistoricalTrends getHistoricalTrends( ) {
        //return new HistoricalTrends( battleResults );
        return null;
    }

    public void replaceCharacterPreferences( String playerId,
                                             List<DataProvider.CharacterPreference> prefs ) {
    }

    public void backupData(Activity activity, int requestId ) {
    }

    public void saveResults( ) throws IOException {
    }
}
