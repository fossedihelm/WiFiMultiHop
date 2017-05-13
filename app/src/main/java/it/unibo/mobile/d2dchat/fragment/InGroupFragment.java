package it.unibo.mobile.d2dchat.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import it.unibo.mobile.d2dchat.MainActivity;
import it.unibo.mobile.d2dchat.R;
import it.unibo.mobile.d2dchat.databinding.FragmentReportBinding;
import it.unibo.mobile.d2dchat.device.DeviceManager;
import it.unibo.mobile.d2dchat.device.GroupOwner;
import it.unibo.mobile.d2dchat.infoReport.InfoMessage;


/**
 * chat fragment attached to main activity.
 */
public class InGroupFragment extends Fragment {
    public InfoMessage infoMessage;
    public FragmentReportBinding binding;
    TextView recvText;
    TextView sentText;

    private static final String TAG = "InGroupFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        infoMessage = ((MainActivity) getActivity()).mInfoMessage;
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_report, container, false);

        binding.setInfo(infoMessage);

        Switch toggle = (Switch) binding.getRoot().findViewById(R.id.switch1);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ((GroupOwner)((MainActivity) getActivity()).deviceManager.peer).setRole(GroupOwner.Role.generator);
                } else {
                    ((GroupOwner)((MainActivity) getActivity()).deviceManager.peer).setRole(GroupOwner.Role.replier);
                }
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
        super.onActivityResult(requestCode, resultCode, data);
    }

}

