package com.example.will.sfclippy;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity
implements View.OnClickListener {
    private DataProvider dataProvider;

    private String p1Choice;
    private String p2Choice;
    private TextView p1Text;
    private TextView p2Text;
    private Button p1Button;
    private Button p2Button;
    private Button p1Win;
    private Button p2Win;
    private DrawerLayout drawerLayout;
    private Snackbar p1Snackbar;
    private Snackbar p2Snackbar;
    private FactsListener factsListener;

    static public final int GET_P1_CHARACTER = 1;
    static public final int GET_P2_CHARACTER = 2;

    private class MenuListener implements View.OnClickListener {
        private final Activity parent;
        private final Button p1Preferences;
        private final Button p2Preferences;
        private final Button results;
        private final DataProvider dataProvider;

        public MenuListener( Activity parent,
                             Button p1Preferences,
                             Button p2Preferences,
                             Button results,
                             DataProvider dataProvider ) {
            this.parent = parent;
            this.p1Preferences = p1Preferences;
            this.p2Preferences = p2Preferences;
            this.results = results;
            this.dataProvider = dataProvider;
        }

        @Override
        public void onClick( View v ) {
            if ( p1Preferences == v ) {
                Intent intent = new Intent(parent, CharacterPreferenceActivity.class);
                intent.putExtra( CharacterPreferenceActivity.PLAYER_ID_PROPERTY,
                        dataProvider.getPlayer1Id());
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parent).toBundle());
            } else if ( p2Preferences == v ) {
                Intent intent = new Intent(parent, CharacterPreferenceActivity.class);
                intent.putExtra( CharacterPreferenceActivity.PLAYER_ID_PROPERTY,
                        dataProvider.getPlayer2Id());
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parent).toBundle());
            } else if ( results == v ) {
                Intent intent = new Intent(parent, ResultsActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parent).toBundle());
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

    private String labelPreferences( String playerName ) {
        return playerName + " prefs";
    }

    private void setupDrawer( ) {
        Button btnP1Preferences = (Button) findViewById(R.id.btnPlayer1Prefs);
        btnP1Preferences.setText( labelPreferences(dataProvider.getPlayer1Name()) );
        Button btnP2Preferences = (Button) findViewById(R.id.btnPlayer2Prefs);
        btnP2Preferences.setText( labelPreferences(dataProvider.getPlayer2Name()) );
        Button btnResults = (Button) findViewById(R.id.btnResults);

        MenuListener listener = new MenuListener( this,
                btnP1Preferences, btnP2Preferences, btnResults,
                dataProvider );
        btnP1Preferences.setOnClickListener( listener );
        btnP2Preferences.setOnClickListener( listener );
        btnResults.setOnClickListener( listener );
    }

    private List<HistoricalTrends.Fact> getBattleFacts( ) {
        List<HistoricalTrends.Fact> facts = dataProvider.getHistoricalTrends().getBattleFacts(
                dataProvider.getPlayerById( dataProvider.getPlayer1Id() ),
                p1Choice,
                dataProvider.getPlayerById( dataProvider.getPlayer2Id() ),
                p2Choice,
                Calendar.getInstance().getTime() );

        // sort facts to more interesting facts appear first
        Collections.sort(facts, new Comparator<HistoricalTrends.Fact>() {
            @Override
            public int compare(HistoricalTrends.Fact lhs, HistoricalTrends.Fact rhs) {
                return rhs.getScore() - lhs.getScore();
            }
        });

        return facts;
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

        List<HistoricalTrends.Fact> facts = getBattleFacts();

        factsListener = new FactsListener( facts, switcher, previous, next );
        next.setOnClickListener(factsListener);
        previous.setOnClickListener(factsListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.dataProvider = AppSingleton.getInstance().getDataProvider();

        p1Choice = "unknown";
        p2Choice = "unknown";

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(myToolbar);

        drawerLayout = (DrawerLayout) findViewById( R.id.mainDrawerLayout );

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawerLayout,
                myToolbar, R.string.drawer_open, R.string.drawer_close );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupTextSwitcher();

        p1Text = (TextView) findViewById(R.id.textP1);
        p1Text.setText( dataProvider.getPlayer1Name() + " choice:");
        p2Text = (TextView) findViewById(R.id.textP2);
        p2Text.setText( dataProvider.getPlayer2Name() + " choice:");

        p1Button = (Button) findViewById(R.id.btnChoiceP1);
        p1Button.setOnClickListener( this );

        p2Button = (Button) findViewById(R.id.btnChoiceP2);
        p2Button.setOnClickListener( this );

        p1Win = (Button) findViewById(R.id.btnWinP1);
        p1Win.setOnClickListener( this );

        p2Win = (Button) findViewById(R.id.btnWinP2);
        p2Win.setOnClickListener( this );

        p1Snackbar = Snackbar.make( drawerLayout,
                "Recorded win for " + dataProvider.getPlayer1Name(),
                Snackbar.LENGTH_LONG );
        p2Snackbar = Snackbar.make( drawerLayout,
                "Recorded win for " + dataProvider.getPlayer2Name(),
                Snackbar.LENGTH_LONG );

        setupDrawer();
    }

    private static class RecordWinTask extends AsyncTask<Void,Void,Void> {
        private String p1Choice;
        private String p2Choice;
        private String winnerId;
        private Snackbar notify;
        private Button p1WinButton;
        private Button p2WinButton;

        public RecordWinTask( String p1Choice,
                              String p2Choice,
                              String winnerId,
                              Button p1WinButton,
                              Button p2WinButton,
                              Snackbar notify ) {
            this.p1Choice = p1Choice;
            this.p2Choice = p2Choice;
            this.winnerId = winnerId;
            this.p1WinButton = p1WinButton;
            this.p2WinButton = p2WinButton;
            this.notify = notify;
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
        protected  void onPreExecute( ) {
            p1WinButton.setEnabled(false);
            p2WinButton.setEnabled(false);
        }

        @Override
        protected void onCancelled( Void result ) {
            p1WinButton.setEnabled(true);
            p2WinButton.setEnabled(true);
        }

        @Override
        protected void onPostExecute( Void result ) {
            Log.d( getClass().getName(), "Update complete" );
            p1WinButton.setEnabled(true);
            p2WinButton.setEnabled(true);
            notify.show();
        }
    }

    private void recordWin( String winnerId, Snackbar notify ) {
        // TODO loading screen
        Log.d( getLocalClassName(), "Recording win for " + winnerId );
        RecordWinTask record = new RecordWinTask( p1Choice, p2Choice, winnerId,
                p1Win, p2Win, notify );
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
            recordWin(dataProvider.getPlayer1Id(), p1Snackbar);
        } else if ( p2Win == v ) {
            recordWin(dataProvider.getPlayer2Id(), p2Snackbar);
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
            }
        } else if ( requestCode == GET_P2_CHARACTER ) {
            if ( null != data ) {
                p2Choice = data.getStringExtra( CharacterSelectActivity.GET_CHARACTER_PROPERTY );
                p2Button.setText( p2Choice );

                List<HistoricalTrends.Fact> facts = getBattleFacts();
                factsListener.replaceFacts(facts);
            }
        } else {
            Toast t = Toast.makeText( this.getApplicationContext(),
                    "Unrecognised Activity result", Toast.LENGTH_SHORT );
            t.show();
        }
    }
}
