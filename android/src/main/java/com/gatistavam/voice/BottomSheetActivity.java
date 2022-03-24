package com.gatistavam.voice;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.twilio.audioswitch.AudioDevice;
import com.twilio.audioswitch.AudioSwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BottomSheetActivity extends BottomSheetDialogFragment {
    private AudioManager audioManager;
    private static final String TAG = "BottomSheetActivity";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
            ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.bottomsheet_layout,
                container, false);

        List<Class<? extends AudioDevice>> preferredDevices = new ArrayList<>();
        try{
            preferredDevices.add(AudioDevice.BluetoothHeadset.class);
        }catch (Exception e){
            Log.e(TAG, "showAudioDevices: Exception AudioHeadSet"+e.getMessage() );
        }
        try{
            preferredDevices.add(AudioDevice.WiredHeadset.class);
        }catch (Exception e){
            Log.e(TAG, "showAudioDevices: Exception AudioHeadSet"+e.getMessage() );
        }
        try{
            preferredDevices.add(AudioDevice.Speakerphone.class);
        }catch (Exception e){
            Log.e(TAG, "showAudioDevices: Exception AudioHeadSet"+e.getMessage() );
        }
        try{
            preferredDevices.add(AudioDevice.Earpiece.class);
        }catch (Exception e){
            Log.e(TAG, "showAudioDevices: Exception AudioHeadSet"+e.getMessage() );
        }

        AudioSwitch audioSwitch = new AudioSwitch(getActivity().getApplicationContext(), false, focusChange -> {}, preferredDevices);
        Button bluetooth_button = v.findViewById(R.id.bluetooth_id);
        Button speaker_button = v.findViewById(R.id.speaker_id);
        Button phone_button = v.findViewById(R.id.phone_earpic);
       audioManager = (AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE);
        bluetooth_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
//                AudioManager audioManager = null;
//                audioManager = (AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE);

                Log.d(TAG, "onClick() called with: Bluttoth");

                    audioManager.setBluetoothScoOn(true);
                AudioDevice selectedAudioDevice ;
//                audioSwitch.selectDevice(selectedAudioDevice);

            }
        });

        speaker_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick() called with: Specker");
                audioManager.setSpeakerphoneOn(true);
            }
        });
        phone_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                Log.d(TAG, "onClick() called with: Earpic");
                audioManager.setSpeakerphoneOn(false);
            }
        });
        return v;
    }
}
