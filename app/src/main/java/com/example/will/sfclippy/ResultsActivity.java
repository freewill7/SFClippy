package com.example.will.sfclippy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.will.sfclippy.models.BattleResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by will on 13/08/2016.
 */
public class ResultsActivity extends AppCompatActivity
implements ResultDialogListener {
    private ResultsAdapter mResultsAdapter;
    private DataProvider mDataProvider;
    private static final int REQUEST_SAVE_RESULTS = 1001;
    public static final String P1_ID = "player1_id";
    public static final String P2_ID = "player2_id";
    private static final String TAG = "ResultsActivity";
    private DatabaseReference mResults;

    public static class ResultViewHolder extends RecyclerView.ViewHolder
            implements View.OnLongClickListener {
        int entryIndex;
        private Activity activity;
        public ImageView winnerImg;
        public TextView dateView;
        public TextView resultPairing;

        public ResultViewHolder( Activity activity, View container ) {
            super(container);
            this.activity = activity;
            container.setLongClickable(true);
            container.setOnLongClickListener(this);
            winnerImg = (ImageView) container.findViewById( R.id.textResultImg );
            dateView = (TextView) container.findViewById( R.id.textResultDateLabel );
            resultPairing = (TextView) container.findViewById( R.id.textResultPairing );
        }

        @Override
        public boolean onLongClick( View view ) {
            Bundle bundle = new Bundle();
            bundle.putInt( ResultDialog.ITEM_ID_VAR, entryIndex );

            ResultDialog dialog = new ResultDialog();
            dialog.setArguments(bundle);
            dialog.show( activity.getFragmentManager(), "frame name" );

            return true;
        }
    }

    public static class DescendingDateOrderer implements Comparator<BattleResult> {
        @Override
        public int compare( BattleResult a, BattleResult b ) {
            return b.date.compareTo(a.date);
        }
    }

    public static class ResultsAdapter extends RecyclerView.Adapter<ResultViewHolder>
    implements ValueEventListener {
        private Activity activity;
        private List<BattleResult> results;
        private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM");
        private String p1Id;
        private String p2Id;
        private Drawable p1Img;
        private Drawable p2Img;
        private Comparator<BattleResult> orderer;

        /**
         * Elements of the view.
         */
        public ResultsAdapter( Activity activity,
                               String p1Id,
                               String p2Id,
                               Drawable p1Img,
                               Drawable p2Img ) {
            this.activity = activity;
            this.results = new ArrayList<>();
            this.p1Id = p1Id;
            this.p2Id = p2Id;
            this.p1Img = p1Img;
            this.p2Img = p2Img;
            orderer = new DescendingDateOrderer();
        }

        @Override
        public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            View entry = LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.layout_result, parent, false );
            ResultViewHolder vh = new ResultViewHolder( activity, entry );
            return vh;
        }

        @Override
        public int getItemCount( ) {
            return results.size();
        }

        @Override
        public void onBindViewHolder( ResultViewHolder holder, int position ) {
            BattleResult result = results.get(position);

            CharSequence p1String = result.characterFor( p1Id );
            CharSequence p2String = result.characterFor( p2Id );

            holder.entryIndex = position;
            if ( result.winnerId.equals(p1Id) ) {
                holder.winnerImg.setImageDrawable( p1Img );
            } else {
                holder.winnerImg.setImageDrawable( p2Img );
            }
            holder.resultPairing.setText( p1String + " vs " + p2String );
            String date = "unknown";
            try {
                date = dateFormat.format( result.dateAsDate());
            } catch ( ParseException parseError ) {
                Log.e( TAG, "Failed to parse date", parseError );
            }
            holder.dateView.setText(date);
        }

        /**
         * Remove an item from the view and underlying data structure.
         * @param itemIndex The item index to remove.
         */
        public void removeItem( int itemIndex ) {
            results.remove(itemIndex);
            notifyItemRemoved(itemIndex);
        }

        @Override
        public void onCancelled(DatabaseError error) {
            Log.e( TAG, "Cancelled listener", error.toException());
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            Log.d( TAG, "path is " + snapshot.getKey() );
            List<BattleResult> results = new ArrayList<>();
            for ( DataSnapshot child : snapshot.getChildren() ) {
                Log.d( TAG, "child is " + child.getKey());

                for ( DataSnapshot mini : child.getChildren() ) {
                    Log.d( TAG, "mini is " + mini.getKey());
                }

                BattleResult result = child.getValue(BattleResult.class);

                results.add( result );
            }

            // sort
            Collections.sort( results, orderer );

            this.results = results;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Results");

        final RecyclerView listView = (RecyclerView) findViewById( R.id.resultsList );
        listView.setHasFixedSize(true);

        // use linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        mDataProvider = AppSingleton.getInstance().getDataProvider();

        Intent intent = getIntent();
        String player1Id = intent.getStringExtra(P1_ID);
        String player2Id = intent.getStringExtra(P2_ID);


        // TODO update based on actual names
        TextDrawable p1Img = TextDrawable.builder()
                .buildRound( "R", Color.RED );
        TextDrawable p2Img = TextDrawable.builder()
                .buildRound( "B", Color.BLUE );

        // fetch results to display
        mResults = mDataProvider.getResults();
        mResultsAdapter = new ResultsAdapter( this, player1Id, player2Id, p1Img, p2Img );
        mResults.addValueEventListener(mResultsAdapter);
        listView.setAdapter(mResultsAdapter);
                /*
        List<DataProvider.BattleResult> results = mDataProvider.getCurrentPlayerResults();
        Collections.sort(results, new Comparator<DataProvider.BattleResult>() {
            @Override
            public int compare(DataProvider.BattleResult lhs, DataProvider.BattleResult rhs) {
                // descending date/time order
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });
        */
        // create adapter

    }

    @Override
    public void removeItem( int itemIndex ) {
        mResultsAdapter.removeItem(itemIndex);
    }

    private static class SaveResults extends AsyncTask<Void,String,Void> {
        private DataProvider dataProvider;
        private Activity caller;

        public SaveResults( DataProvider dataProvider,
                            Activity caller ) {
            this.dataProvider = dataProvider;
            this.caller = caller;
        }

        @Override
        public Void doInBackground( Void ... params ) {
            // TODO progress and animation
            try {
                dataProvider.saveResults();
            } catch ( IOException ioe ) {
                cancel(true);
            }

            return null;
        }

        @Override
        public void onPostExecute( Void result ) {
            caller.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_results, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.action_accept_results:
                SaveResults results = new SaveResults(mDataProvider, this );
                results.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onDestroy( ) {
        mResults.removeEventListener(mResultsAdapter);
        super.onDestroy();
    }
}
