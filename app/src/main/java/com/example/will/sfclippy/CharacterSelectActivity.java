package com.example.will.sfclippy;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

public class CharacterSelectActivity extends Activity {
    static public final int GET_P1_CHARACTER = 1;
    static public final int GET_P2_CHARACTER = 2;
    static public final String GET_CHARACTER_PROPERTY = "choice";

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private String[] mDataset;
        private Activity mActivity;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public Activity mActivity;
            public Button mButton;
            public ViewHolder( Activity activity, Button button ) {
                super(button);
                mActivity = activity;
                mButton = button;

                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent resultData = new Intent();
                        resultData.putExtra(GET_CHARACTER_PROPERTY, mButton.getText());
                        mActivity.setResult(Activity.RESULT_OK, resultData);
                        mActivity.finish();
                    }
                });
            }
        }

        public MyAdapter( Activity activity, String[] myDataset ) {
            mActivity = activity;
            mDataset = myDataset;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            ViewHolder vh = new ViewHolder( mActivity, new Button(parent.getContext() ));
            return vh;
        }

        @Override
        public void onBindViewHolder( ViewHolder holder, int position ) {
            holder.mButton.setText( mDataset[position] );
        }

        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_select);

        LinkedList<String> choices = new LinkedList<>();
        choices.add( "Ryu" );
        choices.add( "Chun-Li" );
        choices.add( "Nash");
        choices.add( "M.Bison" );
        choices.add( "Cammy" );
        choices.add( "Birdie" );
        choices.add( "Ken" );
        choices.add( "Necalli" );
        choices.add( "Vega" );
        choices.add( "R.Mika" );
        choices.add( "Rashid" );
        choices.add( "Karin" );
        choices.add( "Zangief" );
        choices.add( "Cammy" );
        choices.add( "Laura" );
        choices.add( "Dhalsim" );
        choices.add( "F.A.N.G." );
        choices.add( "Alex" );
        choices.add( "Guile" );
        choices.add( "Ibuki" );
        choices.add( "Balrog" );

        final RecyclerView listView = (RecyclerView) findViewById( R.id.characterList );
        listView.setHasFixedSize(true);

        // use linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        // specify an adapter
        String[] arr = choices.toArray( new String[choices.size()]);
        RecyclerView.Adapter mAdapter = new MyAdapter( this, arr );
        listView.setAdapter(mAdapter);

    }
}
