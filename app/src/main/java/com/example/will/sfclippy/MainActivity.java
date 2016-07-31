package com.example.will.sfclippy;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity
implements View.OnClickListener {

    private Button p1Button;
    private Button p2Button;
    private Button p1Win;
    private Button p2Win;

    private static String p1Name = "Ruaidhri";
    private static String p2Name = "Will";

    static final int REQUEST_AUTHORIZATION = 1001;
    static public final int GET_P1_CHARACTER = 1;
    static public final int GET_P2_CHARACTER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        p1Button = (Button) findViewById(R.id.btnChoiceP1);
        p1Button.setOnClickListener( this );

        p2Button = (Button) findViewById(R.id.btnChoiceP2);
        p2Button.setOnClickListener( this );

        p1Win = (Button) findViewById(R.id.btnWinP1);
        p1Win.setOnClickListener( this );

        p2Win = (Button) findViewById(R.id.btnWinP2);
        p2Win.setOnClickListener( this );
    }

    private void recordWin( String winner ) {
        try {
            StorageService ss = AppSingleton.getInstance().getStorageService();

            Calendar c = Calendar.getInstance();
            int year = c.get( Calendar.YEAR );
            int month = c.get( Calendar.MONTH ) + 1;
            int day = c.get( Calendar.DAY_OF_MONTH );

            String date = "" + year + "-" + month + "-" + day;
            RecordBattleTask task = new RecordBattleTask(ss, this.getApplicationContext(),
                    date, p1Button.getText().toString(), p2Button.getText().toString(), winner );
            task.execute();
        } catch ( Exception e ) {
            System.out.println("Failed to record battle: " + e.getMessage());
        }
    }

    @Override
    public void onClick( View v ) {
        if ( p1Button == v ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);
            startActivityForResult(intent, GET_P1_CHARACTER);
        } else if ( p2Button == v ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);
            startActivityForResult(intent, GET_P2_CHARACTER);
        } else if ( p1Win == v ) {
            recordWin( p1Name );
        } else if ( p2Win == v ) {
            recordWin( p2Name );
        } else {
            Toast t = Toast.makeText( v.getContext(), "Unknown button", Toast.LENGTH_SHORT );
            t.show();
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if ( requestCode == GET_P1_CHARACTER ) {
            Button btn = (Button) findViewById( R.id.btnChoiceP1 );
            btn.setText( data.getStringExtra( CharacterSelectActivity.GET_CHARACTER_PROPERTY ));
        } else if ( requestCode == GET_P2_CHARACTER ) {
            Button btn = (Button) findViewById(R.id.btnChoiceP2);
            btn.setText(data.getStringExtra(CharacterSelectActivity.GET_CHARACTER_PROPERTY));
        } else {
            Toast t = Toast.makeText( this.getApplicationContext(),
                    "Unrecognised Activity result", Toast.LENGTH_SHORT );
            t.show();
        }
    }

    private class RecordBattleTask extends AsyncTask<Void,Void,Void> {
        private StorageService storageService;
        private Context context;
        private Exception mLastError;
        private String mDate;
        private String mPlayer1;
        private String mPlayer2;
        private String winner;

        public RecordBattleTask(StorageService storageService,
                                Context context,
                                String date,
                                String p1,
                                String p2,
                                String winner ) {
            this.storageService = storageService;
            this.context = context;
            this.mDate = date;
            this.mPlayer1 = p1;
            this.mPlayer2 = p2;
            this.winner = winner;
        }

        @Override
        protected Void doInBackground(Void... params ) {
            try {
                storageService.recordBattle( mDate, mPlayer1, mPlayer2, winner );
                System.out.println( "Battle recorded" );
            } catch ( IOException ioe ) {
                mLastError = ioe;
                cancel(true);
                //System.out.println("io exception " + ioe.getMessage());
                //ioe.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void ret) {
            Toast t = Toast.makeText( context, "spreadsheet updated", Toast.LENGTH_SHORT);
            t.show();
        }

        @Override
        protected void onCancelled() {
            System.out.println("cancelled " + mLastError.getClass().getName());

            if ( mLastError instanceof UserRecoverableAuthIOException) {
                startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        MainActivity.REQUEST_AUTHORIZATION);
            } else {
                System.out.println( "Error:" + mLastError.getMessage() );
            }
        }
    }

}
