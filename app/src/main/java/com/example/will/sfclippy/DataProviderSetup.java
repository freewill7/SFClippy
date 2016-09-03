package com.example.will.sfclippy;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by will on 13/08/2016.
 */
public class DataProviderSetup extends AsyncTask<Void,String,DataProvider> {
    private DriveHelper helper;
    private NotifyInterface callback;
    private Exception lastError;

    public interface NotifyInterface {
        void onDataProviderUpdate( String stage );
        void onDataProviderSetup( DataProvider provider );
    }

    public DataProviderSetup( DriveHelper helper, NotifyInterface callback ) {
        this.helper = helper;
        this.callback = callback;
    }

    @Override
    public void onProgressUpdate( String... progress ) {
        Log.d( getClass().getName(), progress[0]);
        callback.onDataProviderUpdate(progress[0]);
    }

    private List<DataProvider.CharacterPreference> defaultPreferences( ) {
        List<DataProvider.CharacterPreference> preferences = new ArrayList<>();
        preferences.add( new DataProvider.CharacterPreference("Ryu",1));
        preferences.add( new DataProvider.CharacterPreference("Chun-Li",1));
        preferences.add( new DataProvider.CharacterPreference("Nash",1));
        preferences.add( new DataProvider.CharacterPreference("M.Bison",1));
        preferences.add( new DataProvider.CharacterPreference("Cammy",1));
        preferences.add( new DataProvider.CharacterPreference("Birdie",1));
        preferences.add( new DataProvider.CharacterPreference("Ken",1));
        preferences.add( new DataProvider.CharacterPreference("Necalli",1));
        preferences.add( new DataProvider.CharacterPreference("Vega",1));
        preferences.add( new DataProvider.CharacterPreference("R.Mika",1));
        preferences.add( new DataProvider.CharacterPreference("Rashid",1));
        preferences.add( new DataProvider.CharacterPreference("Karin",1));
        preferences.add( new DataProvider.CharacterPreference("Zangief",1));
        preferences.add( new DataProvider.CharacterPreference("Laura",1));
        preferences.add( new DataProvider.CharacterPreference("Dhalsim",1));
        preferences.add( new DataProvider.CharacterPreference("F.A.N.G.",1));
        preferences.add( new DataProvider.CharacterPreference("Alex",1));
        preferences.add( new DataProvider.CharacterPreference("Guile",1));
        preferences.add( new DataProvider.CharacterPreference("Ibuki",1));
        preferences.add( new DataProvider.CharacterPreference("Balrog",1));
        preferences.add( new DataProvider.CharacterPreference("Juri",1));
        return preferences;
    }

    private List<DataProvider.CharacterPreference> fetchOrBootstrapPlayers( String playerId,
                                                                            String description ) {
        publishProgress( "Fetching " + description + " characters..." );
        List<DataProvider.CharacterPreference> chars = helper.fetchCharacters( playerId );
        if ( null == chars ) {
            publishProgress( "Creating " + description + " characters...");
            chars = defaultPreferences();

            try {
                helper.storeCharacters( playerId, chars );
            } catch ( IOException ioe ) {
                Log.e(getClass().getName(), "Failed to store " + description + " characters", ioe);
                cancel(true);
            }
        }
        Log.d( getClass().getName(), description + " count " + chars.size());

        return chars;
    }

    @Override
    public DataProvider doInBackground( Void... params ) {

        Semaphore semaphore = new Semaphore(1);

        try {
            semaphore.acquire();
        } catch ( InterruptedException interrupted ) {
            lastError = interrupted;
            cancel(true);
            return null;
        }



        publishProgress( "Fetching players..." );
        List<DataProvider.PlayerInfo> playerInfo = helper.fetchPlayers();
        if ( null == playerInfo ) {
            publishProgress( "Creating new players file..." );
            playerInfo = new ArrayList<>();
            playerInfo.add( new DataProvider.PlayerInfo(
                    UUID.randomUUID().toString(), "Red Panda" ));
            playerInfo.add( new DataProvider.PlayerInfo(
                    UUID.randomUUID().toString(), "Blue Goose" ));

            try {
                helper.storePlayers(playerInfo);
            } catch ( IOException ioe ) {
                Log.e(getClass().getName(), "Failed to store players", ioe);
                cancel(true);
            }
        }
        Log.d( getClass().getName(), "Player count: " + playerInfo.size());

        publishProgress( "Fetching results..." );
        List<DataProvider.BattleResult> results = helper.fetchResults();
        if ( null == results ) {
            publishProgress( "Creating new results file... ");
            results = new ArrayList<>();

            try {
                helper.storeResults(results);
            } catch ( IOException ioe ) {
                Log.e(getClass().getName(), "Failed to store results", ioe);
                cancel(true);
            }
        }
        Log.d( getClass().getName(), "Results count: " + results.size());

        publishProgress( "Fetching current state..." );
        DataProvider.CurrentState state = helper.fetchState();
        if ( null == state ) {
            publishProgress( "Creating new state file... ");
            state = new DataProvider.CurrentState( playerInfo.get(0).getPlayerId(),
                    playerInfo.get(1).getPlayerId() );

            try {
                helper.storeState(state);
            } catch ( IOException ioe ) {
                Log.e(getClass().getName(), "Failed to store state", ioe);
                cancel(true);
            }
        }
        Log.d( getClass().getName(), "P1 id: " + state.getPlayer1Id());
        Log.d( getClass().getName(), "P2 id: " + state.getPlayer2Id());

        publishProgress( "Fetching player 1 characters...");
        List<DataProvider.CharacterPreference> p1Chars = fetchOrBootstrapPlayers(
                state.getPlayer1Id(),
                "P1" );
        List<DataProvider.CharacterPreference> p2Chars = fetchOrBootstrapPlayers(
                state.getPlayer2Id(),
                "P2" );

        //return new DataProvider( helper, playerInfo, results, state, p1Chars, p2Chars );
        return null;
    }

    @Override
    public void onPostExecute( DataProvider provider ) {
        callback.onDataProviderSetup( provider );
    }
}
