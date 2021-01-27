package com.sscctv.seeeyesonvif.__Not_Used.Rtsp.Audio;

import android.util.Log;

import com.sscctv.seeeyesonvif.__Not_Used.Rtsp.Stream.RtpStream;


/**
 *
 */
public abstract class AudioStream extends RtpStream {
	private static final String TAG = AudioStream.class.getSimpleName();

	protected void recombinePacket(StreamPacks streamPacks) {
		Log.d(TAG, "AudioStream - recombinePacket");
	}
}
