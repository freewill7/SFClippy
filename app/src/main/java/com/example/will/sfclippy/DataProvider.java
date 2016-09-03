package com.example.will.sfclippy;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    private String player1Id;
    private String player2Id;
    private String player1Name;
    private String player2Name;
    private Map<String,String> players;
    //private Map<String,PojoResult> results;



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
     * Represents a battle result.
     */
    public static class BattleResult {
        private final Date date;
        private final String p1Id;
        private final String p1Character;
        private final String p2Id;
        private final String p2Character;
        private final String winnerId;

        public BattleResult( Date date,
                              String p1Id,
                              String p1Character,
                              String p2Id,
                              String p2Character,
                              String winnerId ) {
            this.date = date;
            this.p1Id = p1Id;
            this.p1Character = p1Character;
            this.p2Id = p2Id;
            this.p2Character = p2Character;
            this.winnerId = winnerId;

            Log.d( getClass().getName(), "BattleResult ("
                    + date + "," + p1Id + "," + p1Character + "," + p2Id + "," + p2Character
                    + winnerId + ")");
        }

        public Date getDate( ) {
            return date;
        }

        public String getP1Id( ) {
            return p1Id;
        }

        public String getP1Character( ) {
            return p1Character;
        }

        public String getP2Id( ) {
            return p2Id;
        }

        public String getP2Character( ) {
            return p2Character;
        }

        public String getWinnerId( ) {
            return winnerId;
        }

        /**
         * Fetch the character used by a given player id.
         * @param playerId The player id we're interested in.
         * @return The character the player used.
         */
        public String characterFor( String playerId ) {
            if ( 0 == playerId.compareTo(p1Id)) {
                return p1Character;
            } else if ( 0 == playerId.compareTo(p2Id)) {
                return p2Character;
            } else {
                return "unknown";
            }
        }

        /**
         * Return whether the provided player on the battle.
         * @param playerId The player id to look up results for.
         */
        public boolean winner( String playerId ) {
            if ( 0 == playerId.compareTo(winnerId)) {
                return true;
            }
            return false;
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

    public List<CharacterPreference> getCharacterPreferences( String playerId ) {
        return null;
    }

    public void recordWin( Date time, String player1Choice, String player2Choice, String winner )
    throws IOException {
    }

    public List<BattleResult> getCurrentPlayerResults( ) {
        // TODO filter by current players
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
