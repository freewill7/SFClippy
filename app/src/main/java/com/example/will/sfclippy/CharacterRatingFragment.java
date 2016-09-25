package com.example.will.sfclippy;

import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.will.sfclippy.models.BattleCounter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CharacterRatingFragment.RatingInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CharacterRatingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CharacterRatingFragment extends DialogFragment
        implements RatingBar.OnRatingBarChangeListener, View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ACCOUNT_ID = "accountId";
    private static final String ARG_PLAYER_ID = "playerId";
    private static final String ARG_CHARACTER_NAME = "characterName";
    private static final String ARG_CHARACTER_SCORE = "characterScore";
    private static final String TAG = "CharacterRatingFragment";

    private String mAccountId;
    private String mPlayerId;
    private String mCharacterName;
    private DatabaseHelper mHelper;

    private int mInitialRating;
    private RatingBar mRatingBar;
    private Button mButton;

    private TextView mLabelFirst;
    private TextView mLabelSecond;
    private TextView mLabelThird;

    private RatingInteractionListener mListener;

    public CharacterRatingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param accountId The account id within firebase.
     * @param playerId The player id.
     * @param characterName The character name.
     * @param characterScore The character score.
     * @return A new instance of fragment CharacterRatingFragment.
     */
    public static CharacterRatingFragment newInstance(String accountId,
                                                      String playerId,
                                                      String characterName,
                                                      int characterScore) {
        CharacterRatingFragment fragment = new CharacterRatingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACCOUNT_ID, accountId);
        args.putString(ARG_PLAYER_ID, playerId);
        args.putString(ARG_CHARACTER_NAME, characterName);
        args.putInt(ARG_CHARACTER_SCORE, characterScore);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccountId = getArguments().getString(ARG_ACCOUNT_ID);
            mPlayerId = getArguments().getString(ARG_PLAYER_ID);
            mCharacterName = getArguments().getString(ARG_CHARACTER_NAME);
            mInitialRating = getArguments().getInt(ARG_CHARACTER_SCORE);
            mHelper = new DatabaseHelper( FirebaseDatabase.getInstance(), mAccountId );
        }
    }

    private static String getDaySuffix( Date previous, Date current ) {
        String ret = "";

        if ( null != previous && null != current ) {
            long nowMs = current.getTime();
            long then = previous.getTime();

            long diff = nowMs - then;
            long days = diff / (1000 * 60 * 60 * 24);

            ret = String.format( "(%d days)", days );
        }

        return ret;
    }

    private String getRunInfo( BattleCounter counter ) {
        String ret = "";

        if ( counter.winsSinceLoss > 0 ) {
            String daySuffix = getDaySuffix( counter.lastDefeatAsDate(),
                    counter.lastVictoryAsDate() );
            ret = String.format( "No losses in %d battles %s",
                    counter.winsSinceLoss, daySuffix );

        } else if ( counter.lossesSinceLastWin > 0 ) {
            String daySuffix = getDaySuffix( counter.lastVictoryAsDate(),
                    counter.lastDefeatAsDate() );
            ret = String.format( "No wins in %d battles %s",
                    counter.lossesSinceLastWin,
                    daySuffix );
        }

        return ret;
    }

    private void updateLabels( BattleCounter counter ) {
        if ( 0 != counter.battles ) {
            mLabelFirst.setText(
                    String.format(Locale.UK, "Played %d times",
                            counter.battles)
            );

            mLabelSecond.setText(
                    String.format(Locale.UK, "Won %d battles (%d%%)",
                            counter.wins,
                            (100 * counter.wins) /  counter.battles)
            );

            mLabelThird.setText(
                    getRunInfo(counter)
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_character_rating, container, false);

        TextView textView = (TextView) view.findViewById(R.id.labelTitle);
        textView.setText( mCharacterName );

        mRatingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        mRatingBar.setRating( mInitialRating );
        mRatingBar.setOnRatingBarChangeListener(this);

        mButton  = (Button) view.findViewById(R.id.btnSelectCharacter);
        mButton.setOnClickListener(this);

        mLabelFirst = (TextView) view.findViewById(R.id.lblFirstFact);
        mLabelSecond = (TextView) view.findViewById(R.id.lblSecondFact);
        mLabelThird = (TextView) view.findViewById(R.id.lblThirdFact);

        // statistics
        final DatabaseReference charReference =
                mHelper.getCharacterStatistics( mPlayerId, mCharacterName );
        charReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                charReference.removeEventListener(this);
                if ( null != dataSnapshot ) {
                    BattleCounter counter = dataSnapshot.getValue(BattleCounter.class);
                    if ( null != counter ) {
                        updateLabels( counter );
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e( TAG, "Problem with character statistics", databaseError.toException() );
            }
        });

        return view;
    }

    @Override
    public void onRatingChanged( RatingBar ratingBar, float rating, boolean fromUser ) {
        if (mListener != null) {
            mListener.onRatingChange( mCharacterName, (int) rating);
            dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RatingInteractionListener) {
            mListener = (RatingInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick( View v ) {
        if ( v == mButton ) {
            mListener.onCharacterSelected( mCharacterName );
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface RatingInteractionListener {
        /**
         * Notify of a change in rating.
         * @param name The character whose rating changed.
         * @param rating The new rating.
         */
        void onRatingChange(String name, int rating);

        /**
         * Notify of a character choice.
         * @param name The character name.
         */
        void onCharacterSelected( String name );
    }
}
