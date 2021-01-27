package com.sscctv.seeeyesonvif.__Not_Used.Rtsp;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.__Not_Used.Rtsp.Socket.RtpSocket;
import com.sscctv.seeeyesonvif.__Not_Used.Rtsp.Video.H264Stream;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 *
 */
@SuppressWarnings({"AccessStaticViaInstance", "SameParameterValue"})
public class RtspClient {

    private final static String TAG = "RtspClient";
    private final static String UserAgent = "SeeEyes";
    private final static int STATE_STARTED = 0x00;
    private final static int STATE_STARTING = 0x01;
    private final static int STATE_STOPPING = 0x02;
    private final static int STATE_STOPPED = 0x03;
    public final static String METHOD_UDP = "udp";
    public final static String METHOD_TCP = "tcp";
    // Unused as audio tracks are not implemented
    // private final static int TRACK_VIDEO = 0x01;
    // private final static int TRACK_AUDIO = 0x02;

    private static class Parameters {
        String userName;
        String passWord;
        String host;
        String address;
        String allAddress;
        int port;
        int rtpPort;
        int serverPort;
    }

    public static class SDPInfo {
        boolean audioTrackFlag;
        boolean videoTrackFlag;
        String videoTrack;
        String audioTrack;
        public String SPS;
        public String PPS;
        int packetizationMode;
    }

    private Socket mSocket;
    private BufferedReader mBufferreader;
    private static OutputStream mOutputStream;
    private static Parameters mParams;
    private Handler mHandler;
    private static int CSeq;
    private int mState;
    private String mSession;
    private RtpSocket mRtpSocket;
    private boolean isTCPtranslate;
    private static boolean Describeflag = false; //used to get SDP info
    private static SDPInfo sdpInfo;
    private final String authorName;
    private final String authorPassword;
    private String authorBase64;
    private HandlerThread thread;

    private H264Stream mH264Stream;

    private SurfaceView mSurfaceView;

    private onStreamStartedListener onStreamStarted;

    private TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    protected SurfaceHolder.Callback mSurfaceHolderCallback = null;
    protected boolean mSurfaceReady = false;

    public interface onStreamStartedListener {
        void onStreamStarted(SDPInfo sdpInfo);
    }


    // ALLOWS YOU TO SET LISTENER && INVOKE THE OVERIDING METHOD
    // FROM WITHIN ACTIVITY
    public void setOnStreamStarted(onStreamStartedListener listener) {
        onStreamStarted = listener;
    }


    public RtspClient(String address, String name, String password, int port) {
        this("tcp", address, name, password);
    }

    public RtspClient(String address) {
        this("udp", address, null, null);
    }

    public RtspClient(String method, String address) {
        this(method, address, null, null);
    }

//	public RtspClient(String method, String address, int port) {
//		this(method,address,null,null,port);
//	}

//	public RtspClient(String address, int port) {
//		this("udp",address,null,null,port);
//	}

    private RtspClient(String method, String address, String name, String password) {
        String url = address.substring(address.indexOf("//") + 2);
        url = url.substring(0, url.indexOf("/"));
        String[] tmp = url.split(":");
        Log.d(TAG, url);
        authorName = name;
        authorPassword = password;
        if (tmp.length == 1) ClientConfig(tmp[0], address, 554);
        else if (tmp.length == 2) ClientConfig(tmp[0], address, Integer.parseInt(tmp[1]));
        if (method.equalsIgnoreCase(METHOD_UDP)) {
            isTCPtranslate = false;
        } else if (method.equalsIgnoreCase(METHOD_TCP)) {
            isTCPtranslate = true;
        }
    }

//	private RtspClient(String method, String address, String name, String password, int port) {
//		String url = address.substring(address.indexOf("//") + 2);
//		url = url.substring(0,url.indexOf("/"));
//		authorName = name;
//		authorPassword = password;
//		ClientConfig(url, address, port);
//		if( method.equalsIgnoreCase(METHOD_UDP) ) {
//			isTCPtranslate = false;
//		} else if( method.equalsIgnoreCase(METHOD_TCP)) {
//			isTCPtranslate = true;
//		}
//	}

//    public void setSurfaceView(SurfaceView s) {
//        mSurfaceView = s;
//    }

    public synchronized void setSurfaceView(SurfaceView view) {
        mSurfaceView = view;
    }

