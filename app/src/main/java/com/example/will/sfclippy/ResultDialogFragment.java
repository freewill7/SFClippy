package com.example.will.sfclippy;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by will on 25/08/2016.
 */
public class ResultDialogFragment extends DialogFragment
implements View.OnClickListener {
    public static String ITEM_ID_VAR = "itemId";
    private Button modifyButton;
    private Button deleteButton;
    private int itemId;
    private ResultDialogListener listener;

    @Override
    public void onAttach( Activity activity ) {
        super.onAttach(activity);
        try {
            listener = (ResultDialogListener) activity;
        } catch ( ClassCastException e ) {
            throw new ClassCastException( activity.toString()
                    + " must implement DialogListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState ) {
        View v = inflater.inflate(R.layout.fragment_result_options, container, false);
        modifyButton = (Button) v.findViewById(R.id.btnModifyResult);
        modifyButton.setOnClickListener(this);
        deleteButton = (Button) v.findViewById(R.id.btnDeleteResult);
        deleteButton.setOnClickListener(this);
        Bundle bundle = getArguments();
        itemId = bundle.getInt( ITEM_ID_VAR );
        return v;
    }

    @Override
    public void onClick( View view ) {
        if ( view == modifyButton ) {
            Log.d( getClass().getName(), "Modify button pressed");
            Toast toast = Toast.makeText( getContext(), "Modify not yet implemented", Toast.LENGTH_SHORT );
            toast.show();
        } else if ( view == deleteButton ) {
            Log.d( getClass().getName(), "Delete button pressed");
            listener.removeItem( itemId );
            dismiss();
        }
    }

    /**
     * Created by will on 25/08/2016.
     */
    public interface ResultDialogListener {
        void removeItem( int itemIndex );
    }
}
