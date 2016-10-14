package com.example.will.sfclippy;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.will.sfclippy.models.BattleCounter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
implements View.OnClickListener, TextToSpeech.OnInitListener {
    private DrawerLayout mainView;
    private String accountId;
    private String player1Id;
    private String player2Id;
    private String player1Name;
    private String player2Name;
    private static final String UNKNOWN_CHOICE = "unknown";
    private String p1Choice = UNKNOWN_CHOICE;
    private String p2Choice = UNKNOWN_CHOICE;
    private Button p1Button;
    private Button p2Button;
    private Button p1Win;
    private Button p2Win;
    private DatabaseHelper helper;
    private TextView lblFact1;
    private TextView lblFact2;
    private TextView lblFact3;
    private TextToSpeech textToSpeech;
    private DatabaseReference p1PrefRef;
    final private CharPrefWatcher p1PrefWatcher = new CharPrefWatcher();
    private DatabaseReference p2PrefRef;
    final private CharPrefWatcher p2PrefWatcher = new CharPrefWatcher();

    static public final int GET_P1_CHARACTER = 1;
    static public final int GET_P2_CHARACTER = 2;
    static public final int DO_BACKUP = 3;
    static public final int RECOGNISE_SPEECH = 4;

    public final static String ACCOUNT_ID_LABEL = "account_id";
    public final static String PLAYER1_ID_LABEL = "player1_id";
    public final static String PLAYER1_NAME_LABEL = "player1_name";
    public final static String PLAYER2_ID_LABEL = "player2_id";
    public final static String PLAYER2_NAME_LABEL = "player2_name";

    private final static String TAG = "MainActivity";

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
                 intent.putExtra( PlayerStatistics.PLAYER_NAME, player1Name );
                 startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parent).toBundle());
             } else if ( stats2 == v ) {
                 Intent intent = new Intent(parent, PlayerStatistics.class);
                 intent.putExtra( PlayerStatistics.ACCOUNT_ID, accountId );
                 intent.putExtra( PlayerStatistics.PLAYER_ID, p2Id );
                 intent.putExtra( PlayerStatistics.PLAYER_NAME, player2Name );
                 startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parent).toBundle());
             }
        }
    };

    private static class OverallStatsWatcher implements ValueEventListener {
        private final TextView lbl;
        private final String p1Name;
        private final String p2Name;
        private static final String TAG = "OverallStatsWatcher";
        private static final String DEFAULT = "No previous results";

        public OverallStatsWatcher( TextView lbl, String p1Name, String p2Name ) {
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
                            p1Name,
                            (counter.wins * 100) / counter.battles,
                            counter.wins, (counter.battles - counter.wins) );
                    lbl.setText( msg );
                }
            }
        }

        @Override
        public void onCancelled( DatabaseError err ) {
            Log.e( TAG, "Stats watcher cancelled", err.toException() );
        }
    }

    private static class CharVsCharWatcher implements ValueEventListener {
        private final TextView lbl;
        private final String p1Name;
        private final String p2Name;
        private static final String TAG = "OverallStatsWatcher";
        private static final String DEFAULT = "No previous pairing";


        public CharVsCharWatcher( TextView lbl,
                                  String p1Name,
                                  String p2Name ) {
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
                                p1Name, (100 * wins) / total, wins, losses );
                    } else if ( losses > wins ) {
                        msg = String.format( Locale.UK,
                                "%s wins %d%% encounters (%d vs %d)",
                                p2Name, (100 * losses) / total, losses, wins );
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
            Log.e( TAG, "Cancelled charvschar", err.toException() );
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
        outState.putString( PLAYER1_NAME_LABEL, player1Name );
        outState.putString( PLAYER2_ID_LABEL, player2Id );
        outState.putString( PLAYER2_NAME_LABEL, player2Name );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get id associated with the battle
        // first from intent... then from saved instance state
        Intent intent = getIntent();
        accountId = intent.getStringExtra( ACCOUNT_ID_LABEL );
        player1Id = intent.getStringExtra( PLAYER1_ID_LABEL );
        player1Name = intent.getStringExtra( PLAYER1_NAME_LABEL );
        player2Id = intent.getStringExtra( PLAYER2_ID_LABEL );
        player2Name = intent.getStringExtra( PLAYER2_NAME_LABEL );
        if ( null == player1Id || null == player2Id ) {
            if ( null != savedInstanceState ) {
                Log.d( TAG, "Restoring from saved state");
                player1Id = savedInstanceState.getString( PLAYER1_ID_LABEL );
                player1Name = savedInstanceState.getString( PLAYER1_NAME_LABEL );
                player2Id = savedInstanceState.getString( PLAYER2_ID_LABEL );
                player2Name = savedInstanceState.getString( PLAYER2_NAME_LABEL );
            }
        }

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
        p1Text.setText( player1Name + " choice:");
        TextView p2Text = (TextView) findViewById(R.id.textP2);
        p2Text.setText( player2Name + " choice:");
        p1Win = (Button) findViewById(R.id.btnWinP1);
        p1Win.setText( player1Name + " win");
        p1Win.setOnClickListener( this );
        p2Win = (Button) findViewById(R.id.btnWinP2);
        p2Win.setText( player2Name + " win");
        p2Win.setOnClickListener( this );

        // get facts
        lblFact1 = (TextView) findViewById(R.id.lblFact1);
        lblFact2 = (TextView) findViewById(R.id.lblFact2);
        lblFact3 = (TextView) findViewById(R.id.lblFact3);

        helper = new DatabaseHelper( FirebaseDatabase.getInstance(), accountId);

        p1PrefRef = helper.getPlayerPrefsRef( player1Id );
        p1PrefRef.addValueEventListener( p1PrefWatcher );
        p2PrefRef = helper.getPlayerPrefsRef( player2Id );
        p2PrefRef.addValueEventListener( p2PrefWatcher );

        overallStatsRef = helper.getPlayerVsPlayerRef( player1Id, player2Id );
        overallStatsWatcher = new OverallStatsWatcher( lblFact1, player1Name, player2Name );
        overallStatsRef.addValueEventListener( overallStatsWatcher );
        charVsCharWatcher = new CharVsCharWatcher( lblFact2, player1Name, player2Name );

        p1Button = (Button) findViewById(R.id.btnChoiceP1);
        p1Button.setOnClickListener( this );

        p2Button = (Button) findViewById(R.id.btnChoiceP2);
        p2Button.setOnClickListener( this );

        textToSpeech = new TextToSpeech( this, this );

        checkButtons();

        setupDrawer();
    }

    private void announceCharacters( ) {
        // TODO check enabled
        textToSpeech.speak( "Next battle is " + p1Choice + " versus " + p2Choice,
                TextToSpeech.QUEUE_ADD,
                null,
                "utterance_id");
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.action_lucky:

                String choice1 = p1PrefWatcher.getRandomCharacter();
                if ( ! choice1.equals(UNKNOWN_CHOICE) ) {
                    setP1Choice(choice1);
                }

                String choice2 = p2PrefWatcher.getRandomCharacter();
                if ( ! choice2.equals(UNKNOWN_CHOICE) ) {
                    setP2Choice(choice2);
                }

                announceCharacters();

                return true;
            case R.id.action_listen:
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                // Start the activity, the intent will be populated with the speech text
                startActivityForResult(intent, RECOGNISE_SPEECH);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
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
            intent.putExtra( CharacterSelectActivity.TITLE, player1Name + " choice");

            startActivityForResult(intent, GET_P1_CHARACTER,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle() );
        } else if ( p2Button == v ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);

            intent.putExtra( CharacterSelectActivity.ACCOUNT_ID, accountId );
            intent.putExtra( CharacterSelectActivity.PLAYER_ID, player2Id );
            intent.putExtra( CharacterSelectActivity.TITLE, player2Name + " choice");

            startActivityForResult(intent, GET_P2_CHARACTER,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle() );
        } else if ( p1Win == v ) {
            recordWin(player1Id, player1Name );
        } else if ( p2Win == v ) {
            recordWin(player2Id, player2Name );
        } else {
            Toast t = Toast.makeText( v.getContext(), "Unknown button", Toast.LENGTH_SHORT );
            t.show();
        }
    }

    private void setP1Choice( String choice ) {
        p1Choice = choice;
        p1Button.setText( p1Choice );

        checkButtons();
    }

    private void setP2Choice( String choice ) {
        p2Choice = choice;
        p2Button.setText( p2Choice );

        checkButtons();
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if ( requestCode == GET_P1_CHARACTER ) {
            if ( null != data ) {
                String choice =  data.getStringExtra( CharacterSelectActivity.GET_CHARACTER_PROPERTY );
                setP1Choice( choice );
            }
        } else if ( requestCode == GET_P2_CHARACTER ) {
            if (null != data) {
                String choice = data.getStringExtra(CharacterSelectActivity.GET_CHARACTER_PROPERTY);
                setP2Choice(choice);
            }
        } else if ( requestCode == DO_BACKUP ) {
            Toast toast = Toast.makeText(this, "Backup complete", Toast.LENGTH_SHORT);
            toast.show();
        } else if ( RESULT_OK == resultCode && requestCode == RECOGNISE_SPEECH) {
            List<String> results = data.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
            if ( results.size() > 0 ) {
                String best = results.get(0);
                Log.d( TAG, "best match is \"" + best + "\"");
                String[] tokens = best.split( " " );
                Log.d( TAG, "token size is " + tokens.length);
                if ( tokens.length == 3 &&
                        ( tokens[1].equals("vs") || tokens[1].equals("versus") ) ) {
                    String u1 = tokens[0];
                    String u2 = tokens[2];

                    String p1 = p1PrefWatcher.matchCharacter( u1 );
                    String p2 = p2PrefWatcher.matchCharacter( u2 );

                    if ( ! p1.equals(UNKNOWN_CHOICE) && ! p2.equals(UNKNOWN_CHOICE) ) {
                        setP1Choice( p1 );
                        setP2Choice( p2 );

                        announceCharacters();
                    } else {
                        Toast.makeText(this,
                                "Problem matching \"" + u1 + "\" and \"" + u2 + "\"",
                                Toast.LENGTH_SHORT ).show();
                    }
                } else {
                    // TODO feedback to user
                    Toast.makeText(this, best, Toast.LENGTH_SHORT ).show();
                }
            }
        } else {
            Toast t = Toast.makeText( this.getApplicationContext(),
                    "Unrecognised Activity result", Toast.LENGTH_SHORT );
            t.show();
        }
    }

    @Override
    public void onInit( int status ) {
        if ( status == TextToSpeech.SUCCESS ) {
            // btnSpeak.setEnabled(true);
        } else {
            Toast.makeText(this, "Failed to initialise speech", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onDestroy( ) {
        p1PrefRef.removeEventListener(p1PrefWatcher);
        p2PrefRef.removeEventListener(p2PrefWatcher);
        overallStatsRef.removeEventListener( overallStatsWatcher );
        if ( null != charVsCharRef ) {
            charVsCharRef.removeEventListener(charVsCharWatcher);
        }

        textToSpeech.shutdown();

        super.onDestroy();
    }
}
