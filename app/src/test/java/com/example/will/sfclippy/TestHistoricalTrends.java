package com.example.will.sfclippy;

import com.example.will.sfclippy.models.BattleResult;
import com.example.will.sfclippy.models.PlayerInfo;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 22/08/2016.
 */
public class TestHistoricalTrends {
    @Test
    public void testWinRatio( ) {
        PlayerInfo p1Info = new PlayerInfo( "WRL", "Will");

        BattleResult r1 = new BattleResult();
        r1.winnerId = "WRL";
        BattleResult r2 = Mockito.mock(BattleResult.class);
        r2.winnerId = "RW";
        BattleResult r3 = Mockito.mock(BattleResult.class);
        r3.winnerId = "WRL";
        BattleResult r4 = Mockito.mock(BattleResult.class);
        r4.winnerId = "WRL";

        ArrayList<BattleResult> results = new ArrayList<>();
        results.add(r1);
        results.add(r2);
        results.add(r3);
        results.add(r4);

        HistoricalTrends trends = new HistoricalTrends(results);
        List<HistoricalTrends.Fact> facts = new ArrayList<>();
        trends.addWinRatioFact(p1Info, facts);

        TestCase.assertEquals( 1, facts.size() );
        TestCase.assertEquals( "Will has an overall win ratio of 75% (4 battles)", facts.get(0).getInfo() );
        TestCase.assertEquals( 1, facts.get(0).getScore() );
    }

    @Test
    public void testCharacterWinRatio( ) {
        PlayerInfo p1Info = new PlayerInfo( "WRL", "Will");

        BattleResult r1 = Mockito.mock(BattleResult.class);
        r1.winnerId = "WRL";
        Mockito.when(r1.characterFor("WRL")).thenReturn("Cammy");
        BattleResult r2 = Mockito.mock(BattleResult.class);
        r2.winnerId = "RW";
        Mockito.when(r2.characterFor("WRL")).thenReturn("Cammy");
        BattleResult r3 = Mockito.mock(BattleResult.class);
        r3.winnerId = "WRL";
        Mockito.when(r3.characterFor("WRL")).thenReturn("Cammy");
        BattleResult r4 = Mockito.mock(BattleResult.class);
        r4.winnerId = "WRL";
        Mockito.when(r4.characterFor("WRL")).thenReturn("Ryu");

        ArrayList<BattleResult> results = new ArrayList<>();
        results.add(r1);
        results.add(r2);
        results.add(r3);
        results.add(r4);

        HistoricalTrends trends = new HistoricalTrends(results);
        List<HistoricalTrends.Fact> facts = new ArrayList<>();
        trends.addCharacterRatio(p1Info, "Cammy", facts);

        TestCase.assertEquals( 1, facts.size() );
        TestCase.assertEquals( "Will has a 66% win ratio with Cammy (3 battles)", facts.get(0).getInfo() );
        TestCase.assertEquals( 2, facts.get(0).getScore() );
    }

    @Test
    public void testEmptyWinRatio( ) {
        PlayerInfo p1Info = new PlayerInfo( "WRL", "Will");

        BattleResult r1 = Mockito.mock(BattleResult.class);
        r1.winnerId = "WRL";
        Mockito.when(r1.characterFor("WRL")).thenReturn("Ryu");

        ArrayList<BattleResult> results = new ArrayList<>();
        results.add(r1);

        HistoricalTrends trends = new HistoricalTrends(results);
        List<HistoricalTrends.Fact> facts = new ArrayList<>();
        trends.addCharacterRatio(p1Info, "Cammy", facts);

        TestCase.assertEquals( 0, facts.size() );
    }

    @Test
    public void testP1PairingHistory( ) {
        PlayerInfo p1Info = new PlayerInfo( "WRL", "Will");
        PlayerInfo p2Info = new PlayerInfo( "RW", "Ruaidhri");

        BattleResult r1 = Mockito.mock(BattleResult.class);
        r1.winnerId ="WRL";
        Mockito.when(r1.characterFor("WRL")).thenReturn("Ryu");
        Mockito.when(r1.characterFor("RW")).thenReturn("Ken");
        BattleResult r2 = Mockito.mock(BattleResult.class);
        r2.winnerId ="RW";
        Mockito.when(r2.characterFor("WRL")).thenReturn("Ryu");
        Mockito.when(r2.characterFor("RW")).thenReturn("Ken");
        BattleResult r3 = Mockito.mock(BattleResult.class);
        r3.winnerId ="WRL";
        Mockito.when(r3.characterFor("WRL")).thenReturn("Ryu");
        Mockito.when(r3.characterFor("RW")).thenReturn("R.Mika");
        BattleResult r4 = Mockito.mock(BattleResult.class);
        r4.winnerId ="WRL";
        Mockito.when(r4.characterFor("WRL")).thenReturn("Ryu");
        Mockito.when(r4.characterFor("RW")).thenReturn("Ken");

        ArrayList<BattleResult> results = new ArrayList<>();
        results.add(r1);
        results.add(r2);
        results.add(r3);
        results.add(r4);

        HistoricalTrends trends = new HistoricalTrends(results);
        List<HistoricalTrends.Fact> facts = new ArrayList<>();
        trends.addPastBattleRatio(p1Info, "Ryu", p2Info, "Ken", facts);

        TestCase.assertEquals( 1, facts.size());
        TestCase.assertEquals( "Previous results for this pairing favour Will (66%) [3 fights]", facts.get(0).getInfo());
        TestCase.assertEquals( 3, facts.get(0).getScore());
    }

