package com.sscctv.seeeyesonvif.__Lib_JIGU;


import android.util.Log;

import java.util.Random;

public class JIGU_Protocol {

    public static final int JiGuHeader_DA = 0;
    public static final int JiGuHeader_R1 = 1;
    public static final int JiGuHeader_R2 = 2;
    public static final int JiGuCheckSum1 = 3;
    public static final int JiGuDataStart = 4;

    public static final int Key1 = 0;
    public static final int Key2 = 1;
    public static final int Key3 = 2;
    public static final int Key4 = 3;

    public static final int JiGu_Error_OK = 0;
    public static final int JiGu_Error_Header = 1;
    public static final int JiGu_Error_CheckSum1 = 2;
    public static final int JiGu_Error_CheckSum2 = 3;

    public final static int SEND_PORTNUM = 36963;
    public final static int RECEIVE_PORTNUM = 36964;

    public static final byte[] JiGuKeyWord = {'D', 'a', 'n', 'i'};
    public static final byte JiGuSTART_CODE = (byte) 0xDA;

    public final static int GET_MAC_INFO = 1;
    public final static int GET_NETWORK_INFO = 2;
    public final static int GET_NETWORK_INFO_CAMERA = 29;

    public final static int GET_IP_INFO = 3;
    public final static int GET_DEVICE_MANAGER = 4;
    public final static int GET_DEVICE_MANAGER02 = 5;
    public final static int GET_MCU_GPIO = 6;
    public final static int GET_MCU_ALL_PIN = 7;

    public final static int Version_1 = 1;

    public final static int SET_DEVICE_SIZE = 11;
    public final static int SET_RESET_SIZE = 8;
    public final static int SET_EMLOCK_SIZE = 9;
    public final static int SET_ACCESS_CODE_SIZE = 12;
    public final static int SET_MAC_SIZE = 14;
    public final static int SET_IP_SIZE = 20;
	public final static int SET_ACCESS_CODE = 32;
	public final static int SET_MAC = 33;
	public final static int SET_NETWORK_INFO = 34;
	public final static int SET_IP = 35;
	public final static int SET_DEVICE_DATA = 36;
	public final static int SET_EMLOCK = 41;
	public final static int SET_INFO_SIZE = 46;
    public final static int SET_FACTORY_DEFAULT = 62;
    public final static int SET_RESET = 63;

    public final static String GET_INFO_MESSAGE = "696E61440101";
    public final static String BroadCast_IP = "255.255.255.255";

    public final static int GET_BOOT_INFO = 64;
    public final static int GET_DM_DATA_RESPONE = 68;
    public final static int GET_DM_DATA_RESPONE02 = 69;
    public final static int GET_ACCESS_CODE_RESPONE = 96;

    public final static int GET_NET_INFO_RESPONSE = 66;
    public final static int GET_NET_INFO_RESPONSE_CAMERA = 93;

    public final static int SET_NETWORK_INFO_RESPONE = 98;
    public final static int SET_DEVICE_DATA_RESPONE = 100;
    public final static int SET_IP_RESPONE = 99;
    public final static int SET_MAC_RESPONE = 97;
    public final static int SET_FACTORY_RESET_RESPONE = 126;
    public final static int SET_RESET_RESPONE = 127;
    public final static int SET_ERROR_RESPONE = 128;


    public static void setVersion(byte buf[]) {
        String hex = JIGU_Library.IntegerToHexString(Version_1);
        JIGU_Library.hexStringToByte(buf, hex, 0, 0);
    }

    public static void setMode(byte buf[], int Mode) {
        String hex = JIGU_Library.IntegerToHexString(Mode);
        JIGU_Library.hexStringToByte(buf, hex, 1, 1);
    }

    public static void setLock(byte buf[], int val) {
        String hex = JIGU_Library.IntegerToHexString(val);
        JIGU_Library.hexStringToByte(buf, hex, 8, 8);

    }

    public static void SetDM_Data(byte buf[], int Port, int RegAddr, int Data) {
        String hex = JIGU_Library.IntegerToHexString(Port);
        JIGU_Library.hexStringToByte(buf, hex, 8, 8);

        hex = JIGU_Library.IntegerToHexString(RegAddr);
        JIGU_Library.hexStringToByte(buf, hex, 9, 9);

        hex = JIGU_Library.IntegerToHexString(Data);
        JIGU_Library.hexStringToByte(buf, hex, 10, 10);
    }


    public static void setAccessCode(byte buf[], byte code[]) {
        buf[8] = (byte) (code[8] ^ JiGuKeyWord[0]);
        buf[9] = (byte) (code[9] ^ JiGuKeyWord[1]);
        buf[10] = (byte) (code[10] ^ JiGuKeyWord[2]);
        buf[11] = (byte) (code[11] ^ JiGuKeyWord[3]);
    }