    private void ClientConfig(String host, String address, int port) {
        mParams = new Parameters();
        sdpInfo = new SDPInfo();
        mParams.userName = authorName;
        mParams.passWord = authorPassword;
        mParams.host = host;
        mParams.port = port;
        mParams.address = address.substring(7);
        mParams.allAddress = address;
        CSeq = 0;
        mState = STATE_STOPPED;
        mSession = null;
        if (authorName == null && authorPassword == null) {
            authorBase64 = null;
        } else {
            authorBase64 = Base64.encodeToString(("root" + ":" + "sscctv3500!").getBytes(), Base64.DEFAULT);
        }

        final Semaphore signal = new Semaphore(0);
        thread = new HandlerThread("RTSPCilentThread") {
            protected void onLooperPrepared() {
                mHandler = new Handler();
                signal.release();
            }
        };
        thread.start();
        signal.acquireUninterruptibly();
    }

    public void start() {
        mHandler.post(startConnection);
    }

    private final Runnable startConnection = new Runnable() {
        @Override
        public void run() {
            if (mState != STATE_STOPPED) return;
            mState = STATE_STARTING;

            Log.d(TAG, "Start to connect the server...");

            try {
                tryConnection();
                mHandler.post(sendGetParameter);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                abort();
            }
            onStreamStarted.onStreamStarted(sdpInfo);
        }
    };

