package com.example.will.sfclippy.models;

/**
 * Created by will on 24/09/2016.
 */

public class BattleCounter {
    public int wins;
    public int battles;

    public BattleCounter( ) {
        wins = 0;
        battles = 0;
    }

    public void recordWin( ) {
        wins++;
        battles++;
    }

    public void recordLoss( ) {
        battles++;
    }
}
