package com.example.will.sfclippy;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class MainActivity extends Activity
implements View.OnClickListener {
    private DataProvider dataProvider;

    private TextView p1Text;
    private TextView p2Text;
    private Button p1Button;
    private Button p2Button;
    private Button p1Win;
    private Button p2Win;

    static public final int GET_P1_CHARACTER = 1;
    static public final int GET_P2_CHARACTER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.dataProvider = AppSingleton.getInstance().getDataProvider();

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
    }

    private void recordWin( String winner ) {
        Log.e( getLocalClassName(), "record win not implemented - " + winner);
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
            recordWin( AppSingleton.getInstance().getDataProvider().getPlayer1Id() );
        } else if ( p2Win == v ) {
            recordWin( AppSingleton.getInstance().getDataProvider().getPlayer2Id() );
        } else {
            Toast t = Toast.makeText( v.getContext(), "Unknown button", Toast.LENGTH_SHORT );
            t.show();
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if ( requestCode == GET_P1_CHARACTER ) {
            if ( null != data ) {
                String choice = data.getStringExtra( CharacterSelectActivity.GET_CHARACTER_PROPERTY );
                p1Button.setText( choice );
            }
        } else if ( requestCode == GET_P2_CHARACTER ) {
            if ( null != data ) {
                String choice = data.getStringExtra( CharacterSelectActivity.GET_CHARACTER_PROPERTY );
                p2Button.setText( choice );
            }
        } else {
            Toast t = Toast.makeText( this.getApplicationContext(),
                    "Unrecognised Activity result", Toast.LENGTH_SHORT );
            t.show();
        }
    }
}
