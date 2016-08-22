package com.example.will.sfclippy;

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
        DataProvider.PlayerInfo p1Info = new DataProvider.PlayerInfo( "WRL", "Will");

        DataProvider.BattleResult r1 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r1.getWinnerId()).thenReturn("WRL");
        DataProvider.BattleResult r2 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r2.getWinnerId()).thenReturn("RW");
        DataProvider.BattleResult r3 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r3.getWinnerId()).thenReturn("WRL");
        DataProvider.BattleResult r4 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r4.getWinnerId()).thenReturn("WRL");

        ArrayList<DataProvider.BattleResult> results = new ArrayList<>();
        results.add(r1);
        results.add(r2);
        results.add(r3);
        results.add(r4);

        HistoricalTrends trends = new HistoricalTrends(results);
        List<HistoricalTrends.Fact> facts = new ArrayList<>();
        trends.addWinRatioFact(p1Info, facts);

        TestCase.assertEquals( 1, facts.size() );
        TestCase.assertEquals( "Will has an overall win ratio of 75%", facts.get(0).getInfo() );
        TestCase.assertEquals( 1, facts.get(0).getScore() );
    }

    @Test
    public void testCharacterWinRatio( ) {
        DataProvider.PlayerInfo p1Info = new DataProvider.PlayerInfo( "WRL", "Will");

        DataProvider.BattleResult r1 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r1.getWinnerId()).thenReturn("WRL");
        Mockito.when(r1.characterFor("WRL")).thenReturn("Cammy");
        DataProvider.BattleResult r2 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r2.getWinnerId()).thenReturn("RW");
        Mockito.when(r2.characterFor("WRL")).thenReturn("Cammy");
        DataProvider.BattleResult r3 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r3.getWinnerId()).thenReturn("WRL");
        Mockito.when(r3.characterFor("WRL")).thenReturn("Cammy");
        DataProvider.BattleResult r4 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r4.getWinnerId()).thenReturn("WRL");
        Mockito.when(r4.characterFor("WRL")).thenReturn("Ryu");

        ArrayList<DataProvider.BattleResult> results = new ArrayList<>();
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
        DataProvider.PlayerInfo p1Info = new DataProvider.PlayerInfo( "WRL", "Will");

        DataProvider.BattleResult r1 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r1.getWinnerId()).thenReturn("WRL");
        Mockito.when(r1.characterFor("WRL")).thenReturn("Ryu");

        ArrayList<DataProvider.BattleResult> results = new ArrayList<>();
        results.add(r1);

        HistoricalTrends trends = new HistoricalTrends(results);
        List<HistoricalTrends.Fact> facts = new ArrayList<>();
        trends.addCharacterRatio(p1Info, "Cammy", facts);

        TestCase.assertEquals( 0, facts.size() );
    }

    @Test
    public void testP1PairingHistory( ) {
        DataProvider.PlayerInfo p1Info = new DataProvider.PlayerInfo( "WRL", "Will");
        DataProvider.PlayerInfo p2Info = new DataProvider.PlayerInfo( "RW", "Ruaidhri");

        DataProvider.BattleResult r1 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r1.getWinnerId()).thenReturn("WRL");
        Mockito.when(r1.characterFor("WRL")).thenReturn("Ryu");
        Mockito.when(r1.characterFor("RW")).thenReturn("Ken");
        DataProvider.BattleResult r2 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r2.getWinnerId()).thenReturn("RW");
        Mockito.when(r2.characterFor("WRL")).thenReturn("Ryu");
        Mockito.when(r2.characterFor("RW")).thenReturn("Ken");
        DataProvider.BattleResult r3 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r3.getWinnerId()).thenReturn("WRL");
        Mockito.when(r3.characterFor("WRL")).thenReturn("Ryu");
        Mockito.when(r3.characterFor("RW")).thenReturn("R.Mika");
        DataProvider.BattleResult r4 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r4.getWinnerId()).thenReturn("WRL");
        Mockito.when(r4.characterFor("WRL")).thenReturn("Ryu");
        Mockito.when(r4.characterFor("RW")).thenReturn("Ken");

        ArrayList<DataProvider.BattleResult> results = new ArrayList<>();
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
        DataProvider.PlayerInfo p1Info = new DataProvider.PlayerInfo( "WRL", "Will");
        DataProvider.PlayerInfo p2Info = new DataProvider.PlayerInfo( "RW", "Ruaidhri");

        DataProvider.BattleResult r1 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r1.getWinnerId()).thenReturn("RW");
        Mockito.when(r1.characterFor("WRL")).thenReturn("Laura");
        Mockito.when(r1.characterFor("RW")).thenReturn("Zangief");
        DataProvider.BattleResult r2 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r2.getWinnerId()).thenReturn("RW");
        Mockito.when(r2.characterFor("WRL")).thenReturn("Laura");
        Mockito.when(r2.characterFor("RW")).thenReturn("Zangief");
        DataProvider.BattleResult r3 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r3.getWinnerId()).thenReturn("WRL");
        Mockito.when(r3.characterFor("WRL")).thenReturn("Laura");
        Mockito.when(r3.characterFor("RW")).thenReturn("Zangief");
        DataProvider.BattleResult r4 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r4.getWinnerId()).thenReturn("WRL");
        Mockito.when(r4.characterFor("WRL")).thenReturn("Laura");
        Mockito.when(r4.characterFor("RW")).thenReturn("Birdie");

        ArrayList<DataProvider.BattleResult> results = new ArrayList<>();
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
        DataProvider.PlayerInfo p1Info = new DataProvider.PlayerInfo( "WRL", "Will");
        DataProvider.PlayerInfo p2Info = new DataProvider.PlayerInfo( "RW", "Ruaidhri");

        DataProvider.BattleResult r1 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r1.getWinnerId()).thenReturn("RW");
        Mockito.when(r1.characterFor("WRL")).thenReturn("Ken");
        Mockito.when(r1.characterFor("RW")).thenReturn("Zangief");
        DataProvider.BattleResult r2 = Mockito.mock(DataProvider.BattleResult.class);
        Mockito.when(r2.getWinnerId()).thenReturn("WRL");
        Mockito.when(r2.characterFor("WRL")).thenReturn("Ken");
        Mockito.when(r2.characterFor("RW")).thenReturn("Zangief");

        ArrayList<DataProvider.BattleResult> results = new ArrayList<>();
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
        DataProvider.PlayerInfo p1Info = new DataProvider.PlayerInfo( "WRL", "Will");
        DataProvider.PlayerInfo p2Info = new DataProvider.PlayerInfo( "RW", "Ruaidhri");

        ArrayList<DataProvider.BattleResult> results = new ArrayList<>();

        HistoricalTrends trends = new HistoricalTrends(results);
        List<HistoricalTrends.Fact> facts = new ArrayList<>();
        trends.addPastBattleRatio(p1Info, "Ken", p2Info, "Zangief", facts);

        TestCase.assertEquals( 1, facts.size());
        TestCase.assertEquals( "No previous results for this pairing", facts.get(0).getInfo());
        TestCase.assertEquals( 3, facts.get(0).getScore());
    }
}