    private final Runnable sendGetParameter = new Runnable() {
        @Override
        public void run() {
            try {
                sendRequestGetParameter();
                mHandler.postDelayed(sendGetParameter, 55000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public void abort() {
        try {
            if (mState == STATE_STARTED) sendRequestTeardown();
        } catch (IOException ignored) {
        }
    }

    public void shutdown() {
        if (mState == STATE_STARTED ||
                mState == STATE_STARTING) {
            mHandler.removeCallbacks(startConnection);
            mHandler.removeCallbacks(sendGetParameter);
            try {
                if (mH264Stream != null) {
//                    mH264Stream.stop();
                    mH264Stream = null;
                }
                if (mRtpSocket != null) {
                    mRtpSocket.stop();
                    mRtpSocket = null;
                }
                if (mState == STATE_STARTED) sendRequestTeardown();
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }
                mState = STATE_STOPPED;
                thread.quit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isStarted() {
        return mState == STATE_STARTED | mState == STATE_STARTING;
    }

    private void tryConnection() throws IOException {
        mSocket = new Socket(mParams.host, mParams.port);
        mBufferreader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        mOutputStream = mSocket.getOutputStream();
        mState = STATE_STARTING;
        sendRequestOptions();
//        sendDigestRequestOptions(tinyDB.getString(KeyList.HASH_KEY));
//        sendRequestDescribe(tinyDB.getString(KeyList.HASH_KEY));
//        sendRequestSetup(tinyDB.getString(KeyList.HASH_KEY));
//        sendRequestPlay(tinyDB.getString(KeyList.HASH_KEY));
        onStreamStarted.onStreamStarted(sdpInfo);
    }

    private void sendRequestOptions() throws IOException {
        String request = "OPTIONS rtsp://" + mParams.address + " RTSP/1.0\r\n" + addHeaders();
        Log.d(TAG, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes(UTF_8));
        Response.parseResponse(mBufferreader);
    }

    public void sendDigestRequestOptions(String str) throws IOException {
        String request = "OPTIONS rtsp://" + mParams.address + " RTSP/1.0\r\n" + addDigestHeaders(str);
        Log.d(TAG, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes(UTF_8));
        Response.parseResponse(mBufferreader);
    }

    private void sendRequestDescribe(String str) throws IOException {
        String request = "DESCRIBE rtsp://" + mParams.address + " RTSP/1.0\r\n" + addDigestHeaders(str);
        Log.d(TAG, request.substring(0, request.indexOf("\r\n")));
        Describeflag = true;
        mOutputStream.write(request.getBytes(UTF_8));
        Response.parseResponse(mBufferreader);
    }

    private void sendRequestSetup(String str) throws IOException {
        Matcher matcher;
        String request;
        if (isTCPtranslate) {
            request = "SETUP " +sdpInfo.videoTrack + " RTSP/1.0\r\n"
//            request = "SETUP rtsp://" + mParams.address + "/" + sdpInfo.videoTrack + " RTSP/1.0\r\n"
                    + "Transport: RTP/AVP/TCP;unicast;client_port=55640-55641" + "\r\n"
                    + addDigestHeaders(str);
        } else {
            request = "SETUP " +sdpInfo.videoTrack + " RTSP/1.0\r\n"
                    + "Transport: RTP/AVP/UDP;unicast;client_port=55640-55641" + "\r\n"
                    + addDigestHeaders(str);
    }
        Log.d(TAG, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes(UTF_8));
        Response mResponse = Response.parseResponse(mBufferreader);

        //there has two different session type, one is without timeout , another is with timeout
        matcher = Response.regexSessionWithTimeout.matcher(Objects.requireNonNull(mResponse.headers.get("session")));
        if (matcher.find()) mSession = matcher.group(1);
        else mSession = mResponse.headers.get("session");
        Log.d(TAG, "the session is " + mSession);

        //get the port information and start the RTP socket, ready to receive data
        if (isTCPtranslate)
            matcher = Response.regexTCPTransport.matcher(Objects.requireNonNull(mResponse.headers.get("transport")));
        else
            matcher = Response.regexUDPTransport.matcher(Objects.requireNonNull(mResponse.headers.get("transport")));
        if (matcher.find()) {
            Log.d(TAG, "The client port is:" + matcher.group(1) + " ,the server port is:" + (isTCPtranslate ? "null" : matcher.group(2)) + "...");
            mParams.rtpPort = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
            if (!isTCPtranslate) mParams.serverPort = Integer.parseInt(Objects.requireNonNull(matcher.group(2)));

            //prepare for the video decoder
            mH264Stream = new H264Stream(sdpInfo);
            mH264Stream.setSurfaceView(mSurfaceView);

//			if(isTCPtranslate) mRtpSocket = new RtpSocket(isTCPtranslate, mParams.rtpPort, mParams.host, -1,TRACK_VIDEO);
//			else mRtpSocket = new RtpSocket(isTCPtranslate, mParams.rtpPort, mParams.host, mParams.serverPort,TRACK_VIDEO);
            if (isTCPtranslate)
                mRtpSocket = new RtpSocket(isTCPtranslate, mParams.rtpPort, mParams.host, -1);
            else
                mRtpSocket = new RtpSocket(isTCPtranslate, mParams.rtpPort, mParams.host, mParams.serverPort);
            mRtpSocket.startRtpSocket();
            mRtpSocket.setStream(mH264Stream);
        } else {
            if (isTCPtranslate) {
                Log.d(TAG, "Without get the transport port infom, use the rtsp tcp socket!");
                mParams.rtpPort = mParams.port;

                //prepare for the video decoder
                mH264Stream = new H264Stream(sdpInfo);
                mH264Stream.setSurfaceView(mSurfaceView);

//				mRtpSocket = new RtpSocket(isTCPtranslate,mParams.rtpPort,mParams.host,-2,TRACK_VIDEO);
                mRtpSocket = new RtpSocket(isTCPtranslate, mParams.rtpPort, mParams.host, -2);
                mRtpSocket.setRtspSocket(mSocket);
                mRtpSocket.startRtpSocket();
                mRtpSocket.setStream(mH264Stream);
                mState = STATE_STARTED;
                onStreamStarted.onStreamStarted(sdpInfo);
            }
        }
    }

    private void sendRequestPlay(String str) throws IOException {
        String request = "PLAY rtsp://" + mParams.address + " RTSP/1.0\r\n"
                + "Range: npt=0.000-\r\n"
                + addDigestHeaders(str);
        Log.d(TAG, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes("UTF-8"));
        Response.parseResponse(mBufferreader);
    }

    private void sendRequestTeardown() throws IOException {
        String request = "TEARDOWN " + sdpInfo.videoTrack + " RTSP/1.0\r\n" + addHeaders();
        Log.d(TAG, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes("UTF-8"));
        mState = STATE_STOPPING;
    }

    private void sendRequestGetParameter() throws IOException {
        String request = "GET_PARAMETER "+ sdpInfo.videoTrack + " RTSP/1.0\r\n" + addHeaders();
        Log.d(TAG, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes("UTF-8"));
    }

    public static void sendRequestSetParameter(String contentType) throws IOException {
        String request = "SET_PARAMETER rtsp://"
                + mParams.address
//				+ "/"
//				+ sdpInfo.videoTrack
                + " RTSP/1.0\r\n"
                + "CSeq: " + (++CSeq) + "\r\n"
                + "Content-length: strlen(Content-type)"
                + "Content-type: " + contentType + "\r\n";
        Log.d(TAG, request);
        mOutputStream.write(request.getBytes("UTF-8"));
    }

    private String addHeaders() {
        return "CSeq: " + (++CSeq) + "\r\n"
                + ((authorBase64 == null) ? "" : ("Authorization: Basic " + authorBase64 + "\r\n"))
                + "User-Agent: " + UserAgent + "\r\n"
                + ((mSession == null) ? "" : ("Session: " + mSession + "\r\n"))
                + "\r\n";
    }

    public static String md5(String str) {
        Log.d(TAG, "MD5: " + str);
        String MD5 = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes(UTF_8));
            byte[] byteData = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte byteDatum : byteData)
                sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
            MD5 = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            MD5 = null;
        }
        return MD5;
    }


//
    private String addDigestHeaders(String str) {
        return "CSeq: " + (++CSeq) + "\r\n"
                + str + "\r\n"
                + "User-Agent: " + UserAgent + "\r\n"
                + "\r\n";
    }


    private static class Response {

        static final Pattern regexStatus = Pattern.compile("RTSP/\\d.\\d (\\d+) .+", Pattern.CASE_INSENSITIVE);
        static final Pattern regexHeader = Pattern.compile("(\\S+): (.+)", Pattern.CASE_INSENSITIVE);
        static final Pattern regexUDPTransport = Pattern.compile("client_port=(\\d+)-\\d+;server_port=(\\d+)-\\d+", Pattern.CASE_INSENSITIVE);
        static final Pattern regexTCPTransport = Pattern.compile("client_port=(\\d+)-\\d+;", Pattern.CASE_INSENSITIVE);
        static final Pattern regexSessionWithTimeout = Pattern.compile("(\\S+);timeout=(\\d+)", Pattern.CASE_INSENSITIVE);
        static final Pattern regexSDPgetTrack1 = Pattern.compile("trackID=(\\d+)", Pattern.CASE_INSENSITIVE);
        static final Pattern regexSDPgetTrack2 = Pattern.compile("control:(\\S+)", Pattern.CASE_INSENSITIVE);
        static final Pattern regexSDPmediadescript = Pattern.compile("m=(\\S+) .+", Pattern.CASE_INSENSITIVE);
        static final Pattern regexSDPpacketizationMode = Pattern.compile("packetization-mode=(\\d);", Pattern.CASE_INSENSITIVE);
        static final Pattern regexSDPspspps = Pattern.compile("sprop-parameter-sets=(\\S+),(\\S+)", Pattern.CASE_INSENSITIVE);
        static final Pattern regexSDPlength = Pattern.compile("Content-length: (\\d+)", Pattern.CASE_INSENSITIVE);
        static final Pattern regexSDPstartFlag = Pattern.compile("v=(\\d)", Pattern.CASE_INSENSITIVE);

        int state;
        static final HashMap<String, String> headers = new HashMap<>();

        private static TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());

        static Response parseResponse(BufferedReader input) throws IOException {
            Response response = new Response();
            String line;
            Matcher matcher;
            int sdpContentLength = 0;
            if ((line = input.readLine()) == null) throw new IOException("Connection lost");
            matcher = regexStatus.matcher(line);
            if (matcher.find()) {
                response.state = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));

            } else {
                while ((line = input.readLine()) != null) {
                    matcher = regexStatus.matcher(line);
                    if (matcher.find()) {
                        response.state = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                        break;
                    }
                }
            }
            Log.d(TAG, "The response state is: " + response.state);

            int foundMediaType = 0;
            int sdpHaveReadLength = 0;
            boolean sdpStartFlag = false;

            while ((line = input.readLine()) != null) {
                if (line.length() > 3 || Describeflag) {
//                    Log.d(TAG, "The line is: " + line + "...");
                    if (response.state == 401) {
                        if (line.contains("WWW-Authenticate:")) {
                            String auth = line.substring(line.indexOf("WWW-Authenticate: ") + 18, line.indexOf(" realm"));
//                            Log.d(TAG, "Auth: " + auth);
                            String realm = line.substring(line.indexOf("realm=\"") + 7, line.indexOf("\", nonce="));
//                            Log.d(TAG, "realm: " + realm);
                            String nonce = line.substring(line.indexOf("nonce=\"") + 7, line.indexOf("nonce=\"")+39);
//                            Log.d(TAG, "nonce: " + nonce);


                            String userName = mParams.userName;
                            String passWord = mParams.passWord;
                            String hash1 = md5(userName + ":" + realm + ":" + passWord);
                            String hash2 = md5("OPTIONS:"+mParams.allAddress);
                            String hash3 = md5(hash1+":"+nonce+":"+hash2);
                            String result = "Authorization: Digest username=\"" + userName + "\", realm=\""+realm+"\", nonce=\""+nonce+"\", uri=\""
                                    +mParams.allAddress+"\", response=\""+hash3+"\"";
                            tinyDB.putString(KeyList.HASH_KEY, result);
                        }
                    }
                    matcher = regexHeader.matcher(line);
                    if (matcher.find())
                        headers.put(Objects.requireNonNull(matcher.group(1)).toLowerCase(Locale.US), matcher.group(2)); //$ to $

                    matcher = regexSDPlength.matcher(line);
                    if (matcher.find()) {
                        sdpContentLength = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                        sdpHaveReadLength = 0;
                    }
                    //Here is trying to get the SDP information from the describe response
                    if (Describeflag) {
                        matcher = regexSDPmediadescript.matcher(line);
                        if (matcher.find())
                            if (Objects.requireNonNull(matcher.group(1)).equalsIgnoreCase("audio")) {
                                foundMediaType = 1;
                                sdpInfo.audioTrackFlag = true;
                            } else if (Objects.requireNonNull(matcher.group(1)).equalsIgnoreCase("video")) {
                                foundMediaType = 2;
                                sdpInfo.videoTrackFlag = true;
                            }

                        matcher = regexSDPpacketizationMode.matcher(line);
                        if (matcher.find()) {
                            sdpInfo.packetizationMode = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                        }

                        matcher = regexSDPspspps.matcher(line);
                        if (matcher.find()) {
                            sdpInfo.SPS = matcher.group(1);
                            sdpInfo.PPS = matcher.group(2);
                        }

                        matcher = regexSDPgetTrack1.matcher(line);
                        if (matcher.find())
                            if (foundMediaType == 1)
                                sdpInfo.audioTrack = "trackID=" + matcher.group(1);
                            else if (foundMediaType == 2)
                                sdpInfo.videoTrack = "trackID=" + matcher.group(1);


                        matcher = regexSDPgetTrack2.matcher(line);
                        if (matcher.find())
                            if (foundMediaType == 1) sdpInfo.audioTrack = matcher.group(1);
                            else if (foundMediaType == 2) sdpInfo.videoTrack = matcher.group(1);


                        matcher = regexSDPstartFlag.matcher(line);
                        if (matcher.find()) sdpStartFlag = true;
                        if (sdpStartFlag) sdpHaveReadLength += line.getBytes().length + 2;
                        if ((sdpContentLength < sdpHaveReadLength + 2) && (sdpContentLength != 0)) {
                            Describeflag = false;
                            Log.d(TAG, "The SDP info: "
                                    + (sdpInfo.audioTrackFlag ? "have audio info.. " : "haven't the audio info.. ")
                                    + ";" + (sdpInfo.audioTrackFlag ? (" the audio track is " + sdpInfo.audioTrack) : ""));
                            Log.d(TAG, "The SDP info: "
                                    + (sdpInfo.videoTrackFlag ? "have video info.. " : "haven't the vedio info..")
                                    + (sdpInfo.videoTrackFlag ? (" the video track is " + sdpInfo.videoTrack) : "")
                                    + ";" + (sdpInfo.videoTrackFlag ? (" the video SPS is " + sdpInfo.SPS) : "")
                                    + ";" + (sdpInfo.videoTrackFlag ? (" the video PPS is " + sdpInfo.PPS) : "")
                                    + ";" + (sdpInfo.videoTrackFlag ? (" the video packetization mode is " + sdpInfo.packetizationMode) : ""));
                            break;
                        }
                    }
                } else {
                    break;
                }

            }

            if (line == null) throw new IOException("Connection lost");

            return response;
        }
    }
}