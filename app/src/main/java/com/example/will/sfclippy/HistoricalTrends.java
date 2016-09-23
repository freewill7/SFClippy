package com.example.will.sfclippy;

import android.util.Log;
import android.util.Pair;

import com.example.will.sfclippy.models.BattleResult;
import com.example.will.sfclippy.models.PlayerInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by will on 22/08/2016.
 */
public class HistoricalTrends {
    private Map<String,BattleCounter> playerToResults = new HashMap<>();
    private Map<String,StringToResults> playerToCharResults = new HashMap<>();
    private Map<BattleKey, BattleCounter> pastBattles = new HashMap<>();

    private int GENERAL_FACT_SCORE = 1;
    private int GENERAL_CHARACTER_FACT_SCORE = 2;
    private int GENERAL_BATTLE_SCORE = 3;

    /**
     * Information about group of battles.
     */
    public class BattleCounter {
        private int wins = 0;
        private int total = 0;

        public BattleCounter( ) {
            // nothing to do
        }

        public BattleCounter( BattleCounter other ) {
            this.wins = other.wins;
            this.total = other.total;
        }

        /**
         * Record a win for the player.
         */
        public void recordWin( ) {
            wins++;
            total++;
        }

        /**
         * Record a loss for the player.
         */
        public void recordLoss( ) {
            total++;
        }

        public int getWinPercentage( ) {
            return wins * 100 / total;
        }

        public int getTotalBattles( ) {
            return total;
        }

        public int getWonBattles( ) { return wins; }
    }

    /**
     * Keeps track of wins by character.
     */
    private class StringToResults {
        public Map<String,BattleCounter> stringToCharacter = new HashMap<>();

        public StringToResults( ) {

        }

        public BattleCounter getBattleFor( String characterName ) {
            BattleCounter counter = stringToCharacter.get( characterName );
            if ( null == counter ) {
                counter = new BattleCounter();
                stringToCharacter.put( characterName, counter );
            }
            return counter;
        }
    }

    private static final class BattleKey {
        private final String p1Id;
        private final String p1Player;
        private final String p2Id;
        private final String p2Player;

        public BattleKey( String p1Id,
                          String p1Player,
                          String p2Id,
                          String p2Player ) {
            this.p1Id = p1Id;
            this.p1Player = p1Player;
            this.p2Id = p2Id;
            this.p2Player = p2Player;

            Log.d( "BattleKey", "Battle key (" + p1Id + "," + p1Player
            + "," + p2Id
            + "," + p2Player );
        }

        @Override
        public boolean equals( Object other ) {
            if ( other instanceof BattleKey ) {
                BattleKey otherKey = (BattleKey) other;
                return p1Id.equals(otherKey.p1Id) &&
                        p1Player.equals(otherKey.p1Player) &&
                        p2Id.equals(otherKey.p2Id) &&
                        p2Player.equals(otherKey.p2Player);
            }
            return false;
        }

        @Override
        public int hashCode( ) {
            return 1;
        }
    }

    /**
     * Represents an interesting fact.
     */
    public class Fact {
        private String info;
        private int score;

        public Fact( String info, int score ) {
            this.info = info;
            this.score = score;
        }

        public String getInfo( ) {
            return info;
        }

        public int getScore( ) {
            return score;
        }
    }

    public HistoricalTrends( ) {

    }

    public String formatPastBattleWinner( PlayerInfo player,
                                          int percent,
                                          int totalBattles ) {
        return "Previous results for this pairing favour " + player.playerName +
                    " (" + percent + "% wins for " + totalBattles + ")";
    }

    public String formatPastBattles( PlayerInfo player1,
                                     PlayerInfo player2,
                                     BattleCounter results ) {
        int p1Percent = results.getWinPercentage();
        int totalCounter = results.getTotalBattles();

        if ( 50 == results.getWinPercentage() ) {
            return "Previous results for this pairing are even (" +
                    results.getTotalBattles() + " battles)";
        } else if ( results.getWinPercentage() < 50 ) {
            return formatPastBattleWinner( player2, (100-p1Percent), totalCounter );
        } else {
            return formatPastBattleWinner( player1, p1Percent, totalCounter );
        }
    }

    private String formatCharacterStats( PlayerInfo playerInfo,
                                         String playerChoice,
                                         BattleCounter counter) {
        String ret = playerInfo.playerName + " has a " + counter.getWinPercentage() + "% win ratio "
                + "with " + playerChoice + " (" + counter.getTotalBattles() + " battles)";
        return ret;
    }

    private BattleCounter lookupPlayerCharacter( PlayerInfo player, String playerChoice ) {
        BattleCounter counter = new BattleCounter();
        StringToResults playerChar = playerToCharResults.get(player.playerId);
        if ( null != playerChar ) {
            counter = playerChar.getBattleFor(playerChoice);
        }
        return counter;
    }

