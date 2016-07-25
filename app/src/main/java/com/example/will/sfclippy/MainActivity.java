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
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity
implements View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private GoogleAccountCredential mCredential;
    private Button authButton;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button p1Button = (Button) findViewById(R.id.btnChoiceP1);
        p1Button.setOnClickListener( this );

        final Button p2Button = (Button) findViewById(R.id.btnChoiceP2);
        p2Button.setOnClickListener( this );

        final Button sheetsButton = (Button) findViewById(R.id.btnSheets);
        authButton = sheetsButton;
        sheetsButton.setOnClickListener( this );

        final Button updateButton = (Button) findViewById(R.id.btnAppendRow);
        updateButton.setOnClickListener( this );

        // Initialize credentials and service object
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void getResultsFromApi( ) {
        if ( ! isGooglePlayServicesAvailable() ) {
            System.out.println("get google play services");
            acquireGooglePlayServices();
        } else if ( mCredential.getSelectedAccountName() == null) {
            System.out.println("choose account");
            chooseAccount();
            System.out.println("account selection done");
        } else if ( ! isDeviceOnline() ) {
            System.out.println("device offline");
        } else {
            System.out.println("All good");
        }
    }

    public void appendRow( ) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        com.google.api.services.sheets.v4.Sheets service =
                new com.google.api.services.sheets.v4.Sheets.Builder(
                        transport, jsonFactory, mCredential )
                        .setApplicationName( "Android app" )
                        .build();

        try {
            StorageService ss = new StorageService( service,
                    "1Mxk7jNAP3twXMeP3LgHCGasUkFpVyxI8J63u3gokDcI");
            new RecordBattleTask(ss, this.getApplicationContext()).execute();
        } catch ( Exception e ) {
            System.out.println("Failed to record battle: " + e.getMessage());
        }
    }

    @Override
    public void onClick( View v ) {
        if ( R.id.btnChoiceP1 == v.getId() ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);
            startActivityForResult(intent, CharacterSelectActivity.GET_P1_CHARACTER);
        } else if ( R.id.btnChoiceP2 == v.getId() ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);
            startActivityForResult(intent, CharacterSelectActivity.GET_P2_CHARACTER);
        } else if ( R.id.btnSheets == v.getId() ) {
            getResultsFromApi();
        } else if ( R.id.btnAppendRow == v.getId() ) {
            appendRow();
        } else {
            Toast t = Toast.makeText( v.getContext(), "Unknown button", Toast.LENGTH_SHORT );
            t.show();
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if ( requestCode == CharacterSelectActivity.GET_P1_CHARACTER ) {
            Button btn = (Button) findViewById( R.id.btnChoiceP1 );
            btn.setText( data.getStringExtra( CharacterSelectActivity.GET_CHARACTER_PROPERTY ));
        } else if ( requestCode == CharacterSelectActivity.GET_P2_CHARACTER ) {
            Button btn = (Button) findViewById(R.id.btnChoiceP2);
            btn.setText(data.getStringExtra(CharacterSelectActivity.GET_CHARACTER_PROPERTY));
        } else if ( requestCode == REQUEST_GOOGLE_PLAY_SERVICES ) {
            if (resultCode != RESULT_OK) {
                authButton.setText("No Google Play Services");
            } else {
                getResultsFromApi();
            }
        } else if ( requestCode == REQUEST_ACCOUNT_PICKER ) {
            if (resultCode == RESULT_OK && data != null &&
                    data.getExtras() != null) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (accountName != null) {
                    SharedPreferences settings =
                            getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(PREF_ACCOUNT_NAME, accountName);
                    editor.apply();
                    mCredential.setSelectedAccountName(accountName);
                    authButton.setText(accountName);
                    getResultsFromApi();
                }
            }
        } else if ( requestCode == REQUEST_AUTHORIZATION ) {
            if (resultCode == RESULT_OK) {
                getResultsFromApi();
            }
        } else {
            Toast t = Toast.makeText( this.getApplicationContext(),
                    "Unrecognised Activity result", Toast.LENGTH_SHORT );
            t.show();
        }
    }

    @Override
    public void onPermissionsGranted( int requestCode, List<String> list ) {

    }

    @Override
    public void onPermissionsDenied( int requestCode, List<String> list ) {

    }

    private class RecordBattleTask extends AsyncTask<Void,Void,Void> {
        private StorageService storageService;
        private Context context;
        private Exception mLastError;

        public RecordBattleTask(StorageService storageService, Context context ) {
            this.storageService = storageService;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params ) {
            try {
                storageService.recordBattle( "2016-07-24", "Ryu", "Ken", "Ruaidhri");
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
