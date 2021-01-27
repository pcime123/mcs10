package com.sscctv.seeeyesonvif.__Lib_RTSP;

public class RTSP_Describe {
    // Carriage return and new line to send to server
    private final static String CRLF = "\r\n";

    public static String Get_Describe(String Request, String Host, int Port, String VideoFile, int CSeq) {
        StringBuilder stringToSend = new StringBuilder();

        if (VideoFile != null) {
            stringToSend.append(Request).append(" rtsp://").append(Host).append(":").append(Port).append("/").append(VideoFile).append(" RTSP/1.0").append(CRLF);
        } else {
            stringToSend.append(Request).append(" rtsp://").append(Host).append(":").append(Port).append("/ RTSP/1.0").append(CRLF);
        }
        stringToSend.append("CSeq: " + 1 + CRLF);
        stringToSend.append("Accept: " + "application/sdp" + CRLF);
        stringToSend.append("User-Agent: " + RTPPacket.AGENT + CRLF);
        stringToSend.append("Require: " + "www.onvif.org/ver20/backchannel" + CRLF + CRLF);

        return stringToSend.toString();

    }

    public static StringBuilder Get_Describe_StringBuilder(String Request, String Host, int Port, String VideoFile, int CSeq) {

        StringBuilder stringToSend = new StringBuilder();

        if (VideoFile != null)
            stringToSend.append(Request).append(" rtsp://").append(Host).append(":").append(Port).append("/").append(VideoFile).append(" RTSP/1.0").append(CRLF);
        else
            stringToSend.append(Request).append(" rtsp://").append(Host).append(":").append(Port).append("/ RTSP/1.0").append(CRLF);

        // Send sequence number
        // If there is no session ID, this is the last thing we send
        stringToSend.append("CSeq: " + 1 + CRLF);
        stringToSend.append("User-Agent: " + "Seeeyes mcs rtsp client" + CRLF);


        stringToSend.append("Accept: " + "application/sdp" + CRLF);
        stringToSend.append("Require: " + "www.onvif.org/ver20/backchannel" + CRLF + CRLF);

        return stringToSend;

    }


}
