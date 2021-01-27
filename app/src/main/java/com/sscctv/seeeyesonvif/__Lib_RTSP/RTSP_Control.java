package com.sscctv.seeeyesonvif.__Lib_RTSP;

import android.content.Context;
import android.util.Log;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class RTSP_Control {

    private static final String TAG = RTSP_Control.class.getSimpleName();


    enum RTSPState {
        INIT, READY, PLAYING
    }

    // Request variables
    enum RTSPRequest {
        SETUP, DESCRIBE, PLAY, PAUSE, TEARDOWN, OPTIONS
    }


    // Current state of the client
    private RTSPState state = null;

    private String hostName = null;
    private int serverPort = 554;
    private String videoFile = null;

    // UDP packet received from server
    private DatagramPacket rcvdp = null;
    // Socket used to send and receive UDP packets
    private DatagramSocket RTPsocket = null;
    // Port where client will receive the RTP packets
    private int RTP_RCV_PORT = 9000;

    private Socket RTSPSocket = null;

    // Used to write RTSP messages and send to server
    private BufferedReader RTSPBufferedReader = null;

    // Used to receive RTSP messages and data
    private BufferedWriter RTSPBufferedWriter = null;

    // Buffer to receive the data
    byte[] Receive_buf = null;

    // Timer used to receive data from the UDP socket
    Timer timer = null;
    Timer timerOptions = null;

    // Initially set to zero
    private int RTSPSeqNb = 0;

    // Variables that define the periodic options request sent to the
    // RTSP server
    // In order to prevent a time out, it's good to periodically send
    // something to the server to let you know that you're still here
    // You can do this very innocuously with an OPTIONS request
    // As such, every 45 seconds we send an OPTIONS request to let
    // the server know we're still here
    // We will also start the timer at about 15 seconds after we schedule
    // the task to ensure no conflicts
    private final int TIMEROPTIONSDELAY = 15000;
    private final int TIMEROPTIONSFREQUENCY = 45000;

    // Flag to establish that we have set up our parameters
    private boolean isSetup = false;

    private String RTSPSessionID = null; // ID of RTSP sessions - given by RTSP server

    // Carriage return and new line to send to server
    private final String CRLF = "\r\n";

    private String Result_Respone = "";
    private String Full_URL = "";

    public List<ItemRtspTrack> trackList;
    private final Context context = ActivityMain.getAppContext();

    public RTSP_Control(String serverHost, int serverPort, String fileName) {

    }

    public RTSP_Control(String rtspURL) {
        setRTSPURL(rtspURL);

        trackList = new ArrayList<ItemRtspTrack>();
    }
    // This timer task gets invoked every so often to ensure that the connection
    // is still alive and doesn't time out

    public void setRTSPURL(String rtspURL) {
        String temp = "rtsp://";
        int locOfRtsp = rtspURL.indexOf(temp);

        Full_URL = rtspURL;

//        System.out.println(rtspURL); /* BY INGU */

        if (locOfRtsp == -1)
            throw new IllegalArgumentException("Must give URL that begins with rtsp://");

        // Obtain a string excluding the "rtsp://" bit
        String parsedURL = rtspURL.substring(locOfRtsp + temp.length());

        int indexOfSlash = parsedURL.indexOf("/");
        String hostnameTemp;
        if (indexOfSlash != -1)
            hostnameTemp = parsedURL.substring(0, indexOfSlash);
        else
            throw new IllegalArgumentException("RTSP URL must end with a slash (/)");

        int indexOfColon = hostnameTemp.indexOf(":");

        if (indexOfColon != -1) {
            String[] Str = hostnameTemp.split(":");
            this.hostName = Str[0];
            this.serverPort = Integer.parseInt(Str[1]);
        } else {
            this.hostName = hostnameTemp;
            this.serverPort = 554;
        }
//        System.out.println(this.hostName); /* BY INGU */
        // Get the video file name now
        // If none is provided, this is null
        if (indexOfSlash + 1 > parsedURL.length())
            videoFile = null;
        else
            videoFile = parsedURL.substring(indexOfSlash + 1);

        setUpConnectionAndParameters();
    }

    private void setUpConnectionAndParameters() {
        try {
            InetAddress ServerIPAddr = InetAddress.getByName(hostName);
            RTSPSocket = new Socket(ServerIPAddr, serverPort);
            RTSPSocket.setSoTimeout(500);
            RTSPSocket.setSoLinger(true, 3000);
        } catch (ConnectException e1) {
            Log.e(TAG, Objects.requireNonNull(e1.getLocalizedMessage()));
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            RTSPBufferedReader = new BufferedReader(new InputStreamReader(RTSPSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(RTSPSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        state = RTSPState.INIT;
        Receive_buf = new byte[300000];
        RTSPSessionID = null;
        isSetup = true;
        RTPsocket = null;
        imagenb = 0;

    }


    public String get_Full_RTSPURL() {
        return new String(Full_URL);
    }

    public String getRTSPURL() {
        return new String(hostName);
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public String getVideoFilename() {
        if (this.videoFile != null)
            return new String(videoFile);
        else
            return null;
    }

    public boolean sendRTSPBuffer(String Data) {
        try {
            if (RTSPSocket != null) {
                if (RTSPSocket.isConnected()) {
                    RTSPBufferedWriter.write(Data);
                    RTSPBufferedWriter.flush();
                } else {
                    return false;
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String sendDescribe() {

        if (RTSPSocket != null) {
            RTSPSeqNb = 0;
            this.RTSPSessionID = null;
            try {

                if (RTSPSocket.isConnected()) {
                    RTSPSocket.close();
                    RTSPSocket = null;
                }

                setUpConnectionAndParameters();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String requestType = new String("DESCRIBE");

            RTSPSeqNb++;

            return RTSP_Describe.Get_Describe(requestType, getRTSPURL(), getServerPort(), getVideoFilename(), RTSPSeqNb);
        } else {
            return null;
        }
    }


    public void parseDescribe(String Value) {
//        Log.d(TAG, "Parse Describe: " + Value);

        String[] Line = Value.split(CRLF);
        String Track = null;

        ItemRtspTrack rtsp_track = null;

        boolean is_audio = false;
        boolean is_video = false;
        trackList.clear();
        for (String s : Line) {
            if (s.contains("a=control")) {
                Track = s.substring(s.indexOf("a=control") + 10, s.length());
            }

            if (s.contains("m=audio")) {
                is_audio = true;
            }

            if (s.contains("m=video")) {
                is_video = true;
            }

            if (Track != null) {

                if (s.contains("a=sendonly")) {
                    rtsp_track = new ItemRtspTrack(Track, true);
                    trackList.add(rtsp_track);

                    rtsp_track.Set_Audio_Video(is_audio, is_video);

                    rtsp_track = null;
                    Track = null;
                    is_audio = false;
                    is_video = false;
                } else if (s.contains("a=recvonly")) {
                    rtsp_track = new ItemRtspTrack(Track, false);
                    trackList.add(rtsp_track);

                    rtsp_track.Set_Audio_Video(is_audio, is_video);

                    rtsp_track = null;
                    Track = null;
                    is_audio = false;
                    is_video = false;
                }

            }
        }

        if (Track != null) {
            rtsp_track = new ItemRtspTrack(Track, false);
            trackList.add(rtsp_track);
            rtsp_track.Set_Audio_Video(is_audio, is_video);
            Track = null;
            is_audio = false;
            is_video = false;
        }

//        for (int i = 0; i < trackList.size(); i++) {
//            Log.w(TAG, "---> Set Track: " + trackList.get(i).Track + " / " + trackList.get(i).Sendonly + " / " + trackList.get(i).Track_Num +
//                    " / " + trackList.get(i).is_audio + " / " + trackList.get(i).is_vedio); /* BY INGU */
//        }
    }

    public String sendSETUP(int index) {
        String requestType = new String("SETUP");
        ;
        RTSPSeqNb++;
        switch (index) {
            case 0:
                return RTSP_Setup.Get_Setup(requestType, getRTSPURL(), getServerPort(), getVideoFilename(), RTSPSeqNb,
                        trackList.get(index), RTSPSessionID, 4588);
            case 1:
                return RTSP_Setup.Get_Setup(requestType, getRTSPURL(), getServerPort(), getVideoFilename(), RTSPSeqNb,
                        trackList.get(index), RTSPSessionID, 4578);
            case 2:
                return RTSP_Setup.Get_Setup(requestType, getRTSPURL(), getServerPort(), getVideoFilename(), RTSPSeqNb,
                        trackList.get(index), RTSPSessionID, 6296);
            case 3:
                return RTSP_Setup.Get_Setup(requestType, getRTSPURL(), getServerPort(), getVideoFilename(), RTSPSeqNb,
                        trackList.get(index), RTSPSessionID, 7000);
        }

        return null;
    }

    public String sendTearDown(int index) {
        String requestType = "TEARDOWN";
        RTSPSeqNb++;
        return RTSP_TearDown.Get_TEAR(requestType, getRTSPURL(), getServerPort(), getVideoFilename(), RTSPSeqNb, RTSPSessionID);
    }


    public String receiveBuffer() {

        Result_Respone = null;
        // Wait for server response
        if (parseServerResponse() != 200) {
            Log.e(TAG, "Invalid Server Response");
        }

        return Result_Respone;
    }

    public void parseSetup(String Value, ItemRtspTrack Track) {
        String[] Line = Value.split(CRLF);
        for (String value : Line) {
            if (value.contains("Transport:")) {
                String[] Opt = value.split(";");
                for (String s : Opt) {
                    if (s.contains("client_port=")) {
                        String Client_Port = s.substring("client_port=".length(), s.length());
                        Track.setClientPort(Client_Port);
                    }

                    if (s.contains("server_port=")) {
                        String Server_Port = s.substring("server_port=".length(), s.length());
                        Track.setServerPort(Server_Port);
                    }
                }
            }

        }
    }

    public String sendPLAY(boolean Voice) {
        String requestType = "PLAY";
        if (Voice) {
            RTSPSeqNb++;
        }


        return RTSP_Play.Get_PLAY(requestType, getRTSPURL(), getServerPort(), getVideoFilename(), RTSPSeqNb, RTSPSessionID);
    }

    public void parsePLAY(String Value) {
        String[] Line = Value.split(CRLF);
        for (int i = 0; i < Line.length; i++) {
        }
    }

    public boolean Check_Track_Send() {
        for (int i = 0; i < trackList.size(); i++) {
            if (!trackList.get(i).Send) {
                return false;
            }
        }

        return true;
    }

    // Goes through the received string from the server
    // Also allows us to parse through and see if there is track
    // information and the type of transport this server supports
    public int parseServerResponse() {
        int replyCode = 0;

        try {
            String statusLine = RTSPBufferedReader.readLine();
//            Log.d(TAG, "---> Line: " + statusLine);

            if(statusLine == null) {
                Log.e(TAG, "null Read Line");
                return -1;
            }
            if (statusLine.isEmpty()) {
                Log.e(TAG, "Not Read Line");
                return -1;
            }

            StringTokenizer tokens = new StringTokenizer(statusLine, " \t\n\r\f");

            tokens.nextToken(); // Gives us RTSP/1.0
            String tempCheck = tokens.nextToken();

            if (isStringNumber(tempCheck)) {
                replyCode = Integer.parseInt(tempCheck);
                if (replyCode == 200 || replyCode == 454) { // begin if
                    String line;

                    try {
                        while (!RTSPBufferedReader.ready()) {
                            continue;
                        }
                    } catch (IOException e) {
                        Result_Respone = "Could not read from read buffer";
//                        System.out.println(Result_Respone);
                        return -1;
                    }
                    while (RTSPBufferedReader.ready()) { // begin for
                        line = RTSPBufferedReader.readLine();
                        if (line.equals("")) {
                            String temp = RTSPBufferedReader.readLine();
                            if (temp.equals("")) {
                                break;
                            }
                        }

                        if (Result_Respone == null) {
                            Result_Respone = line;
                        } else {
                            Result_Respone = Result_Respone + CRLF + line;
                        }
//                    Log.i(TAG, Result_Respone);


                        // Tokenize
                        // Also includes semi-colons
                        tokens = new StringTokenizer(line, " \t\n\r\f;:");

                        // Now go through each token
                        while (tokens.hasMoreTokens()) { // begin while1
                            // Grab token
                            String part = tokens.nextToken();

                            if (part.equals("m=audio")) { // begin if2

                                break;
                            }
                            // Extract the Session ID for subsequent setups
                            else if (part.equals("Session") && RTSPSessionID == null) { // begin if2
                                this.RTSPSessionID = tokens.nextToken();
//                                System.out.println("*** Session ID: " + RTSPSessionID);
                                // Break out of this loop and continue reading other lines
                                break;
                            } // end if2
                        } // end while1
                    } // end for
                } // end if
            } else {
                Log.e(TAG, "Error: " + tempCheck);
//                String line;
//                try {
//                    while (!RTSPBufferedReader.ready()) {
//                        continue;
//                    }
//                } catch (IOException e) {
//                    Result_Respone = "Could not read from read buffer";
//                    System.out.println(Result_Respone);
//                    return -1;
//                }
//                while (RTSPBufferedReader.ready()) { // begin for
//                    line = RTSPBufferedReader.readLine();
//                    if (line.equals("")) {
//                        String temp = RTSPBufferedReader.readLine();
//                        if (temp.equals("")) {
//                            break;
//                        }
//                    }
//
//                    if (Result_Respone == null) {
//                        Result_Respone = line;
//                    } else {
//                        Result_Respone = Result_Respone + CRLF + line;
//                    }
//                    Log.e(TAG, "Error: " + Result_Respone);
//                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Could not read in string from buffer");
//            e.printStackTrace();
        }
        return replyCode;
    }

    private boolean isStringNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void Start_Option_Timer() {
        timerOptions = new Timer();
        timerOptions.scheduleAtFixedRate(new RTSPOptionsTimerTask(), TIMEROPTIONSDELAY, TIMEROPTIONSFREQUENCY);
    }

    public void stopOptionTimer() {
        if (timerOptions != null) {
            timerOptions.cancel();
        }
    }

    private class RTSPOptionsTimerTask extends TimerTask {
        @Override
        public void run() {
            RTSPOptions();
        }
    }

    // Send a request to see what the options are
    public void RTSPOptions() {
        // Increase RTSP Sequence Number
        RTSPSeqNb = 0;
        String requestType = new String("OPTIONS");
        String Send_Data = RTSP_Option.Get_Option(requestType, getRTSPURL(), getServerPort(), getVideoFilename(), RTSPSeqNb);

        sendRTSPBuffer(Send_Data);

        String Respone = receiveBuffer();
//        Log.d(TAG, "---> Response: " + Respone);
//		System.out.println(Respone); /* BY INGU */
    }

    int imagenb = 0;
    DatagramSocket Datagram_send = null;

    public void Send_Voice_Data(byte[] pcm) {


        try {
//			Datagram_send = new DatagramSocket();
            InetAddress ia = InetAddress.getByName(getRTSPURL());
//            for (int i = 0; i < Track_List.size(); i++) {
//                if (Track_List.get(i).Sendonly) {

            imagenb++;

            RTPPacket rtp_packet = new RTPPacket(0, imagenb, 0, pcm, pcm.length);
            int packet_length = rtp_packet.getlength();

            byte[] packet_bits = new byte[packet_length];
            rtp_packet.getpacket(packet_bits);
//
//                    Log.d(TAG, "Packet bits: " + Arrays.toString(packet_bits));
//                    Log.d(TAG, "Packet len: " + packet_bits.length);
//                    Log.d(TAG, "ia: " + ia);
//                    Log.d(TAG, "Integer.parseInt(Track_List.get(i).Server_Port_01): " + Integer.parseInt(Track_List.get(i).Server_Port_01));
//            Log.e(TAG, "TrackList: " + trackList);
//            Log.e(TAG, "TrackList Server_Port_01: " + trackList.get(2).Server_Port_01);
            if (trackList.get(2).Server_Port_01 == null) {
                return;
            }

            DatagramPacket dp = new DatagramPacket(packet_bits, packet_bits.length, ia, Integer.parseInt(trackList.get(2).Server_Port_01));
            Datagram_send.send(dp);
//                    break;
//                }
//            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
        } finally {

        }

    }

    Socket RTP_Socket = null;

    public void openRTPSocket() {

        try {
            Datagram_send = new DatagramSocket();
//            Log.d(TAG, "---> Open RTP Socket");
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void closeRTPSocket() {
        if (Datagram_send != null)
            Datagram_send.close();
    }
}