    @Test
    public void testP2PairingHistory( ) {
        PlayerInfo p1Info = new PlayerInfo( "WRL", "Will");
        PlayerInfo p2Info = new PlayerInfo( "RW", "Ruaidhri");

        BattleResult r1 = Mockito.mock(BattleResult.class);
        r1.winnerId ="RW";
        Mockito.when(r1.characterFor("WRL")).thenReturn("Laura");
        Mockito.when(r1.characterFor("RW")).thenReturn("Zangief");
        BattleResult r2 = Mockito.mock(BattleResult.class);
        r2.winnerId ="RW";
        Mockito.when(r2.characterFor("WRL")).thenReturn("Laura");
        Mockito.when(r2.characterFor("RW")).thenReturn("Zangief");
        BattleResult r3 = Mockito.mock(BattleResult.class);
        r3.winnerId ="WRL";
        Mockito.when(r3.characterFor("WRL")).thenReturn("Laura");
        Mockito.when(r3.characterFor("RW")).thenReturn("Zangief");
        BattleResult r4 = Mockito.mock(BattleResult.class);
        r4.winnerId ="WRL";
        Mockito.when(r4.characterFor("WRL")).thenReturn("Laura");
        Mockito.when(r4.characterFor("RW")).thenReturn("Birdie");

        ArrayList<BattleResult> results = new ArrayList<>();
        results.add(r1);
        results.add(r2);
        results.add(r3);
        results.add(r4);

        HistoricalTrends trends = new HistoricalTrends(results);
        List<HistoricalTrends.Fact> facts = new ArrayList<>();
        trends.addPastBattleRatio(p1Info, "Laura", p2Info, "Zangief", facts);

        TestCase.assertEquals( 1, facts.size());
        TestCase.assertEquals( "Previous results for this pairing favour Ruaidhri (66%) [3 fights]", facts.get(0).getInfo());
        TestCase.assertEquals( 3, facts.get(0).getScore());
    }

    @Test
    public void testEvenPairingHistory( ) {
        PlayerInfo p1Info = new PlayerInfo( "WRL", "Will");
        PlayerInfo p2Info = new PlayerInfo( "RW", "Ruaidhri");

        BattleResult r1 = Mockito.mock(BattleResult.class);
        r1.winnerId ="RW";
        Mockito.when(r1.characterFor("WRL")).thenReturn("Ken");
        Mockito.when(r1.characterFor("RW")).thenReturn("Zangief");
        BattleResult r2 = Mockito.mock(BattleResult.class);
        r2.winnerId ="WRL";
        Mockito.when(r2.characterFor("WRL")).thenReturn("Ken");
        Mockito.when(r2.characterFor("RW")).thenReturn("Zangief");

        ArrayList<BattleResult> results = new ArrayList<>();
        results.add(r1);
        results.add(r2);

        HistoricalTrends trends = new HistoricalTrends(results);
        List<HistoricalTrends.Fact> facts = new ArrayList<>();
        trends.addPastBattleRatio(p1Info, "Ken", p2Info, "Zangief", facts);

        TestCase.assertEquals( 1, facts.size());
        TestCase.assertEquals( "Previous results for this pairing are even [2 fights]", facts.get(0).getInfo());
        TestCase.assertEquals( 3, facts.get(0).getScore());
    }

    @Test
    public void testNoPairingHistory( ) {
        PlayerInfo p1Info = new PlayerInfo( "WRL", "Will");
        PlayerInfo p2Info = new PlayerInfo( "RW", "Ruaidhri");

        ArrayList<BattleResult> results = new ArrayList<>();

        HistoricalTrends trends = new HistoricalTrends(results);
        List<HistoricalTrends.Fact> facts = new ArrayList<>();
        trends.addPastBattleRatio(p1Info, "Ken", p2Info, "Zangief", facts);

        TestCase.assertEquals( 1, facts.size());
        TestCase.assertEquals( "No previous results for this pairing", facts.get(0).getInfo());
        TestCase.assertEquals( 3, facts.get(0).getScore());
    }
}
