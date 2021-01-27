package com.sscctv.seeeyesonvif.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.sscctv.seeeyesonvif.R;

import java.security.MessageDigest;

public class IPUtilsCommand {
    private static final String TAG = IPUtilsCommand.class.getSimpleName();
    public static final String IP = "255.255.255.255";
    public static final int PORT = 65305;

    public static final String MODE_DISCOVERY = "mode_discovery";
    public static final String MODE_IP_CHANGE = "mode_ip_change";
    public static final String MODE_FACTORY_RESET = "mode_factory_reset";
    public static final String MODE_EMERGENCY_CALL = "mode_emergency_call";

    public static final String hexDiscovery = "6373010004001a0000000100000000000000000000000000000000000000000000003511";
    public static final String hexModelName = "0000014D6F64656C204E616D650000000000000000000000000000000000000000000020000000";
    public static final String hexSubModel = "537562204D6F64656C20417070656E640000000000000000000000000000000008000000";
    public static final String hexFirmware = "4669726D776172652056657273696F6E0000000000000000000000000000000020000000";
    public static final String hexFormat = "566964656F20466F726D6174000000000000000000000000000000000000000020000000";
    public static final String hexResponseDiscovery = "637301000500";
    public static final String hexResponseIpChange = "63730100030045000000";
    public static final String hexResponseFactoryReset = "63730100010134000000";
    public static final String hexResponseEmergencyCall = "6373070001009d000000";

    public static final String STRING_SPLIT= "_@#@_";
    public static final String sendMainDeviceDiscovery = "send_main_discovery";
    public static final String resMainDeviceDiscovery = "res_main_discovery";


    public static String changeNetwork(boolean strMode, String id, String pass, String mac, String mode, String ip, String subnet, String gate, String http, String https) throws Exception {

        String head = "637301000200e1000000";
        String strId = stringToHex(id, 64, true);
        String strPass = getEncMD5(pass);
        String strRandom = getRandom(32);
        String strMac = stringToHex(mac, 40, true);
        String strIp = stringToHex(ip, 32, true);
        String strSubnet = stringToHex(subnet, 32, true);
        String strGate = stringToHex(gate, 32, true);
        String strHttp;
        String strHttps;
        if (strMode) {
            strHttp = stringToHex(http, 8, false);
            strHttps = stringToHex(https, 8, false);
        } else {
            strHttp = reverseTransPort(http);
            strHttps = reverseTransPort(https);
        }
        String strNull = stringToHex("", 168, true);

        return head + strId + strPass + strRandom + strMac + mode + strIp + strSubnet + strGate + strHttp + strHttps + strNull;
    }

    public static String cgiCreateAccount(Context context, String ip, String port, String id, String pass) throws Exception {

        String modeAdd = "&mode=add";
        String userId = "&user_id=";
        String userPw = "&user_pw=";
        String userLevel = "&user_level=ADMIN";

        String newId = context.getResources().getString(R.string.default_login_id);
        String newPass = context.getResources().getString(R.string.default_login_pass);

        Log.d(TAG, "CGI: " + IPUtilsCGI.REQUEST_HTTP + ip + ":" + port + IPUtilsCGI.CGI_SET + IPUtilsCGI.CGI_ID + id
                + IPUtilsCGI.CGI_PASS + getEncMD5(pass) + modeAdd + IPUtilsCGI.CGI_GROUP + IPUtilsCGI.CGI_SYSTEM + IPUtilsCGI.CGI_SYSTEM_USER
                + userId + newId + userPw + newPass + userLevel);
        return IPUtilsCGI.REQUEST_HTTP + ip + ":" + port + IPUtilsCGI.CGI_SET + IPUtilsCGI.CGI_ID + id
                + IPUtilsCGI.CGI_PASS + getEncMD5(pass) + modeAdd + IPUtilsCGI.CGI_GROUP + IPUtilsCGI.CGI_SYSTEM + IPUtilsCGI.CGI_SYSTEM_USER
                + userId + newId + userPw + newPass + userLevel;
    }

