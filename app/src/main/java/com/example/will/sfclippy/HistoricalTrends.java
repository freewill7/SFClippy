package com.example.will.sfclippy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by will on 22/08/2016.
 */
public class HistoricalTrends {
    private List<DataProvider.BattleResult> pastResults;
    private int GENERAL_FACT_SCORE = 1;
    private int GENERAL_CHARACTER_FACT_SCORE = 2;
    private int GENERAL_BATTLE_SCORE = 3;

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

    public HistoricalTrends( List<DataProvider.BattleResult> pastResults ) {
        this.pastResults = pastResults;
    }

    private float calculateWinRatio( String player1Id ) {
        int p1Count = 0;
        int p2Count = 0;
        for ( DataProvider.BattleResult result : pastResults ) {
            if ( 0 == result.getWinnerId().compareTo(player1Id) ) {
                p1Count++;
            } else {
                p2Count++;
            }
        }

        return (float) p1Count / (float) (p1Count + p2Count);
    }

    protected void addWinRatioFact(DataProvider.PlayerInfo p1Info,
                                   List<Fact> facts ) {
        float p1Ratio = calculateWinRatio( p1Info.getPlayerId() );
        String description =
                p1Info.getPlayerName() + " has an overall win ratio of " + (int) (p1Ratio*100) + "%";
        facts.add( new Fact(description, GENERAL_FACT_SCORE));
    }

    protected void addCharacterRatio( DataProvider.PlayerInfo playerInfo,
                                      String character,
                                      List<Fact> facts ) {
        int wins = 0;
        int losses = 0;
        String playerId = playerInfo.getPlayerId();

        for ( DataProvider.BattleResult result : pastResults ) {
            if ( 0 == result.characterFor( playerId ).compareTo( character ) ) {
                if ( 0 == result.getWinnerId().compareTo(playerId) ) {
                    wins++;
                } else {
                    losses++;
                }
            }
        }

        if ( wins + losses > 0 ) {
            int percent = (int) (100 * (((float) wins) / (float) (wins+losses)));
            int battles = wins + losses;
            String description =
                    playerInfo.getPlayerName() + " has a " + percent + "% win ratio with "
                    + character + " (" + battles + " battles)";
            facts.add( new Fact(description, GENERAL_CHARACTER_FACT_SCORE));
        }
    }

    private String formatWinner( String playerName, int winCount, int total ) {
        int percent = (int) (100 * ((float) winCount / (float) total));
        String description = "Previous results for this pairing favour " +
                playerName + " (" + percent + "%)";
        return description;
    }

    protected void addPastBattleRatio( DataProvider.PlayerInfo p1Info,
                                       String p1Character,
                                       DataProvider.PlayerInfo p2Info,
                                       String p2Character,
                                       List<Fact> facts ) {
        int p1Count = 0;
        int p2Count = 0;

        String p1Id = p1Info.getPlayerId();
        String p2Id = p2Info.getPlayerId();

        for ( DataProvider.BattleResult result : pastResults ) {
            if ( 0 == result.characterFor(p1Id).compareTo(p1Character)
                    && 0 == result.characterFor(p2Id).compareTo(p2Character) ) {
                if ( 0 == result.getWinnerId().compareTo(p1Id) ) {
                    p1Count++;
                } else if ( 0 == result.getWinnerId().compareTo(p2Id) ) {
                    p2Count++;
                }
            }
        }

        int total = p1Count + p2Count;
        if ( total > 0 ) {
            String description = "Previous results for this pairing are even";
            if ( p1Count > p2Count ) {
                description = formatWinner( p1Info.getPlayerName(), p1Count, total );
            } else if ( p2Count > p1Count ){
                description = formatWinner( p2Info.getPlayerName(), p2Count, total );
            }
            description = description + " [" + total + " fights]";
            facts.add( new Fact( description, GENERAL_BATTLE_SCORE ) );
        } else {
            String description = "No previous results for this pairing";
            facts.add( new Fact(description, GENERAL_BATTLE_SCORE));
        }
    }

    public List<Fact> getBattleFacts(DataProvider.PlayerInfo player1,
                                     String player1Choice,
                                     DataProvider.PlayerInfo player2,
                                     String player2Choice,
                                     Date date ) {
        List<Fact> ret = new ArrayList<>();

        if ( pastResults.size() > 0 ) {
            addWinRatioFact( player1, ret );

            addCharacterRatio( player1, player1Choice, ret );
            addCharacterRatio( player2, player2Choice, ret );
            addPastBattleRatio( player1, player1Choice, player2, player2Choice, ret );

        }

        return ret;
    }
}
