package com.sscctv.seeeyesonvif.__Not_Used;

import android.util.Log;

import com.sscctv.seeeyesonvif.Fragment.FragmentCallView;

import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;

class MyPlayerListener implements MediaPlayer.EventListener {

    private static String TAG = "PlayerListener";
    private WeakReference<FragmentCallView> mOwner;


    public MyPlayerListener(FragmentCallView owner) {
        mOwner = new WeakReference<FragmentCallView>(owner);
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        FragmentCallView player = mOwner.get();

        switch(event.type) {
            case MediaPlayer.Event.EndReached:
                Log.d(TAG, "MediaPlayerEndReached");
                player.releasePlayer();
                break;
            case MediaPlayer.Event.Playing:
            case MediaPlayer.Event.Paused:
            case MediaPlayer.Event.Stopped:
            default:
                break;
        }
    }
}
