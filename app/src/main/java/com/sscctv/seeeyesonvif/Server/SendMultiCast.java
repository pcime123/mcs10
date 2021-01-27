package com.sscctv.seeeyesonvif.Server;

import android.util.Log;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SendMultiCast implements Runnable {

    private static final String TAG = SendMultiCast.class.getSimpleName();
    private TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private String IP = "239.21.218.198";
    private int PORT = 1235;
    private String str;

    public SendMultiCast(String str) {
        this.str = str;
    }

    @Override
    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket(PORT);
            InetAddress byName = InetAddress.getByName(IP);
            socket.joinGroup(byName);

            Log.w(TAG, "<<  MultiCastClient running... >>");
            Log.i(TAG, "<<  IP: " + IP + " Port: " + PORT + " String: " + str + " >>");
            byte[] buf = str.getBytes();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, byName, PORT);
            socket.send(packet);

            Log.w(TAG, "<<  MultiCastClient send: " + str + " >>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