    private void addCharacterStats( List<Fact> facts,
                                    PlayerInfo player,
                                    String playerChoice ) {
        BattleCounter counter = lookupPlayerCharacter( player, playerChoice );
        if ( counter.getTotalBattles() > 0 ) {
            String info = formatCharacterStats( player, playerChoice, counter );
            facts.add( new Fact(info, GENERAL_CHARACTER_FACT_SCORE));
        }
    }

    private void addOverallStats( List<Fact> facts,
                                  PlayerInfo player ) {
        BattleCounter counter = playerToResults.get( player.playerId );
        if ( null != counter ) {
            int percent = counter.getWinPercentage();
            int wins = counter.getWonBattles();
            int total = counter.getTotalBattles();
            String ret =  player.playerName + " has an overall win ratio of " + percent + "%"
                    + " (" + wins + " wins from " + total + " battles)";
            facts.add( new Fact(ret, GENERAL_FACT_SCORE));
        }
    }

    public List<Fact> getBattleFacts(PlayerInfo player1,
                                     String player1Choice,
                                     PlayerInfo player2,
                                     String player2Choice,
                                     Date date ) {
        List<Fact> ret = new ArrayList<>();

        Log.d( "HistoricalTrends", "Fetching from " + pastBattles.size());

        // find previous stats for this battle
        if ( ! player1Choice.equals("unknown") && ! player2Choice.equals("unknown") ) {
            BattleKey key = new BattleKey(player1.playerId, player1Choice,
                    player2.playerId, player2Choice);
            BattleCounter pastBattle = pastBattles.get(key);
            if (null != pastBattle && pastBattle.getTotalBattles() > 0) {
                String info = formatPastBattles(player1, player2, pastBattle);
                ret.add(new Fact(info, GENERAL_BATTLE_SCORE));
            }

            // find character stats for player 1
            addCharacterStats(ret, player1, player1Choice);

            // find character status for player 2
            addCharacterStats(ret, player2, player2Choice);
        }

        // overall stats for player 1
        addOverallStats( ret, player1 );

        // overally stats for player 2
        addOverallStats( ret, player2 );

        return ret;
    }

    private void addGeneralResult( String playerId, BattleResult result ) {
        BattleCounter overall = playerToResults.get( playerId );
        if ( null == overall ) {
            overall = new BattleCounter();
            playerToResults.put( playerId, overall );
        }

        if ( result.winnerId.equals(playerId) ) {
            overall.recordWin();
        } else {
            overall.recordLoss();
        }
    }

    private void addByCharacterResult( String playerId, BattleResult result ) {
        StringToResults characterMap = playerToCharResults.get( playerId );
        if ( null == characterMap ) {
            characterMap = new StringToResults();
            playerToCharResults.put( playerId, characterMap );
        }

        BattleCounter counter = characterMap.getBattleFor( result.characterFor(playerId) );
        if ( result.winnerId.equals(playerId) ) {
            counter.recordWin();
        } else {
            counter.recordLoss();
        }
    }

    private Pair<String,String> formPlayerPair( String p1Id, String p2Id ) {
        if ( 0 < p1Id.compareTo(p2Id) ) {
            return new Pair<>( p1Id, p2Id );
        } else {
            return new Pair<>( p2Id, p1Id );
        }
    }

    private void addByBattleResult( String p1Id, String p2Id, BattleResult result ) {
        Log.d( "HistoricalTrends", "Storing");
        BattleKey key = new BattleKey( p1Id, result.characterFor(p1Id),
                p2Id, result.characterFor(p2Id) );

        BattleCounter counter = pastBattles.get(key);
        if ( null == counter ) {
            counter = new BattleCounter();
            pastBattles.put( key, counter );
        }

        if ( result.winnerId.equals(p1Id) ) {
            counter.recordWin();
        } else {
            counter.recordLoss();
        }
    }

    public void addBattle( BattleResult result ) {
        addGeneralResult( result.p1Id, result );
        addGeneralResult( result.p2Id, result );

        addByCharacterResult( result.p1Id, result );
        addByCharacterResult( result.p2Id, result );

        addByBattleResult( result.p1Id, result.p2Id, result );
        addByBattleResult( result.p2Id, result.p1Id, result );
    }

    public Map<String,BattleCounter> getPlayerCharacterStats( PlayerInfo player ) {
        StringToResults lookup = playerToCharResults.get( player.playerId );
        Map<String,BattleCounter> ret = new HashMap<>();
        if ( null != lookup ) {
            ret = lookup.stringToCharacter;
        }
        return ret;
    }
}
