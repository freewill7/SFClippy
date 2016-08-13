package com.example.will.sfclippy;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

public class AccountActivity extends Activity
implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataProviderSetup.NotifyInterface {
    GoogleApiClient mGoogleApiClient;

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 1000;

    private TextView progressLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        progressLabel = (TextView) findViewById(R.id.progressLabel);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        Log.d( getLocalClassName(), "On start" );
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected( Bundle bundler ) {
        Log.d( this.getLocalClassName(), "Connected" );
        progressLabel.setText( "Connected" );

        DriveHelper helper = new DriveHelper(mGoogleApiClient);
        DataProviderSetup setup = new DataProviderSetup(helper, this);
        setup.execute();
    }

    @Override
    public void onConnectionSuspended( int cause ) {
        Log.d( this.getLocalClassName(), "Connection suspended" );
    }

    @Override
    public void onConnectionFailed( ConnectionResult connectionResult ) {
        Log.d( getLocalClassName(), "Connection failed" );

        if ( connectionResult.hasResolution() ) {
            Log.d( this.getLocalClassName(), "Connection failed... trying to resolve" );
            try {
                connectionResult.startResolutionForResult( this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e ) {
                Log.e( getLocalClassName(), "Failed to start resolution for result", e );
            }
        } else {
            Log.e( getLocalClassName(), "Couldn't recover from connection failure");
            Log.e( getLocalClassName(), "Reason: " + connectionResult.getErrorMessage() +
            connectionResult.getErrorCode() );
            /* GoogleApiAvailability.getInstance()
            .getErrorDialog( this, connectionResult.getErrorCode(), RESOLVE_CONNECTION_REQUEST_CODE ); */
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        System.out.println("on activity result");
        switch ( requestCode ) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if ( resultCode == RESULT_OK ) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onProgressUpdate( String progress ) {
        progressLabel.setText( progress );
    }

    @Override
    public void onError( Exception e ) {
        Log.e( getLocalClassName(), "Error from SetupStorageService");
    }

    @Override
    public void onComplete( DataProvider dataProvider ) {
        AppSingleton.getInstance().setDataProvider( dataProvider );

        Intent intent = new Intent( this, MainActivity.class );
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        finish();
    }

}
