package com.gatistavam.voice;

import android.content.Context;
import android.util.Log;

import com.twilio.audioswitch.AudioSwitch;

import kotlin.Unit;

//class AudioSwitchMang {
//
//    private static final String TAG = "audioSwitchMang";
//
//    public static AudioSwitch getAudioSwitch() {
//        return audioSwitch;
//    }
//
////    public void setAudioSwitch(AudioSwitch audioSwitch) {
////        this.audioSwitch = audioSwitch;
////    }
//
//    static AudioSwitch audioSwitch;
//
//    public  AudioSwitchMang(Context c ) {
//        audioSwitch = new AudioSwitch(c);
//    }
//
//
//
//    void  startAudioSwitch() {
//        audioSwitch.start((audioDevices, audioDevice) -> {
//            Log.d(TAG, "Updating AudioDeviceIcon");
////            updateAudioDeviceIcon(audioDevice);
//            return Unit.INSTANCE;
//        });
//    }
//
//
//}
//package com.journaldev.singleton;

public class AudioSwitchMang {
    private static final String TAG = "audioSwitchMang";
    private static final AudioSwitchMang instance = new AudioSwitchMang();
    static AudioSwitch audioSwitch;
//    //private constructor to avoid client applications to use constructor
//    AudioSwitchMang(){}

    public static AudioSwitchMang getInstance( Context c ){
        audioSwitch = new AudioSwitch(c);
        return instance;
    }

    void  startAudioSwitch() {
        audioSwitch.start((audioDevices, audioDevice) -> {
            Log.d(TAG, "Updating AudioDeviceIcon");
//            updateAudioDeviceIcon(audioDevice);
            return Unit.INSTANCE;
        });
    }

    public static AudioSwitch getAudioSwitch() {
        return audioSwitch;
    }
}
