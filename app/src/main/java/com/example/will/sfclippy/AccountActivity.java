package com.example.will.sfclippy;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity
implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 1000;
    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "AccountActivity";
    private boolean bootstrapComplete = false;

    private TextView progressLabel;

    private void launchMainActivity( String p1Id, String p2Id ) {
        Log.d( TAG, "Launch Main activity " + p1Id + " " + p2Id );

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra( MainActivity.PLAYER1_ID_LABEL, p1Id );
        intent.putExtra( MainActivity.PLAYER2_ID_LABEL, p2Id );

        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void setupDataProvider( String userHome ) {
        Log.d( TAG, "Setting up data provider" );
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference home = database.getReference( userHome );
        final DatabaseReference users = home.child( "players" );
        DatabaseReference preferences = home.child( "preferences" );
        DatabaseReference results = home.child( "results" );

        DataProvider provider = new DataProvider( users, preferences, results );
        AppSingleton.getInstance().setDataProvider(provider);

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.removeEventListener(this);
                if ( null == dataSnapshot.getValue() ) {
                    Log.d( TAG, "Bootstrapping users");
                    DatabaseReference p1 = users.push();
                    FirebaseHelper.initialiseUser( p1, "Red Panda");

                    DatabaseReference p2 = users.push();
                    FirebaseHelper.initialiseUser( p2, "Blue Goose");

                    launchMainActivity( p1.getKey(), p2.getKey() );
                } else {
                    Log.d( TAG, "Fetching users");
                    List<String> playerIds = new ArrayList<>();
                    for ( DataSnapshot child : dataSnapshot.getChildren() ) {
                        playerIds.add( child.getKey() );
                    }

                    launchMainActivity( playerIds.get(0), playerIds.get(1));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w( TAG, "Cancelled", databaseError.toException() );
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        progressLabel = (TextView) findViewById(R.id.progressLabel);

        String webClientId = getString(R.string.default_web_client_id);
        Log.d( TAG, "Default client web id is " + webClientId );

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken( webClientId ) // generated from google-services.json
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .build();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if ( user != null ) {
                    if ( ! bootstrapComplete ) {
                        Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                        bootstrapComplete = true;

                        String userHome = "/users/" + user.getUid();
                        setupDataProvider(userHome);
                    } else {
                        Log.d(TAG, "bootstrap already in progress");
                    }
                } else {
                    Log.d( TAG, "onAuthStateChanged: signed_out" );
                }
            }
        };
    }

    @Override
    public void onStart() {
        Log.d( TAG, "On start" );
        super.onStart();
        mGoogleApiClient.connect();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( mAuthListener != null ) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void signIn( ) {
        Intent signInForIntent = Auth.GoogleSignInApi.getSignInIntent( mGoogleApiClient );
        startActivityForResult(signInForIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnected( Bundle bundler ) {
        Log.d( TAG, "Connected" );
        progressLabel.setText( "Connected" );

        Toast toast = Toast.makeText( this, "Connected", Toast.LENGTH_SHORT);
        toast.show();

        signIn();
    }

    @Override
    public void onConnectionSuspended( int cause ) {
        Log.d( TAG, "Connection suspended" );
    }

    @Override
    public void onConnectionFailed( ConnectionResult connectionResult ) {
        Log.d( getLocalClassName(), "Connection failed" );

        if ( connectionResult.hasResolution() ) {
            Log.d( TAG, "Connection failed... trying to resolve" );
            try {
                connectionResult.startResolutionForResult( this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e ) {
                Log.e( TAG, "Failed to start resolution for result", e );
            }
        } else {
            Log.e( TAG, "Couldn't recover from connection failure");
            Log.e( TAG, "Reason: " + connectionResult.getErrorMessage() +
            connectionResult.getErrorCode() );
        }
    }

    private void firebaseAuthWithGoogle( GoogleSignInAccount acct ) {
        Log.d( TAG, "firebaseAuthWithGoogle: " + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential( acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails display message to user
                        // otherwise leave to auth state listener
                        if ( ! task.isSuccessful() ) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(AccountActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);

        switch ( requestCode ) {
            case RC_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if ( result.isSuccess() ) {
                    // Sign in successful
                    GoogleSignInAccount account = result.getSignInAccount();
                    firebaseAuthWithGoogle(account);
                } else {
                    Toast toast = Toast.makeText( this, "Failed to sign in", Toast.LENGTH_SHORT);
                    toast.show();
                    Log.e( getLocalClassName(), "Failed to sign in " +
                            result.getStatus().getStatusMessage());
                }
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if ( resultCode == RESULT_OK ) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }
}
