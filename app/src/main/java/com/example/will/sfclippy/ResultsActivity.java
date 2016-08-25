package com.example.will.sfclippy;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.gms.vision.text.Text;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by will on 13/08/2016.
 */
public class ResultsActivity extends AppCompatActivity
implements ResultDialogListener {
    private ResultsAdapter resultsAdapter;

    public static class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
        private Activity activity;
        private List<DataProvider.BattleResult> results;
        private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM");
        private String p1Id;
        private String p2Id;
        private Drawable p1Img;
        private Drawable p2Img;

        /**
         * Elements of the view.
         */
        public static class ViewHolder extends RecyclerView.ViewHolder
        implements View.OnLongClickListener {
            int entryIndex;
            private Activity activity;
            public ImageView winnerImg;
            public TextView dateView;
            public TextView resultPairing;

            public ViewHolder( Activity activity, View container ) {
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

        public ResultsAdapter( Activity activity,
                               List<DataProvider.BattleResult> results,
                               String p1Id,
                               String p2Id,
                               Drawable p1Img,
                               Drawable p2Img ) {
            this.activity = activity;
            this.results = results;
            this.p1Id = p1Id;
            this.p2Id = p2Id;
            this.p1Img = p1Img;
            this.p2Img = p2Img;
        }

        @Override
        public ResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            View entry = LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.layout_result, parent, false );
            ViewHolder vh = new ViewHolder( activity, entry );
            return vh;
        }

        @Override
        public int getItemCount( ) {
            return results.size();
        }

        @Override
        public void onBindViewHolder( ViewHolder holder, int position ) {
            DataProvider.BattleResult result = results.get(position);

            CharSequence p1String = result.characterFor( p1Id );
            CharSequence p2String = result.characterFor( p2Id );

            holder.entryIndex = position;
            if ( result.winner(p1Id) ) {
                holder.winnerImg.setImageDrawable( p1Img );
            } else {
                holder.winnerImg.setImageDrawable( p2Img );
            }
            holder.resultPairing.setText( p1String + " vs " + p2String );
            holder.dateView.setText( dateFormat.format( result.getDate()) );
        }

        /**
         * Remove an item from the view and underlying data structure.
         * @param itemIndex The item index to remove.
         */
        public void removeItem( int itemIndex ) {
            results.remove(itemIndex);
            notifyItemRemoved(itemIndex);
        }
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final RecyclerView listView = (RecyclerView) findViewById( R.id.resultsList );
        listView.setHasFixedSize(true);

        // use linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        DataProvider provider = AppSingleton.getInstance().getDataProvider();
        String player1Id = provider.getPlayer1Id();
        String player2Id = provider.getPlayer2Id();

        // fetch results to display
        List<DataProvider.BattleResult> results = provider.getCurrentPlayerResults();
        Collections.sort(results, new Comparator<DataProvider.BattleResult>() {
            @Override
            public int compare(DataProvider.BattleResult lhs, DataProvider.BattleResult rhs) {
                // descending date/time order
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });

        TextDrawable p1Img = TextDrawable.builder()
                .buildRound( provider.getPlayer1Name().substring(0, 1), Color.RED );
        TextDrawable p2Img = TextDrawable.builder()
                .buildRound( provider.getPlayer2Name().substring(0, 1), Color.BLUE );

        // create adapter
        Log.d( getLocalClassName(), "Creating adapter for " + results.size());
        resultsAdapter = new ResultsAdapter( this, results, player1Id, player2Id,
                p1Img, p2Img );
        listView.setAdapter(resultsAdapter);
    }

    @Override
    public void removeItem( int itemIndex ) {
        resultsAdapter.removeItem(itemIndex);
    }
}
