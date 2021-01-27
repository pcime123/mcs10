package com.sscctv.seeeyesonvif.Server;

import android.util.Log;

import com.sscctv.seeeyesonvif.Items.ItemDoorDevice;
import com.sscctv.seeeyesonvif.__Lib_JIGU.JIGU_Protocol;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.IPUtilsCommand;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SendDoorPacket implements Runnable {

    private static final String TAG = SendDoorPacket.class.getSimpleName();
    private static final String IP = "255.255.255.255";
    private static final int PORT = 36963;
    private String mode;
    private ItemDoorDevice itemDoorDevice;

    public SendDoorPacket(String mode, ItemDoorDevice itemDoorDevice) {
        this.mode = mode;
        this.itemDoorDevice = itemDoorDevice;
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
                case AppUtils.EDAL_MODE_CHANGE:
                    Log.d(TAG, "Mac: " + itemDoorDevice.getdMac() + "," + itemDoorDevice.getdIp() + "," + itemDoorDevice.getdGateWay() + "," + itemDoorDevice.getdNetMask());
                    buffer = JIGU_Protocol.Make_SET_IP_Packet(itemDoorDevice.getdMac(), itemDoorDevice.getdIp(), itemDoorDevice.getdGateWay(), itemDoorDevice.getdNetMask());
                    break;
                case AppUtils.EDAL_MODE_RESET:
                    buffer = JIGU_Protocol.Make_Set_Reset(itemDoorDevice.getdMac());
                    break;
                case AppUtils.EDAL_MODE_DEFAULT:
                    buffer = JIGU_Protocol.setFactoryDefault(itemDoorDevice.getdMac());
                    break;
                case AppUtils.EDAL_MODE_EMLOCK:
                    buffer = JIGU_Protocol.makeEmLock(itemDoorDevice.getdMac(), Integer.parseInt(itemDoorDevice.getdIp()));
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
