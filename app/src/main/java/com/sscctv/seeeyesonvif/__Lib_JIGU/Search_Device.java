package com.sscctv.seeeyesonvif.__Lib_JIGU;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

//import Table_Struct.Device_List_Class;


public class Search_Device {

    DatagramSocket Datagram_send, Datagram_receive, Datagram_send_Info;

    private Thread UDP_Send_Thread;
    private boolean UDP_Send_Flag;

    private Thread UDP_Receivce_Thread;
    private boolean UDP_Receivce_Flag;

    //
//    public Device_List_Class DeviceList ;
//    public Search_Device() {
//    	UDP_Send_Flag = false;
//    	UDP_Receivce_Flag = false;
//
//    	DeviceList = new Device_List_Class();
//    }
//
    public void Start_Device_Search() {
//    	DeviceList.Device_Clear();

        Send_BroadCast_GETINFO();
        UDP_Send_Flag = true;
        UDP_Send_Thread.start();

        Receive_BroadCast_GETINFO();
        UDP_Receivce_Flag = true;
        UDP_Receivce_Thread.start();
    }

    public void Stop_Device_Search() {
        if (UDP_Receivce_Thread != null) {
            UDP_Receivce_Thread.interrupt();
            UDP_Receivce_Flag = false;
        }

        if (UDP_Send_Thread != null) {
            UDP_Send_Thread.interrupt();
            Datagram_receive.close();
            UDP_Send_Flag = false;
        }
    }

    MulticastSocket socket = null;
    BufferedReader reader;

    private boolean Send_BroadCast_GETINFO() {
        UDP_Send_Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    reader = new BufferedReader(new InputStreamReader(System.in));
                    try {
                        socket = new MulticastSocket();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    while (UDP_Send_Flag) {

//						try {
//							InetAddress address = InetAddress.getByName("255.255.255.255"); // ��Ƽĳ��Ʈ ������� ���� �ּҸ� ������.
//							//InetAddress address = InetAddress.getByName("224.0.0.1"); // ��Ƽĳ��Ʈ ������� ���� �ּҸ� ������.
//							byte buffer[] = new byte[2];
//							byte[] JiGuKey = new byte[4];
//							JiGuKey[0] = 0;
//
//							DeviceManagement_Protocol.setVersion(buffer);
//							DeviceManagement_Protocol.setMode(buffer, DeviceManagement_Protocol.GET_NETWORK_INFO);
//
//							byte[] send_buf = DeviceManagement_Protocol.JiGu_Encode(buffer,buffer.length,JiGuKey);
//							DatagramPacket dp = new DatagramPacket(
//									send_buf,send_buf.length,address,DeviceManagement_Protocol.SEND_PORTNUM);
//
//
//							socket.send(dp);
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}


                        try {
                            Datagram_send = new DatagramSocket();
                            //MainConfig.Debug_log.error("SEND Port : " + Datagram_send.getLocalPort());

                            InetAddress ia = InetAddress.getByName(JIGU_Protocol.BroadCast_IP);

                            byte buffer[] = new byte[2];
                            byte[] JiGuKey = new byte[4];
                            JiGuKey[0] = 0;

                            JIGU_Protocol.setVersion(buffer);
                            JIGU_Protocol.setMode(buffer, JIGU_Protocol.GET_NETWORK_INFO);

                            byte[] send_buf = JIGU_Protocol.JiGu_Encode(buffer, buffer.length, JiGuKey);
                            DatagramPacket dp = new DatagramPacket(
                                    send_buf, send_buf.length, ia, JIGU_Protocol.SEND_PORTNUM);
                            Datagram_send.send(dp);


                            Thread.sleep(800);
                        } catch (IOException | InterruptedException e) {
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        } finally {
                            if (Datagram_send != null) {
                                Datagram_send.close();
                            }
                        }
                    }
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                    if (Datagram_send != null) {
                        Datagram_send.close();
                    }

                }
            }

        });
        return true;
    }

    private boolean Receive_BroadCast_GETINFO() {
        try {
            Datagram_receive = new DatagramSocket(JIGU_Protocol.RECEIVE_PORTNUM);
        } catch (SocketException e) {
            return false;
        }

        UDP_Receivce_Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (UDP_Receivce_Flag) {

                    try {

                        byte[] rececive_buffer = new byte[512];

                        byte[] JiGuKey = new byte[4];
                        JiGuKey[0] = 0;

                        DatagramPacket data_receive = new DatagramPacket(rececive_buffer, rececive_buffer.length);

                        Datagram_receive.receive(data_receive);

                        int data_length = data_receive.getLength();
                        byte result_buffer[] = new byte[data_length];

                        data_receive.setData(rececive_buffer, 0, data_length);
                        //System.out.println(DeviceManagement_Library.byteArrayToHexString(rececive_buffer, 0, data_length));

                        byte error = JIGU_Protocol.JiGu_Decode(rececive_buffer, data_length, result_buffer, JiGuKey);
                        switch (error) {
                            case JIGU_Protocol.JiGu_Error_OK:
                                process_Data(result_buffer, data_receive.getAddress().toString());
                                break;
                            case JIGU_Protocol.JiGu_Error_Header:
                                System.out.println("Error Header");
                                break;
                            case JIGU_Protocol.JiGu_Error_CheckSum1:
                                System.out.println("Error_CheckSum1");
                                break;
                            case JIGU_Protocol.JiGu_Error_CheckSum2:
                                System.out.println("Error_CheckSum2");
                                break;
                        }

                    } catch (IOException e) {

                    }

                }
            }
        });
        return true;
    }

    private void process_Data(byte[] Data, String IP) {
        int Mode = JIGU_Library.HexByteToInteger(Data, 1, 1);

        try {
            switch (Mode) {

                case JIGU_Protocol.GET_NET_INFO_RESPONSE:

                    Process_NetInfo(Data);
                    break;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage()); /* BY INGU */

        }

    }


    private void Process_NetInfo(byte[] Data) {

        String Mac = JIGU_Library.byteArrayToMacAddress(Data, 2, 7).trim();
        String IP_Address = JIGU_Library.byteArrayToIPAddress(Data, 8, 11);
        String GateWay = JIGU_Library.byteArrayToIPAddress(Data, 12, 15);
        String NetMask = JIGU_Library.byteArrayToIPAddress(Data, 16, 19);
        String Device_Name = JIGU_Library.byteArrayToDeviceModel(Data, 20, 33).trim();
        String Version = JIGU_Library.byteArrayToDeviceModel(Data, 34, 34).trim();
        String Camera_Ip = JIGU_Library.byteArrayToIPAddress(Data, 35, 38).trim();

//		DeviceList.Add_Device(IP_Address, Mac, Device_Name , GateWay , NetMask);

    }
}
