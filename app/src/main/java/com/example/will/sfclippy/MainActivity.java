package com.example.will.sfclippy;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends Activity
implements View.OnClickListener {
    private DataProvider dataProvider;

    private String p1Choice;
    private String p2Choice;
    private TextView p1Text;
    private TextView p2Text;
    private Button p1Button;
    private Button p2Button;
    private Button btnResults;
    private Button p1Win;
    private Button p2Win;
    private Button btnP1Preferences;
    private Button btnP2Preferences;

    static public final int GET_P1_CHARACTER = 1;
    static public final int GET_P2_CHARACTER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.dataProvider = AppSingleton.getInstance().getDataProvider();

        p1Choice = "unknown";
        p2Choice = "unknown";

        p1Text = (TextView) findViewById(R.id.textP1);
        p1Text.setText( dataProvider.getPlayer1Name() + " choice:");
        p2Text = (TextView) findViewById(R.id.textP2);
        p2Text.setText( dataProvider.getPlayer2Name() + " choice:");

        p1Button = (Button) findViewById(R.id.btnChoiceP1);
        p1Button.setOnClickListener( this );

        p2Button = (Button) findViewById(R.id.btnChoiceP2);
        p2Button.setOnClickListener( this );

        btnResults = (Button) findViewById(R.id.btnResults);
        btnResults.setOnClickListener( this );

        p1Win = (Button) findViewById(R.id.btnWinP1);
        p1Win.setOnClickListener( this );

        p2Win = (Button) findViewById(R.id.btnWinP2);
        p2Win.setOnClickListener( this );

        btnP1Preferences = (Button) findViewById(R.id.btnPlayer1Prefs);
        btnP1Preferences.setOnClickListener( this );

        btnP2Preferences = (Button) findViewById(R.id.btnPlayer2Prefs);
        btnP2Preferences.setOnClickListener( this );
    }

    private static class RecordWinTask extends AsyncTask<Void,Void,Void> {
        private String p1Choice;
        private String p2Choice;
        private String winnerId;

        public RecordWinTask( String p1Choice,
                              String p2Choice,
                              String winnerId ) {
            this.p1Choice = p1Choice;
            this.p2Choice = p2Choice;
            this.winnerId = winnerId;
        }

        @Override
        protected Void doInBackground( Void... params ) {

            try {
                AppSingleton.getInstance().getDataProvider().recordWin(
                        Calendar.getInstance().getTime(), p1Choice, p2Choice, winnerId);
            } catch ( IOException ioe ) {
                Log.e( getClass().getName(), "Failed to record win- check sync");
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute( Void result ) {
            Log.d( getClass().getName(), "Update complete" );
        }
    }

    private void recordWin( String winner ) {
        // TODO loading screen
        Log.d( getLocalClassName(), "Recording win for " + winner );
        RecordWinTask record = new RecordWinTask( p1Choice, p2Choice, winner );
        record.execute( );
    }

    @Override
    public void onClick( View v ) {
        if ( p1Button == v ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);

            String player1Id = dataProvider.getPlayer1Id();
            intent.putExtra( CharacterSelectActivity.PLAYER_ID, player1Id );

            startActivityForResult(intent, GET_P1_CHARACTER,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle() );
        } else if ( p2Button == v ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);

            String player2Id = dataProvider.getPlayer2Id();
            intent.putExtra( CharacterSelectActivity.PLAYER_ID, player2Id );

            startActivityForResult(intent, GET_P2_CHARACTER,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle() );
        } else if ( p1Win == v ) {
            recordWin(dataProvider.getPlayer1Id());
        } else if ( p2Win == v ) {
            recordWin(dataProvider.getPlayer2Id());
        } else if ( btnResults == v ) {
            Intent intent = new Intent(this, ResultsActivity.class);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else if ( btnP1Preferences == v ) {
            Intent intent = new Intent(this, CharacterPreferenceActivity.class);
            intent.putExtra( CharacterPreferenceActivity.PLAYER_ID_PROPERTY,
                    dataProvider.getPlayer1Id());
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else if ( btnP2Preferences == v ) {
            Intent intent = new Intent(this, CharacterPreferenceActivity.class);
            intent.putExtra( CharacterPreferenceActivity.PLAYER_ID_PROPERTY,
                    dataProvider.getPlayer2Id());
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else {
            Toast t = Toast.makeText( v.getContext(), "Unknown button", Toast.LENGTH_SHORT );
            t.show();
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if ( requestCode == GET_P1_CHARACTER ) {
            if ( null != data ) {
                p1Choice = data.getStringExtra( CharacterSelectActivity.GET_CHARACTER_PROPERTY );
                p1Button.setText( p1Choice );
            }
        } else if ( requestCode == GET_P2_CHARACTER ) {
            if ( null != data ) {
                p2Choice = data.getStringExtra( CharacterSelectActivity.GET_CHARACTER_PROPERTY );
                p2Button.setText( p2Choice );
            }
        } else {
            Toast t = Toast.makeText( this.getApplicationContext(),
                    "Unrecognised Activity result", Toast.LENGTH_SHORT );
            t.show();
        }
    }
}
