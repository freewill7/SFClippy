package com.example.will.sfclippy;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by will on 10/08/2016.
 */
public class DataProvider {
    private DriveHelper helper;
    private List<PlayerInfo> players;
    private List<BattleResult> battleResults;
    private List<CharacterPreference> p1Preferences;
    private List<CharacterPreference> p2Preferences;
    private CurrentState currentState;

    public static class CharacterPreference {
        private String characterName;
        private int score;

        public CharacterPreference( String characterName, int score ) {
            this.characterName = characterName;
            this.score = score;
        }

        public String getCharacterName( ) {
            return characterName;
        }

        public int getScore( ) {
            return score;
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

            Log.d( getClass().getName(), "PlayerInfo (" + playerId + "," + playerName + ")");
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

    public DataProvider( DriveHelper helper,
                         List<PlayerInfo> players,
                         List<BattleResult> battles,
                         CurrentState currentState,
                         List<CharacterPreference> p1Preferences,
                         List<CharacterPreference> p2Preferences ) {
        this.helper = helper;
        this.players = players;
        this.battleResults = battles;
        this.currentState = currentState;
        this.p1Preferences = p1Preferences;
        this.p2Preferences = p2Preferences;
    }

    PlayerInfo getPlayerById( String id ) {
        for ( PlayerInfo player : players ) {
            if ( 0 == player.getPlayerId().compareTo(id) ) {
                return player;
            }
        }
        return null;
    }

    public String getPlayer1Id( ) {
        return currentState.getPlayer1Id();
    }

    public String getPlayer2Id( ) {
        return currentState.getPlayer2Id();
    }

    public String getPlayer1Name( ) {
        String ret = "unknown";
        PlayerInfo info = getPlayerById( getPlayer1Id() );
        if ( null != info ) {
            ret = info.getPlayerName();
        }
        return ret;
    }

    public String getPlayer2Name( ) {
        String ret = "unknown";
        PlayerInfo info = getPlayerById( getPlayer2Id() );
        if ( null != info ) {
            ret = info.getPlayerName();
        }
        return ret;
    }

    public List<CharacterPreference> getCharacterPreferences( String playerId ) {
        if ( 0 == playerId.compareTo( getPlayer1Id() )) {
            return p1Preferences;
        } else if ( 0 == playerId.compareTo( getPlayer2Id() )) {
            return p2Preferences;
        } else {
            Log.e( getClass().getName(), "Unknown player " + playerId );
            return new ArrayList<>();
        }
    }

    public void recordWin( Date time, String player1Choice, String player2Choice, String winner )
    throws IOException {
        battleResults.add( new BattleResult(time, getPlayer1Id(), player1Choice,
                getPlayer2Id(), player2Choice,
                winner ));

        helper.storeResults( battleResults );
    }

    public List<BattleResult> getCurrentPlayerResults( ) {
        // TODO filter by current players
        return battleResults;
    }
}
