package com.example.will.sfclippy;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.will.sfclippy.models.BattleCounter;
import com.example.will.sfclippy.models.BattleResult;
import com.example.will.sfclippy.models.PlayerInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
implements View.OnClickListener {
    private DrawerLayout mainView;
    private String accountId;
    private String player1Id;
    private String player2Id;
    private String p1Choice = "unknown";
    private String p2Choice = "unknown";
    private Button p1Button;
    private Button p2Button;
    private Button p1Win;
    private Button p2Win;
    private StringRefWatcher p1Watcher = new StringRefWatcher();
    private StringRefWatcher p2Watcher = new StringRefWatcher();
    private static final String UNKNOWN_CHOICE = "unknown";
    private DatabaseHelper helper;
    private TextView lblFact1;
    private TextView lblFact2;
    private TextView lblFact3;

    static public final int GET_P1_CHARACTER = 1;
    static public final int GET_P2_CHARACTER = 2;
    static public final int DO_BACKUP = 3;

    public final static String ACCOUNT_ID_LABEL = "account_id";
    public final static String PLAYER1_ID_LABEL = "player1_id";
    public final static String PLAYER2_ID_LABEL = "player2_id";

    private final static String TAG = "MainActivity";

    private DatabaseReference p1NameRef;
    private DatabaseReference p2NameRef;
    private OverallStatsWatcher overallStatsWatcher;
    private DatabaseReference overallStatsRef;
    private CharVsCharWatcher charVsCharWatcher;
    private DatabaseReference charVsCharRef;

    private class MenuListener implements View.OnClickListener {
        private final Activity parent;
        private final Button results;
        private final Button backup;
        private final Button regenerateStats;
        private final Button stats1;
        private final Button stats2;
        private final String p1Id;
        private final String p2Id;

        public MenuListener( Activity parent,
                             Button results,
                             Button backup,
                             Button regenerateStats,
                             Button stats1,
                             Button stats2,
                             String p1Id,
                             String p2Id ) {
            this.parent = parent;
            this.results = results;
            this.backup = backup;
            this.regenerateStats = regenerateStats;
            this.stats1 = stats1;
            this.stats2 = stats2;
            this.p1Id = p1Id;
            this.p2Id = p2Id;
        }

        @Override
        public void onClick( View v ) {
             if ( results == v ) {
                Intent intent = new Intent(parent, ResultsActivity.class);
                 intent.putExtra( ResultsActivity.ACCOUNT_ID, accountId );
                intent.putExtra( ResultsActivity.P1_ID, p1Id );
                intent.putExtra( ResultsActivity.P2_ID, p2Id );
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parent).toBundle());
            } else if ( backup == v ) {
                Toast.makeText(parent, "Backup not implemented", Toast.LENGTH_SHORT).show();
            } else if ( regenerateStats == v ) {
                 helper.regenerateStatistics(new DatabaseHelper.StatisticsCompleteListener() {
                     @Override
                     public void statisticsComplete() {
                         Toast.makeText(parent, "Statistics regenerated", Toast.LENGTH_LONG).show();
                     }
                 });
             } else if ( stats1 == v ) {
                 Intent intent = new Intent(parent, PlayerStatistics.class);
                 intent.putExtra( PlayerStatistics.ACCOUNT_ID, accountId );
                 intent.putExtra( PlayerStatistics.PLAYER_ID, p1Id );
                 intent.putExtra( PlayerStatistics.PLAYER_NAME, p1Watcher.getValue() );
                 startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parent).toBundle());
             } else if ( stats2 == v ) {
                 Intent intent = new Intent(parent, PlayerStatistics.class);
                 intent.putExtra( PlayerStatistics.ACCOUNT_ID, accountId );
                 intent.putExtra( PlayerStatistics.PLAYER_ID, p2Id );
                 intent.putExtra( PlayerStatistics.PLAYER_NAME, p2Watcher.getValue() );
                 startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parent).toBundle());
             }
        }
    };

    private static class OverallStatsWatcher implements ValueEventListener {
        private final TextView lbl;
        private final StringRefWatcher p1Name;
        private final StringRefWatcher p2Name;
        private static final String TAG = "OverallStatsWatcher";
        private static final String DEFAULT = "No previous results";

        public OverallStatsWatcher( TextView lbl, StringRefWatcher p1Name, StringRefWatcher p2Name ) {
            this.lbl = lbl;
            this.p1Name = p1Name;
            this.p2Name = p2Name;
            lbl.setText( DEFAULT );
        }

        @Override
        public void onDataChange( DataSnapshot snapshot ) {
            lbl.setText( DEFAULT );
            if ( null != snapshot ) {
                BattleCounter counter = snapshot.getValue(BattleCounter.class);
                if ( null != counter ) {
                    String msg = String.format(Locale.UK,
                            "%s wins %d%% of battles (%d vs %d)",
                            p1Name.getValue(),
                            (counter.wins * 100) / counter.battles,
                            counter.wins, (counter.battles - counter.wins) );
                    lbl.setText( msg );
                }
            }
        }

        @Override
        public void onCancelled( DatabaseError err ) {
            Log.e( TAG, "Cancelled", err.toException() );
        }
    }

    private static class CharVsCharWatcher implements ValueEventListener {
        private final TextView lbl;
        private final StringRefWatcher p1Name;
        private final StringRefWatcher p2Name;
        private static final String TAG = "OverallStatsWatcher";
        private static final String DEFAULT = "No previous pairing";


        public CharVsCharWatcher( TextView lbl,
                                  StringRefWatcher p1Name,
                                  StringRefWatcher p2Name ) {
            this.lbl = lbl;
            this.p1Name = p1Name;
            this.p2Name = p2Name;
            lbl.setText( DEFAULT );
        }

        @Override
        public void onDataChange( DataSnapshot snapshot ) {
            lbl.setText(DEFAULT);
            if ( null != snapshot ) {
                BattleCounter counter = snapshot.getValue(BattleCounter.class);
                if ( null != counter ) {
                    int wins = counter.wins;
                    int losses = counter.battles - counter.wins;
                    int total = counter.battles;

                    String msg = "";
                    if ( wins > losses ) {
                        msg = String.format(Locale.UK,
                                "%s wins %d%% encounters (%d vs %d)",
                                p1Name.getValue(), (100 * wins) / total, wins, losses );
                    } else if ( losses > wins ) {
                        msg = String.format( Locale.UK,
                                "%s wins %d%% encounters (%d vs %d)",
                                p2Name.getValue(), (100 * losses) / total, losses, wins );
                    } else {
                        msg = String.format( Locale.UK,
                                "Even encounter history (%d vs %d)",
                                wins, losses );
                    }

                    lbl.setText( msg );
                }
            }
        }

        @Override
        public void onCancelled( DatabaseError err ) {
            Log.e( TAG, "Cancelled", err.toException() );
        }
    }

    private void setupDrawer( ) {
        Button btnBackup = (Button) findViewById(R.id.btnBackup);
        Button btnResults = (Button) findViewById(R.id.btnResults);
        Button btnRegenerateStatistics = (Button) findViewById(R.id.btnRegenerateStats);
        Button btnStats1 = (Button) findViewById(R.id.btnStatsP1);
        Button btnStats2 = (Button) findViewById(R.id.btnStatsP2);


        MenuListener listener = new MenuListener( this,
                btnResults,
                btnBackup, btnRegenerateStatistics, btnStats1, btnStats2, player1Id, player2Id );
        btnBackup.setOnClickListener( listener );
        btnResults.setOnClickListener( listener );
        btnRegenerateStatistics.setOnClickListener( listener );
        btnStats1.setOnClickListener( listener );
        btnStats2.setOnClickListener( listener );
    }

    private void checkButtons( ) {
        boolean enabled = true;
        if ( 0 == p1Choice.compareTo(UNKNOWN_CHOICE)
                || 0 == p2Choice.compareTo(UNKNOWN_CHOICE) ) {
            enabled = false;
        } else {
            if ( null != charVsCharRef ) {
                charVsCharRef.removeEventListener(charVsCharWatcher);
            }
            charVsCharRef = helper.getPvpCvcRef( player1Id, player2Id,
                    p1Choice, p2Choice );
            charVsCharRef.addValueEventListener(charVsCharWatcher);
        }

        p1Win.setEnabled(enabled);
        p2Win.setEnabled(enabled);
    }

    @Override
    protected void onSaveInstanceState( Bundle outState  ) {
        Log.d( TAG, "Saving state" );
        super.onSaveInstanceState( outState );
        outState.putString( ACCOUNT_ID_LABEL, accountId );
        outState.putString( PLAYER1_ID_LABEL, player1Id );
        outState.putString( PLAYER2_ID_LABEL, player2Id );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set-up action bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle( getString( R.string.app_name) );

        mainView = (DrawerLayout) findViewById( R.id.mainDrawerLayout );

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, mainView,
                myToolbar, R.string.drawer_open, R.string.drawer_close );
        mainView.addDrawerListener(toggle);
        toggle.syncState();

        // set-up buttons
        TextView p1Text = (TextView) findViewById(R.id.textP1);
        TextView p2Text = (TextView) findViewById(R.id.textP2);
        p1Win = (Button) findViewById(R.id.btnWinP1);
        p1Win.setOnClickListener( this );
        p2Win = (Button) findViewById(R.id.btnWinP2);
        p2Win.setOnClickListener( this );

        // set up views that show player name
        p1Watcher.registerTextView( p1Text, "%s choice:");
        p1Watcher.registerTextView( p1Win, "%s win" );
        p2Watcher.registerTextView( p2Text, "%s choice:");
        p2Watcher.registerTextView( p2Win, "%s win" );

        // get facts
        lblFact1 = (TextView) findViewById(R.id.lblFact1);
        lblFact2 = (TextView) findViewById(R.id.lblFact2);
        lblFact3 = (TextView) findViewById(R.id.lblFact3);

        // get id associated with the battle
        // first from intent... then from saved instance state
        Intent intent = getIntent();
        accountId = intent.getStringExtra( ACCOUNT_ID_LABEL );
        player1Id = intent.getStringExtra( PLAYER1_ID_LABEL );
        player2Id = intent.getStringExtra( PLAYER2_ID_LABEL );
        if ( null == player1Id || null == player2Id ) {
            if ( null != savedInstanceState ) {
                Log.d( TAG, "Restoring from saved state");
                player1Id = savedInstanceState.getString( PLAYER1_ID_LABEL );
                player2Id = savedInstanceState.getString( PLAYER2_ID_LABEL );
            }
        }

        helper = new DatabaseHelper( FirebaseDatabase.getInstance(), accountId);
        p1NameRef = helper.getPlayerNameRef( player1Id );
        p1NameRef.addValueEventListener( p1Watcher );
        p2NameRef = helper.getPlayerNameRef( player2Id );
        p2NameRef.addValueEventListener( p2Watcher );

        overallStatsRef = helper.getPlayerVsPlayerRef( player1Id, player2Id );
        overallStatsWatcher = new OverallStatsWatcher( lblFact1, p1Watcher, p2Watcher );
        overallStatsRef.addValueEventListener( overallStatsWatcher );
        charVsCharWatcher = new CharVsCharWatcher( lblFact2, p1Watcher, p2Watcher );

        p1Button = (Button) findViewById(R.id.btnChoiceP1);
        p1Button.setOnClickListener( this );

        p2Button = (Button) findViewById(R.id.btnChoiceP2);
        p2Button.setOnClickListener( this );

        checkButtons();

        setupDrawer();
    }

    private void recordWin(String winnerId, String winnerName ) {
        // TODO loading screen
        Log.d( getLocalClassName(), "Recording win for " + winnerId );

        final Snackbar notify = Snackbar.make( mainView,
                "Recorded win for " + winnerName,
                Snackbar.LENGTH_LONG );

        // set loading
        p1Win.setEnabled(false);
        p2Win.setEnabled(false);

        helper.storeResult( player1Id, p1Choice, player2Id, p2Choice, winnerId,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        p1Win.setEnabled(true);
                        p2Win.setEnabled(true);
                        notify.show();
                    }
        });
    }

    @Override
    public void onClick( View v ) {
        if ( p1Button == v ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);

            intent.putExtra( CharacterSelectActivity.ACCOUNT_ID, accountId );
            intent.putExtra( CharacterSelectActivity.PLAYER_ID, player1Id );
            intent.putExtra( CharacterSelectActivity.TITLE, "Choose player 1");

            startActivityForResult(intent, GET_P1_CHARACTER,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle() );
        } else if ( p2Button == v ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);

            intent.putExtra( CharacterSelectActivity.ACCOUNT_ID, accountId );
            intent.putExtra( CharacterSelectActivity.PLAYER_ID, player2Id );
            intent.putExtra( CharacterSelectActivity.TITLE, "Choose player 2");

            startActivityForResult(intent, GET_P2_CHARACTER,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle() );
        } else if ( p1Win == v ) {
            recordWin(player1Id, p1Watcher.getValue());
        } else if ( p2Win == v ) {
            recordWin(player2Id, p2Watcher.getValue());
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

                checkButtons();
            }
        } else if ( requestCode == GET_P2_CHARACTER ) {
            if (null != data) {
                p2Choice = data.getStringExtra(CharacterSelectActivity.GET_CHARACTER_PROPERTY);
                p2Button.setText(p2Choice);

                checkButtons();
            }
        } else if ( requestCode == DO_BACKUP ) {
            Toast toast = Toast.makeText( this, "Backup complete", Toast.LENGTH_SHORT );
            toast.show();
        } else {
            Toast t = Toast.makeText( this.getApplicationContext(),
                    "Unrecognised Activity result", Toast.LENGTH_SHORT );
            t.show();
        }
    }

    @Override
    public void onDestroy( ) {
        p1NameRef.removeEventListener(p1Watcher);
        p2NameRef.removeEventListener(p2Watcher);
        overallStatsRef.removeEventListener( overallStatsWatcher );
        if ( null != charVsCharRef ) {
            charVsCharRef.removeEventListener(charVsCharWatcher);
        }

        super.onDestroy();
    }
}
