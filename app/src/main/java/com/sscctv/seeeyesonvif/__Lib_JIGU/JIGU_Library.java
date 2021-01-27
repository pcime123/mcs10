package com.sscctv.seeeyesonvif.__Lib_JIGU;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class JIGU_Library {

	public static int Save_Interval = 60000;
	public static int SNMP_SET_INTERVAL = 1500;
	public static int AUTO_POE_THRESHOLD = 180000;

	public final static int ERROR_OK = 1;
	public final static int ERROR_NOTCLEAR = 2;
	public final static int ERROR_CLEAR = 3;
	public final static int ERROR_NORMAL = 50;

	public final static int ERROR_DISCONNECTED = 0;
	public final static int POESTAT_ON = 1;
	public final static int ERROR_OPEN = 2;
	public final static int ERROR_SHORT = 3;
	public final static int ERROR_OVERCURRNET = 4;
	public final static int ERROR_AUTOPOE = 5;
	public final static int ERROR_PORTRESET = 6;
	public final static int ERROR_SLOTCHANGE = 7;
	public final static int ERROR_EXTENDPORT_DOWN = 8;

	public final static int UDP_DM_REPONE = 100;

	public final static int UPLINK01 = 1;
	public final static int UPLINK02 = 2;
	public final static int SFPPORT01 = 3;
	public final static int SFPPORT02 = 4;

	public final static int UNKNOWN_DEVICE = 0;
	public final static int IPS24GFS = 1;
	public final static int IPJ24M = 2;
	public final static int IPC0708H = 3;
	public final static int IPJ08M = 4;
	public final static int IPJ16M = 5;
	public final static int IPS24P = 6;
	public final static int IPS16P = 7;
	public final static int IPS08P = 8;
	public final static int IPS24PL = 9;
	public final static int IPS16PL = 10;
	public final static int IPS08PL = 11;
	public final static int IPS24PL_8 = 12;
	public final static int IPS24PL_24 = 13;
	public final static int IPS16PL_8 = 14;
	public final static int IPS16PL_16 = 15;
	public final static int IPC0708HU = 16;

	public final static int OTHERDEVICE = 99;
	public final static int CAMERA_LIST = 100;

	public final static int SHOW_IP = 0;
	public final static int SHOW_NAME = 1;
	public final static int SHOW_LOCATION = 2;

	public final static int MIX_TYPE = 0;
	public final static int UTP_TYPE = 1;
	public final static int BNC_TYPE = 2;

	public final static int SPEED_100M = 0;
	public final static int SPEED_10M = 1;
	public final static int SPEED_1000M = 2;
	public final static int SPEED_NONE = 3;

	public final static int ALARM_DISOCONNECT = 0;
	public final static int ALARM_OPEN = 1;
	public final static int ALARM_SHORT = 2;
	public final static int ALARM_OVERCURRENT = 3;
	public final static int ALARM_AUTOPOE = 4;
	public final static int ALARM_EXTENDPORT_DOWN = 5;
	public final static int ALARM_DEFAULT = 6;
	public final static int ALARM_TOTAL = 7;

	public final static int ALARM_EXTENDPORT_NUM = 100;

	public final static int ALARM_POWERBOARD = 500;

	public final static int BOOT_INFO_BOOTING = 0;
	public final static int BOOT_INFO_ACCESSCODE = 1;
	public final static int BOOT_INFO_COMPLETE = 2;
	public final static int BOOT_INFO_DISCONNECTED = 3;
	public final static int BOOT_INFO_UNKNOWN = 4;

	public final static int LICENSE_VERSION = 0;
	public final static int NO_LICENSE_VERSION = 1;
	public final static int TRIAL_VERSION = 2;
	

	public final static int MAC_CONFIG = 1 ; 
	public final static int IP_CONFIG = 2 ;
	
	private static final String PATTERN = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
	
	public static String byteArrayToString(byte[] buf, int start, int end) {
		String result;
		String Temp_Str = "";

		try {
			StringBuilder sb = new StringBuilder();

			for (int i = start; i <= end; i++) {
				sb.append(String.format("%02X", buf[i] & 0xff));
			}

			byte[] bytes = Hex.decodeHex(sb.toString().toCharArray());
			result = new String(bytes, StandardCharsets.UTF_8);
			return result;
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	public static String byteArrayToDeviceModel(byte[] buf, int start, int end) {
		String result;
		String Temp_Str = "";

		try {
			StringBuilder sb = new StringBuilder();

			for (int i = start; i <= end; i++) {
				if (byteArrayToHexString(buf[i]).contains("00")) {
					break;
				}
				sb.append(String.format("%02X", buf[i] & 0xff));
			}

			byte[] bytes = Hex.decodeHex(sb.toString().toCharArray());
			result = new String(bytes, StandardCharsets.UTF_8);
			return result;
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	public static String byteArrayToMacAddress(byte[] buf, int start, int end) {
		StringBuilder sb = new StringBuilder();

		for (int i = start; i <= end; i++) {
			if (i == end) {
				sb.append(String.format("%02x", buf[i] & 0xff));
			} else {
				sb.append(String.format("%02x:", buf[i] & 0xff));
			}
		}

		return sb.toString();
	}

	public static String byteArrayToIPAddress(byte[] buf, int start, int end) {
		String Temp, result = "";
		int decimal;
		for (int i = start; i <= end; i++) {
			Temp = String.format("%02X", buf[i] & 0xff);
			decimal = Integer.parseInt(Temp, 16);
			if (i == end) {
				result += String.format("%s", decimal);
			} else {
				result += String.format("%s.", decimal);
			}

		}
		return result;
	}

	public static String byteArrayToIntegerString(byte[] buf, int start, int end) {
		String Temp, result = "";
		int decimal;
		for (int i = start; i <= end; i++) {
			Temp = String.format("%02X", buf[i] & 0xff);
			decimal = Integer.parseInt(Temp, 16);
			result += String.format("%s", decimal);
		}
		return result;
	}

	public static int byteToInteger(byte buf) {
		String Temp, result = "";
		int decimal;

		Temp = String.format("%02X", buf & 0xff);
		decimal = Integer.parseInt(Temp, 16);

		return decimal;
	}

	public static Integer HexByteToInteger(byte[] buf, int start, int end) {
		String Temp, result = "";
		int decimal;
		for (int i = start; i <= end; i++) {
			Temp = String.format("%02X", buf[i] & 0xff);
			decimal = Integer.parseInt(Temp, 16);
			result += String.format("%s", decimal);
		}
		return Integer.parseInt(result);
	}

	public static boolean HexByteToBoolean(byte[] buf, int start, int end) {
		String Temp, result = "";
		int decimal;
		for (int i = start; i <= end; i++) {
			Temp = String.format("%02X", buf[i] & 0xff);
			decimal = Integer.parseInt(Temp, 16);
			result += String.format("%s", decimal);
		}
		return Integer.parseInt(result) == 1;
	}

	public static byte[] hexStringToByte(String str) {
		byte[] bytes = new byte[str.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}

	public static void hexStringToByte(byte[] buf, String str, int start, int end) {
		byte[] bytes = new byte[str.length() / 2];

		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
			buf[start + i] = bytes[i];
		}
	}

	public static String byteArrayToHexString(byte[] bytes, int start, int end) {

		StringBuilder sb = new StringBuilder();

		for (int i = start; i < end; i++) {

			sb.append(String.format("%02X ", bytes[i] & 0xff));
		}

		return sb.toString();
	}

	public static String byteArrayToHexString1(byte[] bytes, int start, int end) {

		StringBuilder sb = new StringBuilder();

		for (int i = start; i < end; i++) {

			sb.append(bytes[i] & 0xff);
		}

		return sb.toString();
	}

	public static String byteArrayToHexString_Nonspace(byte[] bytes, int start, int end) {

		StringBuilder sb = new StringBuilder();

		for (int i = start; i < end; i++) {

			sb.append(String.format("%02X", bytes[i] & 0xff));
		}

		return sb.toString();
	}

	public static String byteArrayToHexString(byte[] bytes) {

		StringBuilder sb = new StringBuilder();

		for (byte b : bytes) {

			sb.append(String.format("%02X ", b & 0xff));
		}

		return sb.toString();
	}

	public static String byteArrayToHexString(byte bytes) {
		return String.format("%02X ", bytes & 0xff);
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static String stringToHex(String s) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < s.length(); i++) {
			result.append(String.format("%02X", (int) s.charAt(i)));
		}

		return result.toString();
	}

	public static String stringToHex(String s, int start, int end) {
		StringBuilder result = new StringBuilder();

		for (int i = start; i < end; i++) {
			result.append(String.format("%02X ", (int) s.charAt(i)));
		}

		return result.toString();
	}

	public static String IntegerToHexString(int data) {
		String Hex = Integer.toHexString(data);
		if (Hex.length() < 2) {
			Hex = "0" + Hex;
		}
		return Hex;
	}

	public static boolean validate(final String ip) {

		String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
		return ip.matches(PATTERN);
	}

    public static boolean MAC_validate(String password) {
	   Pattern pattern = Pattern.compile(PATTERN);
	   Matcher matcher = pattern.matcher(password);
	   return matcher.matches();

	}
	public static void Copy_byteArray(byte[] new_buf, byte[] old_buf, int offset) {
		System.arraycopy(old_buf, offset, new_buf, 0, new_buf.length);
	}

	public static String hexToAscii(String hexStr) {
		StringBuilder output = new StringBuilder("");

		for (int i = 0; i < hexStr.length(); i += 2) {
			String str = hexStr.substring(i, i + 2);
			output.append((char) Integer.parseInt(str, 16));
		}

		return output.toString();
	}



	public static void Delete_Map(String MAP_Name) {
		File file = new File("resources/MAP/MAP" + MAP_Name);
		if (file.exists()) {
			if (file.delete()) {
				System.out.println("suceess file delete"); /* BY INGU */
			} else {
				System.out.println("fail to remove file"); /* BY INGU */
			}
		}
	}



	public static String getModeltoString(int Model) {
		String Name = "";
		switch (Model) {
		case IPS24GFS:
			Name = "SC-IPS24GFS";
			break;
		case IPJ24M:
			Name = "SC-IPJ24M";
			break;
		case IPC0708H:
			Name = "SC-IPC0708H";
			break;
		case IPC0708HU:
			Name = "SC-IPC0708HU";
			break;
		case IPJ16M:
			Name = "SC-IPJ16M";
			break;
		case IPJ08M:
			Name = "SC-IPJ08M";
			break;
		case IPS24P:
			Name = "SC-IPS24P";
			break;
		case IPS16P:
			Name = "SC-IPS16P";
			break;
		case IPS08P:
			Name = "SC-IPS08P";
			break;
		case IPS24PL:
			Name = "SC-IPS24PL";
			break;
		case IPS24PL_8:
			Name = "SC-IPS24PL8";
			break;
		case IPS24PL_24:
			Name = "SC-IPS24PLD";
			break;
		case IPS16PL:
			Name = "SC-IPS16PL";
			break;
		case IPS16PL_16:
			Name = "SC-IPS16PLD";
			break;
		case IPS16PL_8:
			Name = "SC-IPS16PL8";
			break;
		case IPS08PL:
			Name = "SC-IPS08PL";
			break;
		}

		return Name;
	}



	public static void Write_Log(String ip, String content, String stat, String PortNum, String MAC) {
		try {

			TimeZone zone = TimeZone.getTimeZone("Asia/Seoul");
			Date date = new Date();
			DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			format.setTimeZone(zone);
			String currentTime = format.format(date);

			System.out.println("IP : " + ip + " Content : " + content); /* BY INGU */
			FileWriter writer = new FileWriter("resources/device/" + MAC + "/log.txt", true);

			writer.write(currentTime + "#" + ip + "#" + content + "#" + Get_ERRORStatus(Integer.parseInt(stat)) + "#"
					+ stat + "#" + PortNum + System.getProperty("line.separator"));

			writer.flush();
			writer.close();

			changeLog(MAC, "1");
			errorLog(MAC, "�߻�", content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	public static void Write_Log(String ip, String content, String MAC) {
		try {

			TimeZone zone = TimeZone.getTimeZone("Asia/Seoul");
			Date date = new Date();
			DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			format.setTimeZone(zone);
			String currentTime = format.format(date);

			System.out.println("IP : " + ip + " Content : " + content); /* BY INGU */
			FileWriter writer = new FileWriter("resources/device/" + MAC + "/log.txt", true);

			writer.write(currentTime + "#" + ip + "#" + content + "#" + Get_ERRORStatus(ERROR_NORMAL) + "#"
					+ ERROR_NORMAL + "#" + "9999" + System.getProperty("line.separator"));

			writer.flush();
			writer.close();

			changeLog(MAC, "1");
			errorLog(MAC, "�߻�", content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	public static void Write_Log(String ip, String content, String stat, String MAC) {
		try {

			TimeZone zone = TimeZone.getTimeZone("Asia/Seoul");
			Date date = new Date();
			DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			format.setTimeZone(zone);
			String currentTime = format.format(date);

			System.out.println("IP : " + ip + " Content : " + content); /* BY INGU */
			FileWriter writer = new FileWriter("resources/device/" + MAC + "/log.txt", true);

			writer.write(currentTime + "#" + ip + "#" + content + "#" + Get_ERRORStatus(Integer.parseInt(stat)) + "#"
					+ stat + "#" + "9999" + System.getProperty("line.separator"));

			writer.flush();
			writer.close();

			changeLog(MAC, "1");
			errorLog(MAC, "�߻�", content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	public static String Get_ERRORStatus(int stat) {
		String Str = "1";
		switch (stat) {
		case ERROR_PORTRESET:
		case ERROR_SLOTCHANGE:
		case ERROR_NORMAL:
		case ERROR_OK:
		case UDP_DM_REPONE:
		case ERROR_DISCONNECTED:
			Str = Integer.toString(ERROR_NORMAL);
			break;
		case ERROR_OVERCURRNET:
		case ERROR_OPEN:
		case ERROR_AUTOPOE:
		case ERROR_SHORT:
		case ERROR_EXTENDPORT_DOWN:
			Str = Integer.toString(ERROR_NOTCLEAR);
			break;
		default:
			Str = Integer.toString(ERROR_NOTCLEAR);
			break;
		}

		return Str;
	}

	private static void changeLog(String MAC, String chk) {
		try {
			FileWriter writer = new FileWriter("resources/device/" + MAC + "/temp.txt");
			writer.write(chk);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void errorLog(String MAC, String chk, String content) {
		try {
			System.out.println("Content: " + content);
			if (content.contains("System")) {
				FileWriter main = new FileWriter("resources/device/" + MAC + "/errMain.txt");
				main.write(chk + content + System.getProperty("line.separator"));
				main.flush();
				main.close();
			} else if (content.contains("PowerBoard1")) {
				FileWriter power1 = new FileWriter("resources/device/" + MAC + "/errPower1.txt");
				power1.write(chk + content + System.getProperty("line.separator"));
				power1.flush();
				power1.close();
			} else if (content.contains("PowerBoard2")) {
				FileWriter power2 = new FileWriter("resources/device/" + MAC + "/errPower2.txt");
				power2.write(chk + content + System.getProperty("line.separator"));
				power2.flush();
				power2.close();
			} else if ((content.contains("Group1") || content.contains("Slot1")) && !content.contains("OK")) {
				FileWriter slot1 = new FileWriter("resources/device/" + MAC + "/errSlot1.txt");
				slot1.write(chk + content);
				slot1.flush();
				slot1.close();
			} else if ((content.contains("Group2") || content.contains("Slot2")) && !content.contains("OK")) {
				FileWriter slot2 = new FileWriter("resources/device/" + MAC + "/errSlot2.txt");
				slot2.write(chk + content);
				slot2.flush();
				slot2.close();
			} else if ((content.contains("Group3") || content.contains("Slot3")) && !content.contains("OK")) {
				FileWriter slot3 = new FileWriter("resources/device/" + MAC + "/errSlot3.txt");
				slot3.write(chk + content);
				slot3.flush();
				slot3.close();
			} else if ((content.contains("Group4") || content.contains("Slot4")) && !content.contains("OK")) {
				FileWriter slot4 = new FileWriter("resources/device/" + MAC + "/errSlot4.txt");
				slot4.write(chk + content);
				slot4.flush();
				slot4.close();
			} else if ((content.contains("Group5") || content.contains("Slot5")) && !content.contains("OK")) {
				FileWriter slot5 = new FileWriter("resources/device/" + MAC + "/errSlot5.txt");
				slot5.write(chk + content);
				slot5.flush();
				slot5.close();
			} else if ((content.contains("Group6") || content.contains("Slot6")) && !content.contains("OK")) {
				FileWriter slot6 = new FileWriter("resources/device/" + MAC + "/errSlot6.txt");
				slot6.write(chk + content);
				slot6.flush();
				slot6.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String byteArrayToBinaryString(byte b) {
		return byteToBinaryString(b);
	}

	public static String byteArrayToBinaryString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (byte value : b) {
			sb.append(byteToBinaryString(value));
		}
		return sb.toString();
	}

	public static String byteToBinaryString(byte n) {
		StringBuilder sb = new StringBuilder("00000000");
		for (int bit = 0; bit < 8; bit++) {
			if (((n >> bit) & 1) > 0) {
				sb.setCharAt(7 - bit, '1');
			}
		}
		return sb.toString();
	}

	public static byte[] binaryStringToByteArray(String s) {
		int count = s.length() / 8;
		byte[] b = new byte[count];
		for (int i = 1; i < count; ++i) {
			String t = s.substring((i - 1) * 8, i * 8);
			b[i - 1] = binaryStringToByte(t);
		}
		return b;
	}

	public static byte binaryStringToByte(String s) {
		byte ret = 0, total = 0;
		for (int i = 0; i < 8; ++i) {
			ret = (s.charAt(7 - i) == '1') ? (byte) (1 << i) : 0;
			total = (byte) (ret | total);
		}
		return total;
	}

	public static String BinaryToString(char[] data, int start, int end) {
		String Result = "";
		for (int i = start; i < end; i++) {
			Result += Character.toString(data[i]);
		}
		return Result;
	}

	public static String nextIpAddress(final String input) {
		final String[] tokens = input.split("\\.");
		if (tokens.length != 4)
			throw new IllegalArgumentException();
		for (int i = tokens.length - 1; i >= 0; i--) {
			final int item = Integer.parseInt(tokens[i]);
			if (item < 255) {
				tokens[i] = String.valueOf(item + 1);
				for (int j = i + 1; j < 4; j++) {
					tokens[j] = "0";
				}
				break;
			}
		}
		return tokens[0] + '.' + tokens[1] + '.' + tokens[2] +
				'.' + tokens[3];
	}



//
//	public static void Write_Title_Exel(XSSFSheet sheet, String Title) {
//		XSSFRow curRow = sheet.getRow(0);
//
//		if (curRow != null) {
//			XSSFCell cell = curRow.getCell(0);
//			cell.setCellValue(Title);
//		}
//	}
//
//	public static int Write_Exel(int Count, XSSFSheet sheet, String Time, String IP, String Content) {
//		XSSFRow curRow;
//		XSSFCell cell;
//
//		curRow = sheet.getRow(Count);
//		if (curRow != null) {
//			cell = curRow.getCell(0);
//			cell.setCellValue(Integer.toString(Count - 2));
//			cell = curRow.getCell(1);
//			cell.setCellValue(Time);
//
//			cell = curRow.getCell(2);
//			cell.setCellValue(IP);
//
//			cell = curRow.getCell(3);
//			cell.setCellValue(Content);
//
//			Count += 1;
//		} else {
//			curRow = sheet.createRow(Count);
//
//			cell = curRow.createCell(0);
//			cell.setCellValue(Integer.toString(Count - 2));
//			cell = curRow.createCell(1);
//			cell.setCellValue(Time);
//
//			cell = curRow.createCell(2);
//			cell.setCellValue(IP);
//
//			cell = curRow.createCell(3);
//			cell.setCellValue(Content);
//
//			Count += 1;
//		}
//		return Count;
//
//	}

	public static int getSlot_Num(int index) {
		switch (index) {
		case 0:
		case 1:
		case 2:
		case 3:
			return 0;
		case 4:
		case 5:
		case 6:
		case 7:
			return 1;
		case 8:
		case 9:
		case 10:
		case 11:
			return 2;
		case 12:
		case 13:
		case 14:
		case 15:
			return 3;
		case 16:
		case 17:
		case 18:
		case 19:
			return 4;
		case 20:
		case 21:
		case 22:
		case 23:
			return 5;
		}
		return 0;
	}

	public static String getPortToString(int index) {
		switch (index) {
		case 0:
			return "A";
		case 1:
			return "B";
		case 2:
			return "C";
		case 3:
			return "D";
		case 4:
			return "A";
		case 5:
			return "B";
		case 6:
			return "C";
		case 7:
			return "D";
		case 8:
			return "A";
		case 9:
			return "B";
		case 10:
			return "C";
		case 11:
			return "D";
		case 12:
			return "A";
		case 13:
			return "B";
		case 14:
			return "C";
		case 15:
			return "D";
		case 16:
			return "A";
		case 17:
			return "B";
		case 18:
			return "C";
		case 19:
			return "D";
		case 20:
			return "A";
		case 21:
			return "B";
		case 22:
			return "C";
		case 23:
			return "D";
		}
		return "A";
	}

	public static String getPortToString(int index, int Device_Type) {
		if (Device_Type == IPS24GFS) {
			switch (index) {
			case 0:
				return "A";
			case 1:
				return "B";
			case 2:
				return "C";
			case 3:
				return "D";
			case 4:
				return "A";
			case 5:
				return "B";
			case 6:
				return "C";
			case 7:
				return "D";
			case 8:
				return "A";
			case 9:
				return "B";
			case 10:
				return "C";
			case 11:
				return "D";
			case 12:
				return "A";
			case 13:
				return "B";
			case 14:
				return "C";
			case 15:
				return "D";
			case 16:
				return "A";
			case 17:
				return "B";
			case 18:
				return "C";
			case 19:
				return "D";
			case 20:
				return "A";
			case 21:
				return "B";
			case 22:
				return "C";
			case 23:
				return "D";
			}
			return "A";
		} else {
			return Integer.toString(index + 1);
		}

	}

	public static String Get_Extend_Port_Name(int port_num, int Device_Type) {
		if (Device_Type == IPS24GFS) {
			switch (port_num) {
			case JIGU_Library.UPLINK01:
				return "NVR SIDE";
			case JIGU_Library.UPLINK02:
				return "SWITCH";
			case JIGU_Library.SFPPORT01:
				return "SFP Port";
			default:
				return "";
			}
		} else if (Device_Type == IPC0708H || Device_Type == IPC0708HU) {
			switch (port_num) {
			case JIGU_Library.UPLINK01:
				return "UPLink";
			case JIGU_Library.UPLINK02:
				return "UPLink";
			case JIGU_Library.SFPPORT01:
				return "SFP Port";
			default:
				return "";
			}
		} else if (Device_Type == IPJ24M || Device_Type == IPJ16M || Device_Type == IPS24P || Device_Type == IPS16P
				|| Device_Type == IPS24PL || Device_Type == IPS16PL || Device_Type == IPS24PL_24
				|| Device_Type == IPS24PL_8 || Device_Type == IPS16PL_16 || Device_Type == IPS16PL_8) {
			switch (port_num) {
			case JIGU_Library.UPLINK01:
				return "UPLink 01";
			case JIGU_Library.UPLINK02:
				return "UPLink 02";
			case JIGU_Library.SFPPORT01:
				return "SFP Port";
			case JIGU_Library.SFPPORT02:
				return "SFP Port";
			default:
				return "";
			}
		} else if (Device_Type == IPJ08M || Device_Type == IPS08P || Device_Type == IPS08PL) {
			switch (port_num) {
			case JIGU_Library.UPLINK01:
				return "UPLink 02";
			case JIGU_Library.UPLINK02:
				return "UPLink 01";
			case JIGU_Library.SFPPORT01:
				return "SFP Port";
			case JIGU_Library.SFPPORT02:
				return "SFP Port";
			default:
				return "";
			}
		} else {
			switch (port_num) {
			case JIGU_Library.UPLINK01:
				return "UPLink 01";
			case JIGU_Library.UPLINK02:
				return "UPLink 02";
			case JIGU_Library.SFPPORT01:
				return "SFP Port 01";
			case JIGU_Library.SFPPORT02:
				return "SFP Port 02";
			default:
				return "";
			}
		}

	}

	public static String Get_Slot_Port(int port_num, int Device_Type, boolean ldphy) {
		if (Device_Type == IPS24GFS) {
			int Slot_Num = JIGU_Library.getSlot_Num(port_num);
			int Slot_Port = port_num - (Slot_Num * 4);

			return "Slot " + String.format("%02d", Slot_Num + 1) + " - "
					+ JIGU_Library.getPortToString(Slot_Port);
		} else if (Device_Type == IPC0708H || Device_Type == IPC0708HU) {
			if (ldphy) {
				return "LD CH " + String.format("%02d", port_num + 1);
			} else {
				return "CH " + String.format("%02d", port_num + 1);
			}
		} else {
			if (ldphy) {
				return "LD Port " + String.format("%02d", port_num + 1);
			} else {
				return "Port " + String.format("%02d", port_num + 1);
			}
		}
	}
	
	public static String Get_Slot_Port(int port_num, int Device_Type) {
		if (Device_Type == IPS24GFS) {
			int Slot_Num = JIGU_Library.getSlot_Num(port_num);
			int Slot_Port = port_num - (Slot_Num * 4);

			return "Slot " + String.format("%02d", Slot_Num + 1) + " - "
					+ JIGU_Library.getPortToString(Slot_Port);
		} else if (Device_Type == IPC0708H || Device_Type == IPC0708HU) {
			return "CH " + String.format("%02d", port_num + 1);
		} else {
			return "Port " + String.format("%02d", port_num + 1);
		}
	}

	public static String Get_Error_type(int stat) {
		switch (stat) {
		case ERROR_OPEN:
			return "Open";
		case ERROR_SHORT:
			return "Short";
		case ERROR_OVERCURRNET:
			return "OverCurrent";
		case ERROR_AUTOPOE:
			return "Low Bitrate";
		case ERROR_EXTENDPORT_DOWN:
			return "LINK Down";
		default:
			return "";
		}
	}

	public static boolean Get_Ping_Result(String IP) {
		boolean isAlive = false;
		try {
			InetAddress pingcheck = InetAddress.getByName(IP);
			isAlive = pingcheck.isReachable(1000);
			return isAlive;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isAlive;
	}

	public static String Get_System_Usage_Time(String Year, String Month, String Day, String Hour, String Min,
			String Sec) {
		if (Integer.parseInt(Year) == 0 && Integer.parseInt(Month) == 0 && Integer.parseInt(Day) == 0) {
			return String.format("%02d", Integer.parseInt(Hour)) + ":" + String.format("%02d", Integer.parseInt(Min))
					+ ":" + String.format("%02d", Integer.parseInt(Sec));
		} else if (Integer.parseInt(Year) == 0 && Integer.parseInt(Month) == 0) {
			return String.format("%02d", Integer.parseInt(Day)) + " Day "
					+ String.format("%02d", Integer.parseInt(Hour)) + ":" + String.format("%02d", Integer.parseInt(Min))
					+ ":" + String.format("%02d", Integer.parseInt(Sec));
		} else if (Integer.parseInt(Year) == 0) {
			return String.format("%02d", Integer.parseInt(Month)) + " Month "
					+ String.format("%02d", Integer.parseInt(Day)) + " Day "
					+ String.format("%02d", Integer.parseInt(Hour)) + ":" + String.format("%02d", Integer.parseInt(Min))
					+ ":" + String.format("%02d", Integer.parseInt(Sec));
		} else {
			return String.format("%02d", Integer.parseInt(Year)) + " Year "
					+ String.format("%02d", Integer.parseInt(Month)) + " Month "
					+ String.format("%02d", Integer.parseInt(Day)) + " Day "
					+ String.format("%02d", Integer.parseInt(Hour)) + ":" + String.format("%02d", Integer.parseInt(Min))
					+ ":" + String.format("%02d", Integer.parseInt(Sec));
		}
	}

	public static String Get_System_Boot_Time(String Year, String Month, String Day, String Hour, String Min,
			String Sec) {
		long Day_tmp = (Integer.parseInt(Year) * 365) + (Integer.parseInt(Month) * 30) + Integer.parseInt(Day);
		long Hour_tmp = (Day_tmp * 24) + Integer.parseInt(Hour);
		long Min_tmp = (Hour_tmp * 60) + Integer.parseInt(Min);
		long Sec_tmp = (Min_tmp * 60) + Integer.parseInt(Sec);
		long mili_sec = Sec_tmp * 1000;
		long time1 = System.currentTimeMillis() - mili_sec;
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String format_time1 = format1.format(time1);
		return format_time1;
	}
}
