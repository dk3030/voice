package com.gatistavam.voice;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.twilio.audioswitch.AudioDevice;
import com.twilio.audioswitch.AudioSwitch;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class BackgroundCallJavaActivity extends AppCompatActivity{

    private static String TAG = "BackgroundCallActivity";
    public static final String TwilioPreferences = "com.twilio.twilio_voicePreferences";
//    public static final String TwilioPreferences = "mx.TwilioPreferences";


//    private Call activeCall;
    private NotificationManager notificationManager;

    private PowerManager.WakeLock wakeLock;

    private TextView tvUserName;
    private TextView tvCallStatus;
    private ImageView btnMute;
    private ImageView btnOutput;
    private ImageView btnHangUp;
    public AudioSwitch audioSwitch;
//    AudioSwitchMang audioSwitchMang;
//    private AudioSwitch audioSwitch;

    @Override
    protected void onResume() {
        super.onResume();
        startAudioSwitch();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_call);
//        audioSwitchMang= new AudioSwitchMang();
//        audioSwitchMang = AudioSwitchMang.getInstance(getApplicationContext());
        audioSwitch = new AudioSwitch(getApplicationContext());
        tvUserName = (TextView) findViewById(R.id.tvUserName) ;
        tvCallStatus = (TextView) findViewById(R.id.tvCallStatus) ;
        btnMute = (ImageView) findViewById(R.id.btnMute);
        btnOutput = (ImageView) findViewById(R.id.btnOutput);
        btnHangUp = (ImageView) findViewById(R.id.btnHangUp);

        KeyguardManager kgm = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        Boolean isKeyguardUp = kgm.inKeyguardRestrictedInputMode();

        Log.d(TAG, "isKeyguardUp $isKeyguardUp");
        if (isKeyguardUp) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                Log.d(TAG, "ohh shiny phone!");
                setTurnScreenOn(true);
                setShowWhenLocked(true);
                kgm.requestDismissKeyguard(this, null);

            }else{
                Log.d(TAG, "diego's old phone!");
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
                wakeLock.acquire();

                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                );
            }
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        handleCallIntent(getIntent());
    }

    private void handleCallIntent(Intent intent){
        if (intent != null){

            String fromId = intent.getStringExtra(Constants.CALL_FROM).replace("client:","");
            if(fromId != null){

                SharedPreferences preferences = getApplicationContext().getSharedPreferences(TwilioPreferences, Context.MODE_PRIVATE);
                String caller = preferences.getString(fromId, preferences.getString("defaultCaller", "Desconocido"));
                Log.d(TAG,"handleCallIntent");
                Log.d(TAG,"caller from");
                Log.d(TAG,caller);

//                tvUserName.setText(caller);
                tvUserName.setText(fromId);
                tvCallStatus.setText("Conectado");
                Log.d(TAG, "handleCallIntent-");
                configCallUI();
            }
        }
    }

    public void startAudioSwitch() {
        audioSwitch.start((audioDevices, audioDevice) -> {
            Log.d(TAG, "Updating AudioDeviceIcon");
            updateAudioDeviceIcon(audioDevice);
            return Unit.INSTANCE;
        });
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getAction() != null){
        Log.d(TAG, "onNewIntent-");
        Log.d(TAG, intent.getAction());
            switch (intent.getAction()){
                case Constants.ACTION_CANCEL_CALL:
                    callCanceled();
                    break;
                default: {
                }
            }
        }
    }


    boolean isMuted = false;
    private void configCallUI() {
        Log.d(TAG, "configCallUI");

            btnMute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onCLick");
                    sendIntent(Constants.ACTION_TOGGLE_MUTE);
                    isMuted = !isMuted;
                    applyFabState(btnMute, isMuted);
                }
            });

            btnHangUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendIntent(Constants.ACTION_END_CALL);
                    finish();
                }
            });
            btnOutput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick() called with: show BottomSheet");
                    showAudioDevices();
//                    Toast toast = Toast.makeText(getApplicationContext(), "call button clicked", Toast.LENGTH_LONG);
//                    toast.show();
//                    BottomSheetActivity bottomSheet = new BottomSheetActivity();
//                    bottomSheet.show(getSupportFragmentManager(),
//                            "ModalBottomSheet");
                   /* AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    boolean isOnSpeaker = !audioManager.isSpeakerphoneOn();
                    audioManager.setSpeakerphoneOn(isOnSpeaker);
                    applyFabState(btnOutput, isOnSpeaker);*/
                }
            });

    }