    public static void setMacAddress(byte[] buf, String Mac, int start) {
        String[] result = Mac.split(":");
        for (int i = 0; i < result.length; i++) {
            JIGU_Library.hexStringToByte(buf, result[i], start + i, start + i);
        }
    }

    public static void setIPAddress(byte buf[], String IP, int start) {
        String[] result = IP.split("\\.");
        for (int i = 0; i < result.length; i++) {
            String hex = JIGU_Library.IntegerToHexString(Integer.parseInt(result[i]));
            JIGU_Library.hexStringToByte(buf, hex, start + i, start + i);
        }
    }

    public static void setMTU(byte[] buf, String MTU_Str, int start) {
        JIGU_Library.hexStringToByte(buf, MTU_Str, start, start + 1);
    }

    public static void setDHCP(byte buf[], String DHCP_Str, int start) {
        JIGU_Library.hexStringToByte(buf, DHCP_Str, start, start);
    }

    public static void setSNMP_RX(byte buf[], String SNMP_RX, int start) {
        JIGU_Library.hexStringToByte(buf, SNMP_RX, start, start + 1);
    }

    public static void setSNMP_TX(byte buf[], String SNMP_TX, int start) {
        JIGU_Library.hexStringToByte(buf, SNMP_TX, start, start + 1);
    }

    public static void setTrap_Status(byte buf[], boolean Trap_Status01, boolean Trap_Status02, boolean Trap_Status03, int start) {
        String hex;
        if (Trap_Status01) {
            hex = JIGU_Library.IntegerToHexString(0);
            JIGU_Library.hexStringToByte(buf, hex, start, start);
        } else if (Trap_Status02) {
            hex = JIGU_Library.IntegerToHexString(1);
            JIGU_Library.hexStringToByte(buf, hex, start, start);
        } else {
            if (Trap_Status03) {
                hex = JIGU_Library.IntegerToHexString(1);
                JIGU_Library.hexStringToByte(buf, hex, start, start);
            } else {
                hex = JIGU_Library.IntegerToHexString(0);
                JIGU_Library.hexStringToByte(buf, hex, start, start);
            }
        }
    }


    /*
    public static void JiGuTest(byte Src[] , byte NumBytes) {
        byte i = 0 ;
        byte Err;

        System.out.println(" JiGuEncryption Test Size "+NumBytes);

        System.out.println(" Encode Sre :");

        for(i=0;i<NumBytes;i++)
        {
            s(DeviceManagement_Library.byteArrayToHexString(Src));
        }

    }
*/
    public static byte[] JiGu_Encode(byte[] Src, int byte_size, byte[] JiGuKey) {
        int sum = 0;
        int LastData = 0;

        byte[] Tmp = new byte[byte_size + JiGuDataStart + 1];

        for (int i = 0; i < Src.length; i++) {
            Tmp[JiGuDataStart + i] = Src[i];
        }
        for (int i = 0; i < JiGuDataStart; i++) {
            Tmp[i] = 0;
        }

        LastData = Tmp.length;

        JiGuHeaderEnc(Tmp, JiGuKey);

        for (int i = JiGuDataStart; i < LastData - 1; i++) {
            Tmp[i] ^= JiGuKey[i % 4];
            sum += Tmp[i];
            Tmp[i] = JiGuExchangeBit(Tmp[i]);
        }

        Tmp[JiGuCheckSum1] = (byte) ((sum & 0xFF) ^ JiGuKey[Key4]);// Step7
        Tmp[LastData - 1] = (byte) ((sum & 0xFF) ^ JiGuKey[Key3]);// Step7

        return Tmp;
    }

    public static void JiGuKeyUpdate(byte JiGuHeadR1, byte JiGuHeadR2, byte[] JiGuKey) {
        JiGuKey[Key1] = (byte) (JiGuHeadR1 ^ JiGuKeyWord[Key1]);
        JiGuKey[Key2] = (byte) (JiGuHeadR2 ^ JiGuKeyWord[Key2]);
        JiGuKey[Key3] = (byte) (JiGuKey[Key1] ^ JiGuKeyWord[Key3]);
        JiGuKey[Key4] = (byte) (JiGuKey[Key2] ^ JiGuKeyWord[Key4]);
    }

    public static void JiGuHeaderEnc(byte[] Src, byte[] JiGuKey) {
        byte[] Random = new byte[2];
        Random ran = new Random();
        ran.nextBytes(Random);

        JiGuKeyUpdate(Random[0], Random[1], JiGuKey);


        Src[JiGuHeader_DA] = (byte) (JiGuSTART_CODE ^ JiGuKey[Key1]);
        Src[JiGuHeader_R1] = Random[0];
        Src[JiGuHeader_R2] = Random[1];
    }

