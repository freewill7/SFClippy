package com.example.will.sfclippy;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity
implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button p1Button = (Button) findViewById(R.id.btnChoiceP1);
        p1Button.setOnClickListener( this );

        final Button p2Button = (Button) findViewById(R.id.btnChoiceP2);
        p2Button.setOnClickListener( this );

    }

    @Override
    public void onClick( View v ) {
        if ( R.id.btnChoiceP1 == v.getId() ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);
            startActivityForResult(intent, CharacterSelectActivity.GET_P1_CHARACTER);
        } else if ( R.id.btnChoiceP2 == v.getId() ) {
            Intent intent = new Intent(this, CharacterSelectActivity.class);
            startActivityForResult(intent, CharacterSelectActivity.GET_P2_CHARACTER);
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
            Button btn = (Button) findViewById( R.id.btnChoiceP2 );
            btn.setText( data.getStringExtra( CharacterSelectActivity.GET_CHARACTER_PROPERTY ));
        } else {
            Toast t = Toast.makeText( this.getApplicationContext(),
                    "Unrecognised Activity result", Toast.LENGTH_SHORT );
            t.show();
        }
    }
}