//    private void startAudioSwitch() {
//        audioSwitchMang.audioSwitch.start((audioDevices, audioDevice) -> {
//            Log.d(TAG, "Updating AudioDeviceIcon");
//            updateAudioDeviceIcon(audioDevice);
//            return Unit.INSTANCE;
//        });
//    }
    private void showAudioDevices() {
        AudioDevice selectedDevice = audioSwitch.getSelectedAudioDevice();
        Log.d(TAG, "selectedDevice List Got Nullllll : "+selectedDevice);
        List<AudioDevice> availableAudioDevices = audioSwitch.getAvailableAudioDevices();

        if (selectedDevice != null) {
            int selectedDeviceIndex = availableAudioDevices.indexOf(selectedDevice);
            Log.d(TAG, "selectedDevice List Got Nullllll : "+selectedDeviceIndex);
            ArrayList<String> audioDeviceNames = new ArrayList<>();
            for (AudioDevice a : availableAudioDevices) {
                audioDeviceNames.add(a.getName());
            }
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            new AlertDialog.Builder(this)
                    .setTitle("Select Audio Device")
                    .setSingleChoiceItems(
                            audioDeviceNames.toArray(new CharSequence[0]),
                            selectedDeviceIndex,
                            (dialog, index) -> {
                                dialog.dismiss();
                                AudioDevice selectedAudioDevice = availableAudioDevices.get(index);
                                updateAudioDeviceIcon(selectedAudioDevice);
                                audioSwitch.selectDevice(selectedAudioDevice);

                            }).create().show();
        }
        /*List<Class<? extends AudioDevice>> preferredDevices = new ArrayList<>();
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



        AudioSwitch audioSwitch = new AudioSwitch(getApplicationContext(), false, focusChange -> {}, preferredDevices);
        AudioDevice selectedDevice = audioSwitch.getSelectedAudioDevice();
        List<AudioDevice> availableAudioDevices = audioSwitch.getAvailableAudioDevices();
        Log.d(TAG, "selectedDevice: "+selectedDevice);
        Log.d(TAG, "selectedDevice List : "+availableAudioDevices);

        try {
            if (selectedDevice != null) {
                int selectedDeviceIndex = availableAudioDevices.indexOf(selectedDevice);

                ArrayList<String> audioDeviceNames = new ArrayList<>();
                for (AudioDevice a : availableAudioDevices) {
                    audioDeviceNames.add(a.getName());
                }

                new AlertDialog.Builder(this)
                        .setTitle("R.string.select_device")
                        .setSingleChoiceItems(
                                audioDeviceNames.toArray(new CharSequence[0]),
                                selectedDeviceIndex,
                                (dialog, index) -> {
                                    dialog.dismiss();
                                    AudioDevice selectedAudioDevice = availableAudioDevices.get(index);
                                    updateAudioDeviceIcon(selectedAudioDevice);
                                    audioSwitch.selectDevice(selectedAudioDevice);
                                }).create().show();
            }
            else{
                Log.d(TAG, "selectedDevice List Got Nullllll : ");
            }
        }catch (Exception e){
            Log.d(TAG, "selectedDevice List : "+e.getMessage().toString()+"\n"+e.toString());
        }*/

    }
   /* @Override
    public void onBackPressed() {
//        super.onBackPressed();

        AlertDialog.Builder builder = new AlertDialog.Builder(BackgroundCallJavaActivity.this);
        builder.setTitle("Call");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Do You Want To End Call?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

//                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }*/

    private void updateAudioDeviceIcon(AudioDevice selectedAudioDevice) {
        int audioDeviceMenuIcon = R.drawable.ic_phone_call;

        if (selectedAudioDevice instanceof AudioDevice.BluetoothHeadset) {
            audioDeviceMenuIcon = R.drawable.ic_bluetooth_white_24dp;
        } else if (selectedAudioDevice instanceof AudioDevice.WiredHeadset) {
            audioDeviceMenuIcon = R.drawable.ic_headset_mic_white_24dp;
        } else if (selectedAudioDevice instanceof AudioDevice.Earpiece) {
            audioDeviceMenuIcon = R.drawable.ic_phone_call;
        } else if (selectedAudioDevice instanceof AudioDevice.Speakerphone) {
            audioDeviceMenuIcon = R.drawable.ic_volume_2;
        }
        if (btnOutput != null) {
            btnOutput.setImageResource(audioDeviceMenuIcon);
        }
    }

    private void applyFabState(ImageView button, Boolean enabled) {
        // Set fab as pressed when call is on hold
        ColorStateList colorStateList;
        if(enabled){
            colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_55));
        }else{
            colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setBackgroundTintList(colorStateList);
        }
    }

    private void sendIntent(String action){
        Log.d(TAG,"Sending intent");
        Log.d(TAG,action);
        Intent activeCallIntent = new Intent();
        activeCallIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activeCallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activeCallIntent.setAction(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(activeCallIntent);
    }


    private void callCanceled(){
        finish();
    }



    private Boolean isAppVisible(){
        return ProcessLifecycleOwner
                .get()
                .getLifecycle()
                .getCurrentState()
                .isAtLeast(Lifecycle.State.STARTED);
    }

    @Override
    protected void onDestroy() {
        audioSwitch.stop();
        super.onDestroy();
        if (wakeLock != null){
            wakeLock.release();
        }
    }

}