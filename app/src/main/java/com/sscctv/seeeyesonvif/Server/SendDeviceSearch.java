package com.sscctv.seeeyesonvif.Server;

import com.sscctv.seeeyesonvif.Utils.IPUtilsCommand;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SendDeviceSearch implements Runnable {

    private static final String TAG = SendDeviceSearch.class.getSimpleName();
    private final String str;

    public SendDeviceSearch(String str) {
        this.str = str;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(IPUtilsCommand.IP);
            socket.setBroadcast(true);

            byte[] buf;
            buf = str.getBytes();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, IPUtilsCommand.PORT);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
