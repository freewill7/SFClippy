package com.example.will.sfclippy;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ActivityOptions;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.credentials.Credential;
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

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class AccountActivity extends Activity
        implements EasyPermissions.PermissionCallbacks {
    private GoogleAccountCredential mCredential;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    static private final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize credentials and service object
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        getResultsFromApi();
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                AccountActivity.this,
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

    private void setupStorageService( ) {
        // Create connection
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
            AppSingleton.getInstance().setStorageService( ss );
        } catch ( Exception e ) {
            System.out.println("Failed to setup spreadsheet: " + e.getMessage());
        }
    }

    private class FetchCharacterPreferences extends AsyncTask<Void,Void,CharacterStatistics> {
        private StorageService storageService;
        private Activity activity;
        private Exception mLastError;

        public FetchCharacterPreferences(StorageService storageService,
                                         Activity activity ) {
            this.storageService = storageService;
            this.activity = activity;
        }

        @Override
        protected CharacterStatistics doInBackground(Void... params ) {
            CharacterStatistics statistics = null;
            try {
                statistics = storageService.getStatistics( );
                System.out.println( "Statistics retrieved" );
            } catch ( IOException ioe ) {
                mLastError = ioe;
                cancel(true);
                //System.out.println("io exception " + ioe.getMessage());
                //ioe.printStackTrace();
            }
            return statistics;
        }

        @Override
        protected void onPostExecute(CharacterStatistics ret) {
            AppSingleton.getInstance().setCharacterStatistics(ret);
            Intent intent = new Intent( activity.getApplicationContext(), MainActivity.class );
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle());
            finish();
            // Toast t = Toast.makeText( context, "values fetched", Toast.LENGTH_SHORT);
            // t.show();
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

    private void getCharacterStatistics( StorageService service, Context context ) {
        FetchCharacterPreferences fcp = new FetchCharacterPreferences( service, this );
        fcp.execute();
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

            setupStorageService();

            getCharacterStatistics( AppSingleton.getInstance().getStorageService(),
                    getApplicationContext() );


        }
    }


    @Override
    public void onPermissionsGranted( int requestCode, List<String> list ) {

    }

    @Override
    public void onPermissionsDenied( int requestCode, List<String> list ) {

    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch ( requestCode ) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast t = Toast.makeText( this, "No Google Play Services", Toast.LENGTH_SHORT );
                    t.show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
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
                        //textAccount.setText(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }
}
