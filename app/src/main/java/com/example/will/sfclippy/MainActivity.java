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
import java.util.Map;

public class MainActivity extends AppCompatActivity
implements View.OnClickListener, FactsUpdateListener {
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
    private Button regen;
    private FactsListener factsListener;
    private StringRefWatcher p1Watcher = new StringRefWatcher();
    private StringRefWatcher p2Watcher = new StringRefWatcher();
    private HistoricalTrends trends = new HistoricalTrends();
    private static final String UNKNOWN_CHOICE = "unknown";
    private DatabaseHelper helper;

    static public final int GET_P1_CHARACTER = 1;
    static public final int GET_P2_CHARACTER = 2;
    static public final int DO_BACKUP = 3;

    public final static String ACCOUNT_ID_LABEL = "account_id";
    public final static String PLAYER1_ID_LABEL = "player1_id";
    public final static String PLAYER2_ID_LABEL = "player2_id";

    private final static String TAG = "MainActivity";

    private DatabaseReference p1NameRef;
    private DatabaseReference p2NameRef;

    private class MenuListener implements View.OnClickListener {
        private final Activity parent;
        private final Button results;
        private final Button backup;
        private final Button regenerateStats;
        private final String p1Id;
        private final String p2Id;

        public MenuListener( Activity parent,
                             Button results,
                             Button backup,
                             Button regenerateStats,
                             String p1Id,
                             String p2Id ) {
            this.parent = parent;
            this.results = results;
            this.backup = backup;
            this.regenerateStats = regenerateStats;
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
                 helper.regenerateStatistics( new DatabaseHelper.StatisticsCompleteListener() {
                     @Override
                     public void statisticsComplete( ) {
                         Toast.makeText(parent, "Statistics regenerated", Toast.LENGTH_LONG).show();
                     }
                 });
             }
        }
    };

    private static class FactsListener implements View.OnClickListener {
        private int currentIndex = 0;
        private TextSwitcher switcher;
        private List<HistoricalTrends.Fact> facts;
        private ImageButton previous;
        private ImageButton next;

        public FactsListener(List<HistoricalTrends.Fact> facts,
                             TextSwitcher switcher,
                             ImageButton previous,
                             ImageButton next ) {
            this.currentIndex = 0;
            this.switcher = switcher;
            this.facts = facts;
            this.previous = previous;
            this.next = next;

            updateButtons();
            updateTextSwitcher();
        }

        private void updateButtons( ) {
            if ( 0 == currentIndex ) {
                previous.setEnabled(false);
                previous.setClickable(false);
            } else {
                previous.setEnabled(true);
                previous.setClickable(true);
            }

            if ( currentIndex + 1 < facts.size() ) {
                next.setEnabled(true);
                next.setClickable(true);
            } else {
                next.setEnabled(false);
                next.setClickable(false);
            }
        }

        private void updateTextSwitcher( ) {
            if ( currentIndex < facts.size() ) {
                switcher.setText(facts.get(currentIndex).getInfo());
            } else {
                switcher.setText("No facts");
            }
        }

        private void replaceFacts( List<HistoricalTrends.Fact> facts ) {
            this.facts = facts;
            this.currentIndex = 0;
            updateButtons();
            updateTextSwitcher();
        }

        @Override
        public void onClick( View v ) {
            if ( v == previous ) {
                currentIndex--;
                updateButtons();
                updateTextSwitcher();
            } else if ( v == next ) {
                currentIndex++;
                updateButtons();
                updateTextSwitcher();
            }
        }
    }

    private void setupDrawer( ) {
        Button btnBackup = (Button) findViewById(R.id.btnBackup);
        Button btnResults = (Button) findViewById(R.id.btnResults);
        Button btnRegenerateStatistics = (Button) findViewById(R.id.btnRegenerateStats);

        MenuListener listener = new MenuListener( this,
                btnResults,
                btnBackup, btnRegenerateStatistics, player1Id, player2Id );
        btnBackup.setOnClickListener( listener );
        btnResults.setOnClickListener( listener );
        btnRegenerateStatistics.setOnClickListener( listener );
    }

    private List<HistoricalTrends.Fact> getBattleFacts( ) {
        return new ArrayList<HistoricalTrends.Fact>();
        /*
        List<HistoricalTrends.Fact> facts = trends.getBattleFacts(
                p1Watcher.getPlayerInfo(),
                p1Choice,
                p2Watcher.getPlayerInfo(),
                p2Choice,
                Calendar.getInstance().getTime() );

        // sort facts to more interesting facts appear first
        Collections.sort(facts, new Comparator<HistoricalTrends.Fact>() {
            @Override
            public int compare(HistoricalTrends.Fact lhs, HistoricalTrends.Fact rhs) {
                return rhs.getScore() - lhs.getScore();
            }
        });

        return facts;]*/
    }

    private void setupTextSwitcher( ) {
        View factsWidget = findViewById(R.id.viewFactWidget);
        ImageButton next = (ImageButton) factsWidget.findViewById(R.id.btnFactNext);
        ImageButton previous = (ImageButton) factsWidget.findViewById(R.id.btnFactPrevious);
        TextSwitcher switcher = (TextSwitcher) factsWidget.findViewById(R.id.textSwitcher);

        switcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(MainActivity.this);
                //myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                //myText.setTextSize(36);
                //myText.setTextColor(Color.BLUE);
                return myText;
            }
        });

        List<HistoricalTrends.Fact> facts = new ArrayList<>();
                //getBattleFacts();

        factsListener = new FactsListener( facts, switcher, previous, next );
        next.setOnClickListener(factsListener);
        previous.setOnClickListener(factsListener);
    }

    private void checkButtons( ) {
        boolean enabled = true;
        if ( 0 == p1Choice.compareTo(UNKNOWN_CHOICE)
                || 0 == p2Choice.compareTo(UNKNOWN_CHOICE) ) {
            enabled = false;
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

        setupTextSwitcher();

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
        } else if ( regen == v ) {
            regenerateFacts();
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

                List<HistoricalTrends.Fact> facts = getBattleFacts();
                factsListener.replaceFacts(facts);
                checkButtons();
            }
        } else if ( requestCode == GET_P2_CHARACTER ) {
            if (null != data) {
                p2Choice = data.getStringExtra(CharacterSelectActivity.GET_CHARACTER_PROPERTY);
                p2Button.setText(p2Choice);

                List<HistoricalTrends.Fact> facts = getBattleFacts();
                factsListener.replaceFacts(facts);
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
        super.onDestroy();
    }

    @Override
    public void factsUpdated( ) {
        Log.d( TAG, "Facts updated" );
        List<HistoricalTrends.Fact> facts = getBattleFacts();
        factsListener.replaceFacts(facts);
    }

    public void regenerateFacts( ) {
        //regeneratePlayerStatistics( p1Watcher.playerInfo );
        // regeneratePlayerStatistics( p2Watcher.playerInfo );
    }
}
