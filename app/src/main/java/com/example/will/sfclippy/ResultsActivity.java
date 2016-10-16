package com.example.will.sfclippy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.will.sfclippy.models.BattleResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Activity for showing all recorded results.
 */
public class ResultsActivity extends AppCompatActivity
implements ResultDialogFragment.ResultDialogListener {
    private ResultsAdapter mResultsAdapter;
    public static final String ACCOUNT_ID = "account_id";
    public static final String P1_ID = "player1_id";
    public static final String P2_ID = "player2_id";
    private static final String TAG = "ResultsActivity";
    private DatabaseReference mResults;

    public static class ResultViewHolder extends RecyclerView.ViewHolder
            implements View.OnLongClickListener {
        int entryIndex;
        private final Activity mActivity;
        public final ImageView mWinnerImage;
        public final TextView mDateView;
        public final TextView mResultPairing;

        public ResultViewHolder( Activity activity, View container ) {
            super(container);
            this.mActivity = activity;
            container.setLongClickable(true);
            container.setOnLongClickListener(this);
            mWinnerImage = (ImageView) container.findViewById( R.id.textResultImg );
            mDateView = (TextView) container.findViewById( R.id.textResultDateLabel );
            mResultPairing = (TextView) container.findViewById( R.id.textResultPairing );
        }

        @Override
        public boolean onLongClick( View view ) {
            Bundle bundle = new Bundle();
            bundle.putInt( ResultDialogFragment.ITEM_ID_VAR, entryIndex );

            ResultDialogFragment dialog = new ResultDialogFragment();
            dialog.setArguments(bundle);
            dialog.show( mActivity.getFragmentManager(), "frame name" );

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
        private final Activity activity;
        private final DatabaseReference resultsRef;
        private List<BattleResult> results;
        private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM", Locale.UK);
        private final String p1Id;
        private final String p2Id;
        private final Drawable p1Img;
        private final Drawable p2Img;
        private final Comparator<BattleResult> orderer = new DescendingDateOrderer();

        /**
         * Elements of the view.
         */
        public ResultsAdapter( Activity activity,
                               DatabaseReference resultsRef,
                               String p1Id,
                               String p2Id,
                               Drawable p1Img,
                               Drawable p2Img ) {
            this.activity = activity;
            this.resultsRef = resultsRef;
            this.results = new ArrayList<>();
            this.p1Id = p1Id;
            this.p2Id = p2Id;
            this.p1Img = p1Img;
            this.p2Img = p2Img;
        }

        @Override
        public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            View entry = LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.layout_result, parent, false );
            return new ResultViewHolder( activity, entry );
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
                holder.mWinnerImage.setImageDrawable( p1Img );
            } else {
                holder.mWinnerImage.setImageDrawable( p2Img );
            }
            holder.mResultPairing.setText( p1String + " vs " + p2String );
            String date = "unknown";
            String tmpDate = dateFormat.format( result.dateAsDate());
            if ( null != tmpDate ) {
                date = tmpDate;
            }
            holder.mDateView.setText(date);
        }

        /**
         * Remove an item from the view and underlying data structure.
         * @param itemIndex The item index to remove.
         */
        public void removeItem( int itemIndex ) {
            String battleId = results.get(itemIndex).battleId;
            resultsRef.child(battleId).removeValue();
            // results.remove(itemIndex);
            // notifyItemRemoved(itemIndex);
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

        Intent intent = getIntent();
        String accountId = intent.getStringExtra(ACCOUNT_ID);
        String player1Id = intent.getStringExtra(P1_ID);
        String player2Id = intent.getStringExtra(P2_ID);

        DatabaseHelper dbHelper = new DatabaseHelper( FirebaseDatabase.getInstance(), accountId );

        // TODO update based on actual names
        TextDrawable p1Img = TextDrawable.builder()
                .buildRound( "R", Color.RED );
        TextDrawable p2Img = TextDrawable.builder()
                .buildRound( "B", Color.BLUE );

        // fetch results to display
        mResults = dbHelper.getResultsDirReference();
        mResultsAdapter = new ResultsAdapter( this, mResults, player1Id, player2Id, p1Img, p2Img );
        mResults.addValueEventListener(mResultsAdapter);
        listView.setAdapter(mResultsAdapter);
    }

    @Override
    public void removeItem( int itemIndex ) {
        mResultsAdapter.removeItem(itemIndex);
    }

    @Override
    public void onDestroy( ) {
        mResults.removeEventListener(mResultsAdapter);
        super.onDestroy();
    }
}
