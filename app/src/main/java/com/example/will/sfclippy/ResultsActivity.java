package com.example.will.sfclippy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.Result;

import org.w3c.dom.Text;

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

        /**
         * Elements of the view.
         */
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;

            public ViewHolder( TextView textView ) {
                super(textView);
                mTextView = textView;
            }
        }

        public ResultsAdapter( List<DataProvider.BattleResult> results,
                               String p1Id,
                               String p2Id ) {
            this.results = results;
            this.p1Id = p1Id;
            this.p2Id = p2Id;
        }

        @Override
        public ResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
            TextView textView = (TextView) LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.text_result, parent, false );
            ViewHolder vh = new ViewHolder( textView );
            return vh;
        }

        @Override
        public int getItemCount( ) {
            return results.size();
        }

        @Override
        public void onBindViewHolder( ViewHolder holder, int position ) {
            DataProvider.BattleResult result = results.get(position);

            String p1String = result.characterFor( p1Id );
            if ( result.winner( p1Id) ) {
                p1String = p1String + " (winner)";
            }

            String p2String = result.characterFor( p2Id );
            if ( result.winner( p2Id) ) {
                p2String = p2String + " (winner)";
            }

            String label = dateFormat.format( result.getDate() )
                    + " "
                    + p1String
                    + " vs "
                    + p2String;
            holder.mTextView.setText(label);;
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

        // create adapter
        Log.d( getLocalClassName(), "Creating adapter for " + results.size());
        RecyclerView.Adapter mAdapter = new ResultsAdapter( results, player1Id, player2Id );
        listView.setAdapter(mAdapter);
    }
}
