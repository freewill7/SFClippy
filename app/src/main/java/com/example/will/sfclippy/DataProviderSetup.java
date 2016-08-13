package com.example.will.sfclippy;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by will on 13/08/2016.
 */
public class DataProviderSetup extends AsyncTask<Void,String,DataProvider> {
    private DriveHelper helper;
    private NotifyInterface callback;

    public interface NotifyInterface {
        void onProgressUpdate( String stage );
        void onError( Exception e );
        void onComplete( DataProvider provider );
    }

    public DataProviderSetup( DriveHelper helper, NotifyInterface callback ) {
        this.helper = helper;
        this.callback = callback;
    }

    @Override
    public void onProgressUpdate( String... progress ) {
        Log.d( getClass().getName(), progress[0]);
        callback.onProgressUpdate(progress[0]);
    }

    @Override
    public DataProvider doInBackground( Void... params ) {

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

        return new DataProvider( helper, playerInfo, results, state );
    }

    @Override
    public void onPostExecute( DataProvider provider ) {
        callback.onComplete( provider );
    }
}