    public static String cgiActivation(String ip, String port, String newId, String newPass) throws Exception {

        String modeUpdate = "&mode=update";
        String userId = "&user_id=";
        String userPw = "&user_pw=";
        String userLevel = "&user_level=ADMIN";

        Log.d(TAG, "CGI: " + IPUtilsCGI.REQUEST_HTTP + ip + ":" + port + IPUtilsCGI.CGI_SET + IPUtilsCGI.CGI_ID + "root"
                + IPUtilsCGI.CGI_PASS + getEncMD5("root") + modeUpdate + IPUtilsCGI.CGI_GROUP + IPUtilsCGI.CGI_SYSTEM + IPUtilsCGI.CGI_SYSTEM_USER
                + userId + newId + userPw + newPass + userLevel);
        return IPUtilsCGI.REQUEST_HTTP + ip + ":" + port + IPUtilsCGI.CGI_SET + IPUtilsCGI.CGI_ID + "root"
                + IPUtilsCGI.CGI_PASS + getEncMD5("root") + modeUpdate + IPUtilsCGI.CGI_GROUP + IPUtilsCGI.CGI_SYSTEM + IPUtilsCGI.CGI_SYSTEM_USER
                + userId + newId + userPw + newPass + userLevel;
    }

    public static String cgiCheckActivation(String ip, String port) throws Exception {
        String modeUpdate = "&mode=get";
        Log.d(TAG, "CGI: " + IPUtilsCGI.REQUEST_HTTP + ip + ":" + port + IPUtilsCGI.CGI_SET + IPUtilsCGI.CGI_ID + "root"
                + IPUtilsCGI.CGI_PASS + getEncMD5("root") + modeUpdate + IPUtilsCGI.CGI_GROUP + IPUtilsCGI.CGI_SYSTEM + IPUtilsCGI.CGI_SYSTEM_INFO);
        return IPUtilsCGI.REQUEST_HTTP + ip + ":" + port + IPUtilsCGI.CGI_SET + IPUtilsCGI.CGI_ID + "root"
                + IPUtilsCGI.CGI_PASS + getEncMD5("root") + modeUpdate + IPUtilsCGI.CGI_GROUP + IPUtilsCGI.CGI_SYSTEM + IPUtilsCGI.CGI_SYSTEM_INFO;
    }

    public static String cgiMcsModeSetup(String ip, String port, String mode) {
        return IPUtilsCGI.REQUEST_HTTP + ip + ":" + port + IPUtilsCGI.CGI_EM_SET + mode;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xff));
        }

        return sb.toString();
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String stringToHex(String s, int len, boolean val) {
        StringBuilder result = new StringBuilder();

        if (val) {
            for (int i = 0; i < s.length(); i++) {
                result.append(String.format("%02x", (int) s.charAt(i)));
            }
            if (result.length() < len) {
                int zeroLen = len - result.length();

                for (int i = 0; i < zeroLen; i++) {
                    result.append(0);
                }
            }

        } else {
            result.append(s);

            if (s.length() < len) {
                int zeroLen = len - s.length();
                for (int i = 0; i < zeroLen; i++) {
                    result.append(0);
                }
            }
        }
        return result.toString();
    }

    @SuppressLint("DefaultLocale")
    public static String reverseTransPort(String val) {
        String str1 = null;
        String str2 = null;
        String hexVal = Integer.toHexString(Integer.parseInt(val));
        Log.d(TAG, "Hex: " + hexVal + " Size: " + hexVal.length());
        String result;
        if (hexVal.length() < 3) {
            str1 = hexVal + "00";
            result = stringToHex(str1, 8, false);
        } else if (hexVal.length() == 3) {
            str1 = "0" + hexVal.substring(0, 1);
            str2 = hexVal.substring(1, 3);
            String temp = str2 + str1;
            result = stringToHex(temp, 8, false);
        } else {
            str1 = hexVal.substring(0, 2);
            str2 = hexVal.substring(2, 4);
            String temp = str2 + str1;
            result = stringToHex(temp, 8, false);

        }
        Log.d(TAG, "result: " + result);
        return result;
//        }
    }


    public static String getEncMD5(String txt) throws Exception {

        StringBuilder sbuf = new StringBuilder();

        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        mDigest.update(txt.getBytes());

        byte[] msgStr = mDigest.digest();
        for (byte b : msgStr) {
            String tmpEncTxt = Integer.toHexString((int) b & 0x00ff);
            if (tmpEncTxt.length() == 1) {
                sbuf.append("0").append(tmpEncTxt);
            } else {
                sbuf.append(tmpEncTxt);
            }
        }
        return sbuf.toString();
    }

    public static String getRandom(int len) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < len; i++) {
            int iValue = (int) (Math.random() * 10);
            result.append(iValue);
        }

        return result.toString();
    }


}
