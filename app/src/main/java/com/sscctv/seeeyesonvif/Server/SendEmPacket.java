package com.sscctv.seeeyesonvif.Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SendEmPacket implements Runnable {

    private static final String TAG = SendEmPacket.class.getSimpleName();
    private static final String IP = "255.255.255.255";
    private static final int PORT = 65305;
    private final byte[] str;

    public SendEmPacket(byte[] str) {
        this.str = str;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(IP);
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(str, str.length, serverAddr, PORT);
            socket.send(packet);
            socket.receive(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
