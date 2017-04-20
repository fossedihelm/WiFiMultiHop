package it.unibo.mobile.d2dchat.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
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
import it.unibo.mobile.d2dchat.messagesManager.FileAttach;
import it.unibo.mobile.d2dchat.messagesManager.Message;


/**
 * chat fragment attached to main activity.
 */
public class ChatFragment extends ListFragment {
    ChatMessageAdapter listAdapter = null;
    Button send = null;
    View view;
    Spinner receiveSpinner;
    EditText textMessage;
    private static final int FILE_SELECT_CODE = 0;
    List<String> ad = new ArrayList<>();

    ArrayAdapter<String> spinnerAdapter;

    private static final String TAG = "ChatFragment";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat_new, container, false);
        receiveSpinner = (Spinner) view.findViewById(R.id.spinner);
        textMessage = (EditText) view.findViewById(R.id.editText);
        if (!ad.contains(Constants.GROUP_MESSAGE))
            ad.add(Constants.GROUP_MESSAGE);
        spinnerAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, ad);
        receiveSpinner.setAdapter(spinnerAdapter);

        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        view.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


        return view;
    }

    public void setParticipants(List<String> participants) {
        if (spinnerAdapter == null) { //Ancora non viene creato, probabilmente è la prima volta che entriamo...Aspettiamo la creazione
            ad.add(Constants.GROUP_MESSAGE);
            ad.addAll(participants);
        } else { //E' già creato, aggiungiamo semplicemente la roba
            spinnerAdapter.clear();
            spinnerAdapter.add(Constants.GROUP_MESSAGE);
            spinnerAdapter.addAll(participants);
            spinnerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listAdapter = new ChatMessageAdapter(this.getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1,
                new ArrayList<Message>());
        setListAdapter(listAdapter);

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
                    receiver = receiveSpinner.getSelectedItem().toString(); //receiver
                    ((MainActivity) getActivity()).sendFile(uri, receiver);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    Chiamato al click su un elemento della lista. In questo caso è una lista di Message
    Viene utilizzata per scaricare i file, valuto solo i click sui Message di tipo file.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Message selectedMessage = (Message) l.getItemAtPosition(position); //Recupero il message cliccato
        if (selectedMessage.getType() == Constants.MESSAGE_FILE && !
                selectedMessage.getSource().equals(((MainActivity)getActivity()).getDeviceName())) {
            FileAttach file = (FileAttach) selectedMessage.getData();
            FileOutputStream outputStream;
            try {
                //Creiamo un file nella cartella di download di default (memoria interna dovrebbe essere), con lo stesso nome del file ricevuto
                File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file.fileName);
                //Scriviamo i byte ricevuti nel nuovo file creato
                outputStream = new FileOutputStream(newFile);
                outputStream.write(file.data);
                outputStream.close();
                Log.d(TAG, "File scaricato: " + newFile.getPath() + "/" + newFile.getName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage() {
        String receiver = receiveSpinner.getSelectedItem().toString(); //receiver
        String message = textMessage.getText().toString();
        if (!message.equals(""))
            ((MainActivity) getActivity()).writeTextMessage(message, receiver);

        textMessage.setText("");
        //nascondiamo la tastiera, veramente si deve fare tutto questo?
        InputMethodManager inputManager =
                (InputMethodManager) getActivity().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            //Chiede android 23
            //Toast.makeText(this.getContext(), "Please install a File Manager.",Toast.LENGTH_SHORT).show();
        }
    }


    public class ChatMessageAdapter extends ArrayAdapter<Message> {

        private List<Message> messages;

        public ChatMessageAdapter(Context context, int resource,
                                  int textViewResourceId, List<Message> messages) {
            super(context, resource, textViewResourceId, messages);
            this.messages = messages;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }
            Message message = messages.get(position);
            if (message != null) {
                TextView nameText = (TextView) v
                        .findViewById(android.R.id.text1);


                if (nameText != null) {
                    if (((MainActivity) getActivity()).getDeviceName().equals(message.getSource()))
                        nameText.setTextColor(Color.RED); //L'ho inviato io
                    else
                        nameText.setTextColor(Color.BLACK); //L'ho ricevuto
                    String sender = message.getSource();
                    sender += (message.getDest().equals("all")) ? "" : " to " + message.getDest();
                    switch (message.getType()) {
                        case Constants.MESSAGE_TEXT:
                            nameText.setText(sender + " : " + message.getData().toString());
                            break;
                        case Constants.MESSAGE_FILE:
                            FileAttach file = (FileAttach) message.getData();
                            nameText.setText(sender + " : Invio file " + file.fileName + " grandezza: " + file.data.length / 1000 + " kb");
                    }
                }

            }
            return v;
        }
    }


    public List<Message> getMessages() {
        return listAdapter.messages;
    }

    public void setMessages(List<Message> messages) {
        listAdapter.messages = messages;
    }

    public void addMessage(Message message){
        listAdapter.messages.add(message);
    }

    public ChatMessageAdapter getListAdapter() {
        return listAdapter;
    }

}

