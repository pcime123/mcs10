package com.sscctv.seeeyesonvif.Server;

import android.util.Log;

import com.sscctv.seeeyesonvif.__Lib_JIGU.JIGU_Protocol;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.IPUtilsCommand;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SendEMTest implements Runnable {

    private static final String TAG = SendEMTest.class.getSimpleName();
    private static final String IP = "255.255.255.255";
    private static final int PORT = 36963;
    private String mode;

    public SendEMTest(String mode) {
        this.mode = mode;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(IP);
            socket.setBroadcast(true);

            byte[] buffer = null;
            byte[] JiGuKey = new byte[4];
            JiGuKey[0] = 0;

            switch (mode) {
                case AppUtils.EDAL_MODE_SEARCH:
                    buffer = new byte[2];
                    JIGU_Protocol.setVersion(buffer);
                    JIGU_Protocol.setMode(buffer, JIGU_Protocol.GET_NETWORK_INFO_CAMERA);
                    break;
                case AppUtils.EDAL_MODE_INFO:
                    buffer = new byte[2];
                    JIGU_Protocol.setVersion(buffer);
                    JIGU_Protocol.setMode(buffer, JIGU_Protocol.GET_DEVICE_MANAGER);
                    break;
                case AppUtils.EDAL_MODE_CHANGE:
                    buffer = JIGU_Protocol.Make_SET_IP_Packet("65:6b:73:6c:73:32", "192.168.50.77", "192.168.50.1", "255.255.255.0");
                    break;
                case AppUtils.EDAL_MODE_RESET:
                    buffer = JIGU_Protocol.Make_Set_Reset("65:6b:73:6c:73:32");
                    break;

            }

            byte[] send_buf = JIGU_Protocol.JiGu_Encode(buffer, buffer.length, JiGuKey);
            DatagramPacket packet = new DatagramPacket(
                    send_buf, send_buf.length, serverAddr, JIGU_Protocol.SEND_PORTNUM);
            Log.v("Protocol", "SendEMData: " + IPUtilsCommand.bytesToHex(send_buf));
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
