package com.gatistavam.voice;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.twilio.audioswitch.AudioSwitch;
import com.twilio.voice.CallInvite;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;


public class AnswerJavaActivity extends AppCompatActivity {

    private static String TAG = "AnswerActivity";
    public static final String TwilioPreferences = "com.twilio.twilio_voicePreferences";
//    public static final String TwilioPreferences = "mx.TwilioPreferences";
    //    AudioSwitchMang audioSwitchMang;
    private CallInvite activeCallInvite;
    private int activeCallNotificationId;
    private static final int RECORD_AUDIOT_CODE = 21;
    private static final int BLUETOOTH_code = 25;
    private static final int MIC_PERMISSION_REQUEST_CODE = 254;
    private static final int BLUETOOTH_CONNECT_CODE = 12;
    private PowerManager.WakeLock wakeLock;
    private TextView tvUserName;
    private TextView tvUserNameCallerDetails;
    private TextView tvCallStatus;
    private ImageView btnAnswer;
    private ImageView btnReject;
    AudioSwitch audioSwitch;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        requestQueue = Volley.newRequestQueue(AnswerJavaActivity.this);
        callCustomerInfo();
        audioSwitch = new AudioSwitch(getApplicationContext());
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvUserNameCallerDetails = (TextView) findViewById(R.id.tvUserDetails);
        tvCallStatus = (TextView) findViewById(R.id.tvCallStatus);
        btnAnswer = (ImageView) findViewById(R.id.btnAnswer);
        btnReject = (ImageView) findViewById(R.id.btnReject);



//        getBluetooth(getApplicationContext());
        if (getBluetooth(getApplicationContext())) {
            audioSwitch.start((audioDevices, audioDevice) -> {
                Log.d(TAG, "Updating AudioDeviceIcon");
//            updateAudioDeviceIcon(audioDevice);
                return Unit.INSTANCE;
            });
        }
        KeyguardManager kgm = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        audioSwitch.start((audioDevices, audioDevice) -> {
//            Log.d(TAG, "Updating AudioDeviceIcon");
////            updateAudioDeviceIcon(audioDevice);
//            return Unit.INSTANCE;
//        });
        boolean isKeyguardUp = kgm.inKeyguardRestrictedInputMode();
//        audioSwitchMang= new AudioSwitchMang();
//        audioSwitchMang = AudioSwitchMang.getInstance(getApplicationContext());
        Log.d(TAG, "isKeyguardUp $isKeyguardUp");
        if (isKeyguardUp) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setTurnScreenOn(true);
                setShowWhenLocked(true);
                kgm.requestDismissKeyguard(this, null);
            } else {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
                wakeLock.acquire(60 * 1000L /*10 minutes*/);

                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                );
            }

        }

        handleIncomingCallIntent(getIntent());
    }


    void callCustomerInfo() {
        StringRequest strRequest = new StringRequest(Request.Method.POST, Constants.CUSTOMERINFOAPI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("Delete_res---", response);
                        try {
                            JSONObject dict = new JSONObject(response);
                            String msg = dict.getString("DATA");


                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                SharedPreferences preferences = getApplicationContext().getSharedPreferences(TwilioPreferences, Context.MODE_PRIVATE);
                String callerUserLogId = preferences.getString("callerUserLogId", "");
                String callerToken = preferences.getString("callerToken", "");
                String fromId = activeCallInvite.getFrom().replace("client:", "");
                Log.d(TAG, "Updating callerUserLogId"+callerUserLogId);
                Log.d(TAG, "Updating callerToken"+callerToken);
                Log.d(TAG, "Updating fromId"+fromId);
                params.put("Token", callerToken);
                params.put("UserIDLog", callerUserLogId);
                params.put("Phone", fromId);

                return params;
            }
        };

        requestQueue.add(strRequest);
    }
    @Override
    protected void onResume() {
        super.onResume();
        audioSwitch.start((audioDevices, audioDevice) -> {
            Log.d(TAG, "Updating AudioDeviceIcon");
//            updateAudioDeviceIcon(audioDevice);
            return Unit.INSTANCE;
        });
    }



    private void handleIncomingCallIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            Log.d(TAG, "handleIncomingCallIntent-");
            String action = intent.getAction();
            activeCallInvite = intent.getParcelableExtra(Constants.INCOMING_CALL_INVITE);
            activeCallNotificationId = intent.getIntExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, 0);
            tvCallStatus.setText(R.string.incoming_call_title);
            Log.d(TAG, action);
            switch (action) {
                case Constants.ACTION_INCOMING_CALL:
                case Constants.ACTION_INCOMING_CALL_NOTIFICATION:
                    configCallUI();
                    break;
                case Constants.ACTION_CANCEL_CALL:
                    newCancelCallClickListener();
                    break;
                case Constants.ACTION_ACCEPT:
                    checkPermissionsAndAccept();
                    break;
//                case Constants.ACTION_REJECT:
//                    newCancelCallClickListener();
//                    break;
                default: {
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getAction() != null) {
            Log.d(TAG, "onNewIntent-");
            Log.d(TAG, intent.getAction());
            switch (intent.getAction()) {
                case Constants.ACTION_CANCEL_CALL:
                    newCancelCallClickListener();
                    break;
                default: {
                }
            }
        }
    }


    private void configCallUI() {
        Log.d(TAG, "configCallUI");
        if (activeCallInvite != null) {

            String fromId = activeCallInvite.getFrom().replace("client:", "");
            Log.d(TAG, "configCallUI fromId = " + fromId);
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(TwilioPreferences, Context.MODE_PRIVATE);
            String caller = preferences.getString("defaultCaller1", "Unkown");
            String callerDetails = preferences.getString("callerDetails", "No Fpkkdkdnnnjknjk");
            //SharedPreferences.Editor edit = pSharedPref.edit();
            //                edit.putString("defaultCaller", caller);
            //                edit.apply();
            tvUserName.setText(fromId);
            tvUserNameCallerDetails.setText(callerDetails);

            btnAnswer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    checkPermissionsAndAccept();
                }
            });

            btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rejectCallClickListener();
                }
            });
        }
    }

    private void checkPermissionsAndAccept() {
        Log.d(TAG, "Clicked accept");

        if (!checkPermissionForMicrophone()) {
            Log.d(TAG, "configCallUI-requestAudioPermissions");
//            requestAudioPermissions();
        } else {
            Log.d(TAG, "configCallUI-newAnswerCallClickListener");

            acceptCall();
        }
    }


    private void acceptCall() {
        Log.d(TAG, "Accepting call");


        audioSwitch.activate();
        Intent acceptIntent = new Intent(this, IncomingCallNotificationService.class);
        acceptIntent.setAction(Constants.ACTION_ACCEPT);
        acceptIntent.putExtra(Constants.INCOMING_CALL_INVITE, activeCallInvite);
        acceptIntent.putExtra(Constants.ACCEPT_CALL_ORIGIN, 1);
        acceptIntent.putExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, activeCallNotificationId);
        Log.d(TAG, "Clicked accept startService");
        startService(acceptIntent);
        finish();
    }

    private void newCancelCallClickListener() {
        finish();
    }

    private void rejectCallClickListener() {
        Log.d(TAG, "Reject Call Click listener");
        audioSwitch.deactivate();
        if (activeCallInvite != null) {
            Intent rejectIntent = new Intent(this, IncomingCallNotificationService.class);
            rejectIntent.setAction(Constants.ACTION_REJECT);
            rejectIntent.putExtra(Constants.INCOMING_CALL_INVITE, activeCallInvite);
            startService(rejectIntent);
            finish();
        }
    }

    private Boolean checkPermissionForMicrophone() {
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return resultMic == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermissions() {
        String[] permissions = {Manifest.permission.RECORD_AUDIO};
        Log.d(TAG, "requestAudioPermissions");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(this, permissions, RECORD_AUDIOT_CODE);
            } else {
                ActivityCompat.requestPermissions(this, permissions, RECORD_AUDIOT_CODE);
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "requestAudioPermissions-> permission granted->newAnswerCallClickListener");
//            acceptCall();
        }
    }

    void getBluetoothConnect(Context c) {
        Log.d(TAG, "getBluetoothConnect() called with: c = [" + c + "]");
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(AnswerJavaActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_CODE);

        } else {
            Toast.makeText(c, "BLUETOOTH_CONNECT permissions needed. Please allow in your application settings", Toast.LENGTH_SHORT).show();
        }
    }

    boolean getBluetooth(Context c) {
        Log.d(TAG, "getBluetooth() called with: c = [" + c + "]");
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(c, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(c, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "getBluetooth() called with: c = [" + "in if call" + "]");
            String[] v = {Manifest.permission.RECORD_AUDIO, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH};
            ActivityCompat.requestPermissions(AnswerJavaActivity.this, v, BLUETOOTH_code);
            Log.d(TAG, "getBluetooth() called with: c = [" + "in if call" + "]");
            return true;
        } else {
            Toast.makeText(c, "BLUETOOTH permissions needed. Please allow in your application settings", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    void getRecodeAudio(Context c) {
        Log.d(TAG, "getRecodeAudio() called with: c = [" + c + "]");
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(AnswerJavaActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIOT_CODE);
        } else {
            Toast.makeText(c, "RECORD_AUDIO permissions needed. Please allow in your application settings", Toast.LENGTH_SHORT).show();
        }
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean v = false;
        switch (requestCode) {
            case BLUETOOTH_code:
//                if(grantResults.length == 0 || grantResults[0] != PERMISSION_GRANTED)
//                ActivityCompat.requestPermissions(this, permissions, BLUETOOTH_code);
                v = true;
                break;
            default:
                Log.d(TAG, "onRequestPermissionsResult() called with: requestCode = [" + requestCode + "], permissions = [" + permissions + "], grantResults = [" + grantResults + "]");
        }

        if (v) {
            audioSwitch.start((audioDevices, audioDevice) -> {
                Log.d(TAG, "Updating AudioDeviceIcon");
//            updateAudioDeviceIcon(audioDevice);
                return Unit.INSTANCE;
            });
        } else {
            rejectCallClickListener();
        }
//        if (requestCode == BLUETOOTH_code) {
//            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Microphone permissions needed. Please allow in your application settings.", Toast.LENGTH_LONG).show();
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_code);
//                rejectCallClickListener();
//            } else {
////                acceptCall();
//
//            }
//        } else {
//            throw new IllegalStateException("Unexpected value: " + requestCode);
//        }
    }

    @Override
    protected void onDestroy() {
        audioSwitch.stop();
        if (wakeLock != null) {
            wakeLock.release();
        }
        super.onDestroy();
    }

}