    public static byte JiGuExchangeBit(byte data) {
        data = (byte) (((data << 2) & 0xCC) | ((data >> 2 & 0x33)));
        return data;
    }

    public static byte JiGu_Decode(byte[] Src, int NumBytes, byte[] result, byte[] JiGuKey) {

        int i;
        int Sum = 0;
        //System.out.println("Decode Length : " + NumBytes); /* BY INGU */
        JiGuKeyUpdate(Src[JiGuHeader_R1], Src[JiGuHeader_R2], JiGuKey);

        if (Src[JiGuHeader_DA] != (JiGuSTART_CODE ^ JiGuKey[Key1]))    // Start Key
        {
            return JiGu_Error_Header;
        }

        for (i = JiGuDataStart; i < NumBytes - 1; i++) {
            Src[i] = JiGuExchangeBit(Src[i]);
            Sum += Src[i];
            Src[i] ^= JiGuKey[i % 4];
            //System.out.println("SRC01 : " + DeviceManagement_Library.byteArrayToHexString(Src[i])); /* BY INGU */
        }
        //System.out.println("Decode Sum = " + DeviceManagement_Library.IntegerToHexString(Sum)); /* BY INGU */

        if (Src[JiGuCheckSum1] != (byte) ((Sum & 0xFF) ^ JiGuKey[Key4])) {
            return JiGu_Error_CheckSum1;
        }

        if (Src[NumBytes - 1] != (byte) ((Sum & 0xFF) ^ JiGuKey[Key3])) {
            return JiGu_Error_CheckSum2;
        }

        for (i = 0; i < result.length; i++) {
            result[i] = Src[JiGuDataStart + i];
        }

        return JiGu_Error_OK;
    }

    public static byte[] Make_SET_IP_Packet(String Mac, String NewIP, String NewGW, String NewNetMask) {
        byte[] buffer = new byte[20];
        byte[] JiGuKey = new byte[4];
        JiGuKey[0] = 0;

        setVersion(buffer);
        setMode(buffer, JIGU_Protocol.SET_IP);
        setMacAddress(buffer, Mac, 2);
        setIPAddress(buffer, NewIP, 8);
        setIPAddress(buffer, NewGW, 12);
        setIPAddress(buffer, NewNetMask, 16);

//		buffer = JiGu_Encode(buffer,buffer.length,JiGuKey);

        return buffer;
    }


    public static byte[] Make_Set_Reset(String Mac) {
        byte[] buffer = new byte[SET_RESET_SIZE];
        setVersion(buffer);
        setMode(buffer, JIGU_Protocol.SET_RESET);
        setMacAddress(buffer, Mac, 2);

//		byte[] JiGuKey = new byte[4];
//		JiGuKey[0] = 0;

//		buffer = JiGu_Encode(buffer,buffer.length,JiGuKey);

        return buffer;
    }

    public static byte[] makeEmLock(String Mac, int val) {
        byte[] buffer = new byte[SET_EMLOCK_SIZE];
        setVersion(buffer);
        setMode(buffer, JIGU_Protocol.SET_EMLOCK);
        setMacAddress(buffer, Mac, 2);
        setLock(buffer, val);
//		byte[] JiGuKey = new byte[4];
//		JiGuKey[0] = 0;

//		buffer = JiGu_Encode(buffer,buffer.length,JiGuKey);
        Log.d("Hi", "Buffer: " + buffer);
        return buffer;
    }


    public static byte[] setFactoryDefault(String Mac) {
        byte[] buffer = new byte[SET_RESET_SIZE];
        setVersion(buffer);
        setMode(buffer, JIGU_Protocol.SET_FACTORY_DEFAULT);
        setMacAddress(buffer, Mac, 2);

//		byte[] JiGuKey = new byte[4];
//		JiGuKey[0] = 0;

//		buffer = JiGu_Encode(buffer,buffer.length,JiGuKey);

        return buffer;
    }

    public static byte[] Make_Set_MAC_Packet(String OldMac, String NewMac) {
        byte buffer[] = new byte[JIGU_Protocol.SET_MAC_SIZE];
        setVersion(buffer);
        setMode(buffer, JIGU_Protocol.SET_MAC);
        setMacAddress(buffer, OldMac, 2);
        setMacAddress(buffer, NewMac, 8);

        byte[] JiGuKey = new byte[4];
        JiGuKey[0] = 0;

        buffer = JiGu_Encode(buffer, buffer.length, JiGuKey);

        return buffer;
    }
}
