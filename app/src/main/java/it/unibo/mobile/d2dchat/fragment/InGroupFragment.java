package it.unibo.mobile.d2dchat.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.MainActivity;
import it.unibo.mobile.d2dchat.R;
import it.unibo.mobile.d2dchat.databinding.FragmentChatNewBinding;
import it.unibo.mobile.d2dchat.infoReport.InfoMessage;
import it.unibo.mobile.d2dchat.messagesManager.FileAttach;
import it.unibo.mobile.d2dchat.messagesManager.Message;


/**
 * chat fragment attached to main activity.
 */
public class InGroupFragment extends Fragment {
    Button send = null;
    View view;
    public InfoMessage infoMessage = new InfoMessage();
    public FragmentChatNewBinding binding;
    TextView recvText;
    TextView sentText;
    private static final int FILE_SELECT_CODE = 0;

    private static final String TAG = "InGroupFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        view = inflater.inflate(R.layout.fragment_chat_new, container, false);
//        recvText = (TextView) view.findViewById(R.id.recv_mess);
//        sentText = (TextView) view.findViewById(R.id.sent_mess);
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_chat_new, container, false);

        binding.setInfo(infoMessage);
//        infoMessage.setPartialRecvMessage(300);

        binding.getRoot().findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoMessage.setPartialRecvMessage(infoMessage.getPartialRecvMessage()+1);
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    String receiver;
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    //We have the URI of selected files, we need to check the spinner to retrieve the name of receiver
//                    receiver = receiveSpinner.getSelectedItem().toString(); //receiver
//                    ((MainActivity) getActivity()).sendFile(uri, receiver);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}

