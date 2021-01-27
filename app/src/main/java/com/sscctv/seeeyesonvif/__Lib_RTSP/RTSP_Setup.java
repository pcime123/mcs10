
package com.sscctv.seeeyesonvif.__Lib_RTSP;

public class RTSP_Setup {
    private final static String CRLF = "\r\n";

    public static String Get_Setup(String Request, String Host, int Port, String VideoFile, int CSeq, ItemRtspTrack Track, String SessionID,
                                   int Client_Port) {
        StringBuilder stringToSend = new StringBuilder();

        if (VideoFile != null) {
            stringToSend.append(Request).append(" rtsp://").append(Host).append(":").append(Port).append("/").append(VideoFile).append("/").append(Track.Track).append(" RTSP/1.0").append(CRLF);
        } else {
            stringToSend.append(Request).append(" rtsp://").append(Host).append(":").append(Port).append("/").append(Track.Track).append("/ RTSP/1.0").append(CRLF);
        }
        stringToSend.append("CSeq: ").append(CSeq).append(CRLF);

        if (SessionID != null) {
            stringToSend.append("Session: ").append(SessionID).append(CRLF);
        }
        stringToSend.append("Transport: " + "RTP/AVP" + ";unicast;client_port=").append(Client_Port).append("-").append(Client_Port + 1).append(CRLF);
        stringToSend.append("User-Agent: " + RTPPacket.AGENT + CRLF);
        stringToSend.append("Require: " + "www.onvif.org/ver20/backchannel" + CRLF + CRLF);
        return stringToSend.toString();

    }
}
