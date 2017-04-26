package it.unibo.mobile.d2dchat;

/**
 * Created by ghosty on 21/04/17.
 */

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

public class MyDialogFragment extends DialogFragment implements View.OnClickListener {

    private EditText mEditText;
    private NumberPicker mNumberPicker;
    private Button buttonSend;

    final String[] values= {"5", "10", "15", "20", "25"};


    public interface TimeListener {
        void onFinishTimeDialog(Integer time);
    }

    // Empty constructor required for DialogFragment
    public MyDialogFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seconds, container);

        buttonSend = (Button) view.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);
        mNumberPicker = (NumberPicker) view.findViewById(R.id.np);
        mNumberPicker.setMinValue(0); //from array first value
        mNumberPicker.setValue(1); //from array default value
        mNumberPicker.setMaxValue(values.length-1); //to array last value
        //Specify the NumberPicker data source as array elements
        mNumberPicker.setDisplayedValues(values);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        mNumberPicker.setWrapSelectorWheel(true);


//        mEditText = (EditText) view.findViewById(R.id.interval);
//
////         set this instance as callback for editor action
//        mEditText.setOnEditorActionListener(this);
//        mEditText.requestFocus();
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//        getDialog().setTitle("Please enter milliseconds");

        return view;
    }

    @Override
    public void onClick(View view) {
        TimeListener activity = (TimeListener) getActivity();
        activity.onFinishTimeDialog(Integer.parseInt(values[mNumberPicker.getValue()])*1000);
        this.dismiss();
    }

//    @Override
//    public boolean onClick(TextView v, int actionId, KeyEvent event) {
//        // Return input text to activity
//        TimeListener activity = (TimeListener) getActivity();
//        activity.onFinishTimeDialog(Integer.parseInt(values[mNumberPicker.getValue()]));
////        activity.onFinishTimeDialog(Integer.parseInt(mEditText.getText().toString()));
//        this.dismiss();
//        return true;
//    }

}