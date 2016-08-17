package com.example.will.sfclippy;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by will on 13/08/2016.
 */
public class ResultsActivity extends Activity {
    public static class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
        private List<DataProvider.BattleResult> results;
        private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private String p1Id;
        private String p2Id;
        private Drawable winImg;

        /**
         * Elements of the view.
         */
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView dateView;
            public TextView p1Player;
            public TextView p2Player;

            public ViewHolder( View container ) {
                super(container);
                dateView = (TextView) container.findViewById( R.id.textResultDate );
                p1Player = (TextView) container.findViewById( R.id.textResultP1 );
                p2Player = (TextView) container.findViewById( R.id.textResultP2 );
            }
        }

        public ResultsAdapter( List<DataProvider.BattleResult> results,
                               String p1Id,
                               String p2Id,
                               Drawable winImg ) {
            this.results = results;
            this.p1Id = p1Id;
            this.p2Id = p2Id;
            this.winImg = winImg;
        }

        @Override
        public ResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            LinearLayout entry = (LinearLayout) LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.layout_result, parent, false );
            ViewHolder vh = new ViewHolder( entry );
            return vh;
        }

        @Override
        public int getItemCount( ) {
            return results.size();
        }

        @Override
        public void onBindViewHolder( ViewHolder holder, int position ) {
            DataProvider.BattleResult result = results.get(position);

            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD );

            CharSequence p1String = result.characterFor( p1Id );
            if ( result.winner( p1Id) ) {
                SpannableStringBuilder str = new SpannableStringBuilder( p1String );
                str.setSpan( boldSpan, 0, p1String.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE );
                /* str.setSpan( new ImageSpan( winImg, "src", DynamicDrawableSpan.ALIGN_BOTTOM ),
                        0, 1,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE ); */
                p1String = str;
            }

            CharSequence p2String = result.characterFor( p2Id );
            if ( result.winner( p2Id) ) {
                SpannableString str = new SpannableString(p2String);
                str.setSpan( boldSpan, 0, p2String.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE );
                p2String = str;
            }

            holder.dateView.setText( dateFormat.format( result.getDate()) );
            holder.p1Player.setText( p1String );
            holder.p2Player.setText( p2String );
        }
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        final RecyclerView listView = (RecyclerView) findViewById( R.id.resultsList );
        listView.setHasFixedSize(true);

        // use linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        DataProvider provider = AppSingleton.getInstance().getDataProvider();
        String player1Id = provider.getPlayer1Id();
        String player2Id = provider.getPlayer2Id();

        TextView p1Label = (TextView) findViewById(R.id.textResultLabelP1);
        p1Label.setText( provider.getPlayer1Name() );

        TextView p2Label = (TextView) findViewById(R.id.textResultLabelP2);
        p2Label.setText( provider.getPlayer2Name() );

        // fetch results to display
        List<DataProvider.BattleResult> results = AppSingleton.getInstance().getDataProvider()
                .getCurrentPlayerResults();
        Collections.sort(results, new Comparator<DataProvider.BattleResult>() {
            @Override
            public int compare(DataProvider.BattleResult lhs, DataProvider.BattleResult rhs) {
                // descending date/time order
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });

        Drawable winImg = getResources().getDrawable(R.drawable.ic_star_border_black_24dp,
                getTheme() );
        winImg.setBounds(0, 0, winImg.getIntrinsicWidth(), winImg.getIntrinsicHeight() );

        // create adapter
        Log.d( getLocalClassName(), "Creating adapter for " + results.size());
        RecyclerView.Adapter mAdapter = new ResultsAdapter( results, player1Id, player2Id, winImg );
        listView.setAdapter(mAdapter);
    }
}
