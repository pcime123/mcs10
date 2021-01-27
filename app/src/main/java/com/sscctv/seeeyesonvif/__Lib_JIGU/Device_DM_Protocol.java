package com.sscctv.seeeyesonvif.__Lib_JIGU;


import android.util.Log;

public class Device_DM_Protocol {
    public static final String TAG = "Device_DM_Protocol";

    final static byte Battery_Normal = 0x00;
    final static byte Battery_Short = 0x01;
    final static byte System_Overtage = 0x02;
    final static byte System_Overtage_Protection = 0x04;
    final static byte Input_Overtage = 0x08;
    final static byte Input_Overtage_Protection = 0x10;
    final static byte Battery_Fault = 0x40;
    final static byte LED_ON = 0x01;
    final static byte LED_OFF = 0x00;

    final static byte EM_Lock_Stat = 8;
    final static byte EM_Lock_WHY = EM_Lock_Stat + 1;
    final static byte Main_Power_Stat = EM_Lock_WHY + 1;
    final static byte EME_Power_Stat = Main_Power_Stat + 1;
    final static byte FIRE_Detect_Stat = EME_Power_Stat + 1;
    final static byte EME_Button_Stat = FIRE_Detect_Stat + 1;
    final static byte EM_Line_Stat = EME_Button_Stat + 1;
    final static byte Battery_Size = EM_Line_Stat + 1;
    final static byte ALARM_1 = Battery_Size + 1;
    final static byte ALARM_2 = ALARM_1 + 1;
    final static byte EMLOCK_Mode = ALARM_2 + 1;
    final static byte EMLOCK_Time = EMLOCK_Mode + 1;
    final static byte Siren_Time = EMLOCK_Time + 1;
    final static byte Device_Stat = Siren_Time + 1;
    final static byte EME_CALL_Stat = Device_Stat + 1;
    final static byte SIREN_Stat = EME_CALL_Stat + 1;
    final static byte Camera_IP_Start = SIREN_Stat + 1;
    final static byte Camera_IP_End = Camera_IP_Start + 3;

    //
    public String process_EDAL_Data(byte[] Data, Device_EDAL Device) {
        String Str = "";
        // System.out.println("process_EDAL_Data"); /* BY INGU */
        // System.out.println(DeviceManagement_Library.byteArrayToHexString(Data, 0,
        // Data.length)); /* BY INGU */

        if (Data[EM_Lock_Stat] == 0x01) {
            if (!Device.EDAL_Status) {
                if (Data[EM_Lock_WHY] != 1) {
                    Str = Get_EM_LOCK_Why(Data[EM_Lock_WHY]) + "EM LOCK이 열렸습니다.";
//                    DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                            Integer.toString(Controller_EMLOCK_Log.PRIORITY_NORMAL),
//                            Integer.toString(DeviceManagement_Library.EDAL_EVENT_GATE_CLOSE), Device.Device.getDevice_Mac(),
//                            Device);
//                    Device.EDAL_New_Alarm = true;
                }
            }
            Device.EDAL_Status = true;
            // System.out.println("EM LOCK OPEN"); /* BY INGU */
        } else {
            if (Device.EDAL_Status) {
                Str = "EM LOCK이 닫혔습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_NORMAL),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_GATE_CLOSE), Device.Device.getDevice_Mac(),
//                        Device);
//                Device.EDAL_New_Alarm = true;
            }
            Device.EDAL_Status = false;
            // System.out.println("EM LOCK CLOSE"); /* BY INGU */
        }
        Log.d(TAG, "EDAL_Status: " + Str);


        if (Data[Main_Power_Stat] == 0x01) {
            if (!Device.Main_Power_Status) {
                Str = "상용전원이 연결되었습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_NORMAL),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_MAIN_POWER), Device.Device.getDevice_Mac(),
//                        Device);
//                Device.EDAL_New_Alarm = true;

            }
            Device.Main_Power_Status = true;
            // System.out.println("Main POWER ON"); /* BY INGU */
        } else {
            if (Device.Main_Power_Status) {
                Str = "상용전원에 문제가 발생하였습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_MAIN_POWER), Device.Device.getDevice_Mac(),
//                        Device);
//                Device.EDAL_MainPower_Error = true;
//                Device.EDAL_New_Alarm = true;
//                Device.EDAL_Audio_Alarm = true;
            }
            Device.Main_Power_Status = false;
            // System.out.println("Main POWER OFF"); /* BY INGU */
        }
        Log.d(TAG, "Main_Power_Status: " + Str);


        if (Data[EME_Power_Stat] == Battery_Normal) {
            Device.Battery_Status = true;
            Device.Battery_Short = false;
            Device.Battery_System_Overtage = false;
            Device.Battery_System_Overtage_Protection = false;
            Device.Battery_Input_Overtage = false;
            Device.Battery_Input_Overtage_Protection = false;
            Device.Battery_Fault = false;
            Device.Battery_Charging = false;
            Str = "배터리 상태 좋음";

        } else {
            String Result = JIGU_Library.byteToBinaryString(Data[EME_Power_Stat]);

            Device.Battery_Status = Data[EME_Power_Stat] == 0x20;

            if (Result.charAt(7) == '1') {
                if (!Device.Battery_Short) {
                    Str = "배터리 SHORT 발생";
//                    DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                            Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                            Integer.toString(DeviceManagement_Library.EDAL_EVENT_BATTERY),
//                            Device.Device.getDevice_Mac(), Device);
//                    Device.EDAL_Battery_Error = true;
//                    Device.EDAL_New_Alarm = true;
//                    Device.EDAL_Audio_Alarm = true;
                }
                Device.Battery_Short = true;

            } else {
                Device.Battery_Short = false;
            }

            if (Result.charAt(6) == '1') {
                if (!Device.Battery_System_Overtage) {
                    Str = "배터리 System OverVoltage 발생";
//                    DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                            Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                            Integer.toString(DeviceManagement_Library.EDAL_EVENT_BATTERY),
//                            Device.Device.getDevice_Mac(), Device);
//                    Device.EDAL_Battery_Error = true;
//                    Device.EDAL_New_Alarm = true;
//                    Device.EDAL_Audio_Alarm = true;
                }
                Device.Battery_System_Overtage = true;
            } else {
                Device.Battery_System_Overtage = false;
            }

            if (Result.charAt(5) == '1') {
                if (!Device.Battery_System_Overtage_Protection) {
                    Str = "배터리 System Overtage Protection 발생";
//                    DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                            Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                            Integer.toString(DeviceManagement_Library.EDAL_EVENT_BATTERY),
//                            Device.Device.getDevice_Mac(), Device);
//                    Device.EDAL_Battery_Error = true;
//                    Device.EDAL_New_Alarm = true;
//                    Device.EDAL_Audio_Alarm = true;
                }
                Device.Battery_System_Overtage_Protection = true;
            } else {
                Device.Battery_System_Overtage_Protection = false;
            }

            if (Result.charAt(4) == '1') {
                if (!Device.Battery_Input_Overtage) {
                    Str = "배터리 Battery Input Overtage 발생";
//                    DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                            Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                            Integer.toString(DeviceManagement_Library.EDAL_EVENT_BATTERY),
//                            Device.Device.getDevice_Mac(), Device);
//                    Device.EDAL_Battery_Error = true;
//                    Device.EDAL_New_Alarm = true;
//                    Device.EDAL_Audio_Alarm = true;
                }
                Device.Battery_Input_Overtage = true;
            } else {
                Device.Battery_Input_Overtage = false;
            }

            if (Result.charAt(3) == '1') {
                if (!Device.Battery_Input_Overtage_Protection) {
                    Str = "배터리 Input Overtage Protection 발생";
//                    DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                            Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                            Integer.toString(DeviceManagement_Library.EDAL_EVENT_BATTERY),
//                            Device.Device.getDevice_Mac(), Device);
//                    Device.EDAL_Battery_Error = true;
//                    Device.EDAL_New_Alarm = true;
//                    Device.EDAL_Audio_Alarm = true;
                }
                Device.Battery_Input_Overtage_Protection = true;
            } else {
                Device.Battery_Input_Overtage_Protection = false;
            }

            if (Result.charAt(2) == '1') {
                if (!Device.Battery_Charging) {
                    Str = "배터리 충전";
//                    DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                            Integer.toString(Controller_EMLOCK_Log.PRIORITY_NORMAL),
//                            Integer.toString(DeviceManagement_Library.EDAL_EVENT_BATTERY),
//                            Device.Device.getDevice_Mac(), Device);
//                    Device.EDAL_New_Alarm = true;

                }
                Device.Battery_Charging = true;
            } else {
                Device.Battery_Charging = false;
            }

            if (Result.charAt(1) == '1') {
                if (!Device.Battery_Fault) {
                    Str = "배터리 상태 불량 ";
//                    DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                            Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                            Integer.toString(DeviceManagement_Library.EDAL_EVENT_BATTERY),
//                            Device.Device.getDevice_Mac(), Device);
//                    Device.EDAL_Battery_Error = true;
//                    Device.EDAL_New_Alarm = true;
//                    Device.EDAL_Audio_Alarm = true;
                }
                Device.Battery_Fault = true;
            } else {
                Device.Battery_Fault = false;
            }

        }
        Log.d(TAG, "Battery: " + Str);


        if (Data[FIRE_Detect_Stat] == 0x00) {
            if (!Device.Fire_Detect) {
                Str = "화재가 감지되었습니다. ";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_FIRE), Device.Device.getDevice_Mac(),
//                        Device);
//                Device.EDAL_Fire_Error = true;
//                Device.EDAL_New_Alarm = true;
//                Device.EDAL_EME_Audio_Alarm = true;
            }
            Device.Fire_Detect = true;
            // System.out.println("Fire Detect"); /* BY INGU */
        } else {
            Device.Fire_Detect = false;
            // System.out.println("NOT Fire Detect"); /* BY INGU */
        }

        Log.d(TAG, "Fire_Detect: " + Str);


        if (Data[EME_Button_Stat] == 0x00) {
            if (!Device.EME_Button_Detect) {
                Str = "비상 버튼이 눌렸습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_EME_BUTTON), Device.Device.getDevice_Mac(),
//                        Device);
//                Device.EDAL_EME_BTN_Error = true;
//                Device.EDAL_New_Alarm = true;
//                Device.EDAL_EME_Audio_Alarm = true;
            }
            Device.EME_Button_Detect = true;
            // System.out.println("EME BUTTON Detect"); /* BY INGU */
        } else {
            Device.EME_Button_Detect = false;
            // System.out.println("NOT EME BUTTON Detect"); /* BY INGU */
        }
        Log.d(TAG, "EME_Button_Detect: " + Str);

        if (Data[EM_Line_Stat] == 0x01) {

            Device.LINE_Status = true;
            // System.out.println("LINE NORMAL"); /* BY INGU */
        } else {
            if (Device.LINE_Status) {
                Str = "단선이 감지되었습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_LINE), Device.Device.getDevice_Mac(),
//                        Device);
//                Device.EDAL_LINE_Error = true;
//                Device.EDAL_New_Alarm = true;
//                Device.EDAL_Audio_Alarm = true;
            }
            Device.LINE_Status = false;
            // System.out.println("LINE ERROR"); /* BY INGU */
        }
        Log.d(TAG, "LINE_Status: " + Str);


        Device.Battery_Size = Data[Battery_Size];
        Log.d(TAG, "Battery_Size: " + Data[14]);

        // System.out.println("Battery : " + Data[14]); /* BY INGU */

        if (Data[ALARM_1] == 0x00) {
            if (!Device.Alarm_01_Detect) {
                Str =
//                        Device.Device.EDAL_Alarm01_Name +
                        " 에서 알람이 발생하였습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_EXTERNAL_ALARM01),
//                        Device.Device.getDevice_Mac(), Device);
//                Device.EDAL_External_Alarm01_Error = true;
//                Device.EDAL_New_Alarm = true;
//                Device.EDAL_EME_Audio_Alarm = true;
            }
            Device.Alarm_01_Detect = true;
            // System.out.println("Alarm 01 Detect"); /* BY INGU */
        } else {
            Device.Alarm_01_Detect = false;
            // System.out.println("NOT Alarm 01 Detect"); /* BY INGU */
        }

        Log.d(TAG, "Alarm_01_Detect: " + Str);


        if (Data[ALARM_2] == 0x00) {
            if (!Device.Alarm_02_Detect) {
                Str =
//                        Device.Device.EDAL_Alarm02_Name +
                        " 에서 알람이 발생하였습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_EXTERNAL_ALARM02),
//                        Device.Device.getDevice_Mac(), Device);
//                Device.EDAL_External_Alarm02_Error = true;
//                Device.EDAL_New_Alarm = true;
//                Device.EDAL_EME_Audio_Alarm = true;
            }
            Device.Alarm_02_Detect = true;
            // System.out.println("Alarm 02 Detect"); /* BY INGU */
        } else {
            Device.Alarm_02_Detect = false;
            // System.out.println("NOT Alarm 02 Detect"); /* BY INGU */
        }
        Log.d(TAG, "Alarm_02_Detect: " + Str);

        if (Data[EMLOCK_Mode] == 0x01) {
//			if(Device.EMLOCK_Mode == 0){
//				Str = "Mode가 변경되었습니다. 수동 -> 자동 ";
//				DeviceManagement_Library
//				.Write_EDAL_Log(Device.Device.Device_ip, Str, Integer.toString(Controller_EMLOCK_Log.PRIORITY_NORMAL),
//														 Integer.toString(DeviceManagement_Library.EDAL_EVENT_CONFIG),
//														 Device.Device.getDevice_Mac());
//			}
            Device.EMLOCK_Mode = 1;

            // System.out.println("Mode Auto"); /* BY INGU */
        } else {
//			if(Device.EMLOCK_Mode == 1){
//				Str = "Mode가 변경되었습니다. 수동 -> 자동 ";
//				DeviceManagement_Library
//				.Write_EDAL_Log(Device.Device.Device_ip, Str, Integer.toString(Controller_EMLOCK_Log.PRIORITY_NORMAL),
//														 Integer.toString(DeviceManagement_Library.EDAL_EVENT_CONFIG),
//														 Device.Device.getDevice_Mac());
//			}
            Device.EMLOCK_Mode = 0;
            // System.out.println("Mode Passive"); /* BY INGU */
        }

        Log.d(TAG, "EMLOCK_Mode: " + EMLOCK_Mode);


        Device.EMLOCK_Time = Data[EMLOCK_Time];
        Log.d(TAG, "EMLOCK_Time: " + Data[18]);

        // System.out.println("EMLOCK TIME : " + Data[18]); /* BY INGU */

        Device.Siren_Time = Data[Siren_Time];
        if (Device.Siren_Time == 0) {
            Device.Siren_Mode = true;
        } else {
            Device.Siren_Mode = false;
        }
        // System.out.println("SIREN TIME : " + Data[19]); /* BY INGU */

        if (Data[Device_Stat] == 0x01) {
            if (!Device.Case_Stat) {
//                Str = "외함 열림이 감지되었습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_CASE_OPEN), Device.Device.getDevice_Mac(),
//                        Device);
//                Device.EDAL_CASE_Error = true;
//                Device.EDAL_New_Alarm = true;
//                Device.EDAL_Audio_Alarm = true;
            }
            Device.Case_Stat = true;
        } else {
            if (Device.Case_Stat) {
//                Str = "외함이 닫혔습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_NORMAL),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_CASE_CLOSE), Device.Device.getDevice_Mac(),
//                        Device);
//                Device.EDAL_New_Alarm = true;
            }
            Device.Case_Stat = false;
        }

        if (Data[EME_CALL_Stat] == 0x00) {
            if (!Device.EME_Call_Stat) {
//                Str = "비상통화 이벤트가 감지되었습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_DANGER),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_EME_CALL), Device.Device.getDevice_Mac(),
//                        Device);
//                Device.EDAL_EME_CALL_Error = true;
//                Device.EDAL_New_Alarm = true;
//                Device.EDAL_EME_CALL_Alarm = true;
//                if (main != null) {
//                    main.Emergency_Call_Detect(Device);
//                }
            }
            Device.EME_Call_Stat = true;
        } else {
            if (Device.EME_Call_Stat) {

//                Device.EDAL_EME_CALL_Error = false;
//                Device.EDAL_New_Alarm = true;
//
//                if (main != null) {
//                    main.Remove_Emergency_Call(Device);
//                }
//
//                Str = "비상통화 이벤트가 종료되었습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_NORMAL),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_EME_CALL), Device.Device.getDevice_Mac(),
//                        Device);
            }
            Device.EME_Call_Stat = false;
        }

        if (Data[SIREN_Stat] == 0x01) {

            Device.Siren_Stat = true;
        } else {
            if (Device.Siren_Stat) {
                Str = "경보가 해제 되었습니다.";
//                DeviceManagement_Library.Write_EDAL_Log(Device.Device.Device_ip, Str,
//                        Integer.toString(Controller_EMLOCK_Log.PRIORITY_NORMAL),
//                        Integer.toString(DeviceManagement_Library.EDAL_EVENT_ALARM_DISABLE),
//                        Device.Device.getDevice_Mac(), Device);
//                Device.EDAL_New_Alarm = true;
            }
            Device.Siren_Stat = false;
        }

        // System.out.println("CASE STAT : " + Data[20]); /* BY INGU */
        System.out.println(JIGU_Library.byteArrayToIPAddress(Data, Camera_IP_Start, Camera_IP_End)); /* BY INGU */

        return JIGU_Library.byteArrayToIPAddress(Data, Camera_IP_Start, Camera_IP_End);
    }
//
//	public static void process_DM_Data(byte[] Data, DeviceManagement_DeviceThread Device,
//			Controller_HOME_MAP HOME_MAP) {
//		byte[] PortData = new byte[Device.Device.Max_Port_Count * 5];
//		byte[] ExtendPort = new byte[Device.getExtend_PortCount() * 6];
//		String LD_INFO01, LD_INFO02, LD_INFO03;
//		for (int i = 0; i < PortData.length; i++) {
//			PortData[i] = Data[13 + i];
//		}
//
//		int Extend_Start = 13 + PortData.length;
//		for (int i = 0; i < ExtendPort.length; i++) {
//			ExtendPort[i] = Data[Extend_Start + i];
//		}
//
//		Device.Internal_Temp = Integer.parseInt(DeviceManagement_Library.byteArrayToIntegerString(Data, 8, 8));
//		Device.Fan_Stat = Integer.parseInt(DeviceManagement_Library.byteArrayToIntegerString(Data, 9, 9));
//
//		LD_INFO01 = DeviceManagement_Library.byteArrayToBinaryString(Data[10]);
//		LD_INFO02 = DeviceManagement_Library.byteArrayToBinaryString(Data[11]);
//		LD_INFO03 = DeviceManagement_Library.byteArrayToBinaryString(Data[12]);
//
//		for (int i = 0; i < Device.Get_Max_Port_Count(); i++) {
//			if (i < 8) {
//				if (LD_INFO03.charAt(7 - i) == '0') {
//					Device.Get_Device_Port(i).Set_Is_LDPHY(false);
//				} else {
//					Device.Get_Device_Port(i).Set_Is_LDPHY(true);
//				}
//			} else if (i < 16) {
//				int tmp_index = i - 8;
//				if (LD_INFO02.charAt(7 - tmp_index) == '0') {
//					Device.Get_Device_Port(i).Set_Is_LDPHY(false);
//				} else {
//					Device.Get_Device_Port(i).Set_Is_LDPHY(true);
//				}
//			} else if (i < 24) {
//				int tmp_index = i - 16;
//				if (LD_INFO01.charAt(7 - tmp_index) == '0') {
//					Device.Get_Device_Port(i).Set_Is_LDPHY(false);
//				} else {
//					Device.Get_Device_Port(i).Set_Is_LDPHY(true);
//				}
//			}
//		}
//
//		for (int i = 0; i < Device.Device.Max_Port_Count; i++) {
//			String DMDP01, DMDP02, DMDP03, DMDP04, DMDP05;
//			DMDP01 = DeviceManagement_Library.byteArrayToBinaryString(PortData[i * 5 + 0]);
//			DMDP02 = DeviceManagement_Library.byteArrayToBinaryString(PortData[i * 5 + 1]);
//			DMDP03 = DeviceManagement_Library.byteArrayToBinaryString(PortData[i * 5 + 2]);
//			DMDP04 = DeviceManagement_Library.byteArrayToBinaryString(PortData[i * 5 + 3]);
//			DMDP05 = DeviceManagement_Library.byteArrayToBinaryString(PortData[i * 5 + 4]);
//
//			// System.out.println("NUM : " + (i+1)); /* BY INGU */
//
//			Process_DMDP01(DMDP01, Device, i, HOME_MAP);
//			Process_Currnet(DMDP02, DMDP03, Device, i);
//			Process_Bitrate(DMDP03, DMDP04, DMDP05, Device, i);
//		}
//
//		for (int i = 0; i < Device.getExtend_PortCount(); i++) {
//			String DMDE01, DMDE02, DMDE03, DMDE04, DMDE05, DMDE06;
//			DMDE01 = DeviceManagement_Library.byteArrayToBinaryString(ExtendPort[i * 6 + 0]);
//			DMDE02 = DeviceManagement_Library.byteArrayToBinaryString(ExtendPort[i * 6 + 1]);
//			DMDE03 = DeviceManagement_Library.byteArrayToBinaryString(ExtendPort[i * 6 + 2]);
//			DMDE04 = DeviceManagement_Library.byteArrayToBinaryString(ExtendPort[i * 6 + 3]);
//			DMDE05 = DeviceManagement_Library.byteArrayToBinaryString(ExtendPort[i * 6 + 4]);
//			DMDE06 = DeviceManagement_Library.byteArrayToBinaryString(ExtendPort[i * 6 + 5]);
//			/*
//			 * if(i == 2) {
//			 * System.out.println(DeviceManagement_Library.byteArrayToHexString(ExtendPort[i
//			 * *6+0]) + " " +
//			 * DeviceManagement_Library.byteArrayToHexString(ExtendPort[i*6+1]) + " " +
//			 * DeviceManagement_Library.byteArrayToHexString(ExtendPort[i*6+2]) + " " +
//			 * DeviceManagement_Library.byteArrayToHexString(ExtendPort[i*6+3]) + " " +
//			 * DeviceManagement_Library.byteArrayToHexString(ExtendPort[i*6+4]) + " " +
//			 * DeviceManagement_Library.byteArrayToHexString(ExtendPort[i*6+5])); }
//			 */
//			Process_DMDE01(DMDE01, Device, i);
//			Process_Bitrate_in(DMDE02, DMDE03, DMDE04, Device, i);
//			Process_Bitrate_out(DMDE02, DMDE05, DMDE06, Device, i);
//		}
//	}
//
//	private static void Process_DMDE01(String Data, DeviceManagement_DeviceThread Device, int port_num) {
//		boolean link_stat;
//		int speed;
//		if (Data.charAt(7) == '1') {
//			link_stat = true;
//			// System.out.println("Link : True / " + port_num); /* BY INGU */
//		} else {
//			link_stat = false;
//			// System.out.println("Link : False / " + port_num); /* BY INGU */
//		}
//		speed = Integer.parseInt(DeviceManagement_Library.BinaryToString(Data.toCharArray(), 5, 7), 2);
//		if (Device != null) {
//			if (link_stat == false) {
//				if (Device.MainPort_Area.get(port_num).Port_Link) {
//					if (Device.ExtendPort_Error[port_num].Add_Error(DeviceManagement_Library.ERROR_EXTENDPORT_DOWN,
//							"")) {
//						String Str = DeviceManagement_Library.Get_Extend_Port_Name(
//								Device.MainPort_Area.get(port_num).port_kind, Device.getModel_Index()) + " LINK Down";
//						DeviceManagement_Library
//								.Write_Log(Device.Device.Device_ip, Str,
//										Integer.toString(DeviceManagement_Library.ERROR_EXTENDPORT_DOWN),
//										Integer.toString(DeviceManagement_Library.ALARM_EXTENDPORT_NUM
//												+ Device.MainPort_Area.get(port_num).port_kind),
//										Device.Device.getDevice_Mac());
//						Device.New_Alarm = true;
//					}
//				}
//			} else if (link_stat) {
//				if (Device.MainPort_Area.get(port_num).Port_Link == false) {
//					Device.ExtendPort_Error[port_num].ExtendPort_Enable();
//
//					String Str = DeviceManagement_Library.Get_Extend_Port_Name(
//							Device.MainPort_Area.get(port_num).port_kind, Device.getModel_Index()) + " LINK UP";
//
//					DeviceManagement_Library
//							.Write_Log(Device.Device.Device_ip, Str,
//									Integer.toString(DeviceManagement_Library.ERROR_OK),
//									Integer.toString(DeviceManagement_Library.ALARM_EXTENDPORT_NUM
//											+ Device.MainPort_Area.get(port_num).port_kind),
//									Device.Device.getDevice_Mac());
//					Device.New_Alarm = true;
//
//				}
//			}
//			Device.MainPort_Area.get(port_num).Port_Link = link_stat;
//			Device.MainPort_Area.get(port_num).Speed = speed;
//
//		}
//	}
//
//	private static void Process_Bitrate_in(String DMDE02, String DMDE03, String DMDE04,
//			DeviceManagement_DeviceThread Device, int index) {
//		int Bitrate;
//		String val;
//
//		val = DeviceManagement_Library.BinaryToString(DMDE02.toCharArray(), 4, 8)
//				+ DeviceManagement_Library.BinaryToString(DMDE04.toCharArray(), 0, 8)
//				+ DeviceManagement_Library.BinaryToString(DMDE03.toCharArray(), 0, 8);
//
//		Bitrate = Integer.parseInt(val, 2);
//
//		if (Device != null) {
//			Device.MainPort_Area.get(index).setBitrate_In(Bitrate);
//		}
//	}
//
//	private static void Process_Bitrate_out(String DMDE02, String DMDE05, String DMDE06,
//			DeviceManagement_DeviceThread Device, int index) {
//		int Bitrate;
//		String val;
//		val = DeviceManagement_Library.BinaryToString(DMDE02.toCharArray(), 0, 4)
//				+ DeviceManagement_Library.BinaryToString(DMDE06.toCharArray(), 0, 8)
//				+ DeviceManagement_Library.BinaryToString(DMDE05.toCharArray(), 0, 8);
//
//		Bitrate = Integer.parseInt(val, 2);
//		if (Device != null) {
//			Device.MainPort_Area.get(index).setBitrate_Out(Bitrate);
//		}
//	}
//
//	private static void Process_DMDP01(String Data, DeviceManagement_DeviceThread Device, int port_num,
//			Controller_HOME_MAP HOME_MAP) {
//		boolean link_stat;
//		boolean auto_Nego;
//		int speed;
//		int pwr_stat;
//		boolean EnPwr;
//		String val;
//
//		if (Data.charAt(0) == '1') {
//			link_stat = true;
//		} else {
//			link_stat = false;
//		}
//
//		if (Data.charAt(1) == '1') {
//			auto_Nego = true;
//		} else {
//			auto_Nego = false;
//		}
//
//		val = Character.toString(Data.charAt(2)) + Character.toString(Data.charAt(3));
//		speed = Integer.parseInt(val, 2);
//
//		val = Character.toString(Data.charAt(4)) + Character.toString(Data.charAt(5))
//				+ Character.toString(Data.charAt(6));
//		pwr_stat = Integer.parseInt(val, 2);
//
//		if (Data.charAt(7) == '1') {
//			EnPwr = true;
//		} else {
//			EnPwr = false;
//		}
//
//		if (Device != null) {
//			String Str;
//			DeviceManagement_Device_Port Port = Device.Get_Device_Port(port_num);
//			if (link_stat == false) {
//				if (Port.Link_Stat) {
//					if (Device.Get_Device_Port(port_num).is_ldphy) {
//						Str = DeviceManagement_Library.Get_Slot_Port(port_num, Device.getModel_Index(), true)
//								+ " LINK Down";
//					} else {
//						Str = DeviceManagement_Library.Get_Slot_Port(port_num, Device.getModel_Index(),
//								Device.Get_Device_Port(port_num).is_ldphy_OLD) + " LINK Down";
//					}
//					Device.Get_Device_Port(port_num).Set_Is_LDPHY_OLD(Device.Get_Device_Port(port_num).is_ldphy);
//					DeviceManagement_Library.Write_Log(Device.Device.Device_ip, Str,
//							Integer.toString(DeviceManagement_Library.ERROR_NORMAL), Integer.toString(port_num),
//							Device.Device.getDevice_Mac());
//					Device.New_Alarm = true;
//				}
//			} else if (link_stat) {
//				if (Port.Link_Stat == false) {
//					if (Device.Get_Device_Port(port_num).is_ldphy) {
//						Str = DeviceManagement_Library.Get_Slot_Port(port_num, Device.getModel_Index(), true)
//								+ " LINK UP";
//					} else {
//						Str = DeviceManagement_Library.Get_Slot_Port(port_num, Device.getModel_Index(),
//								Device.Get_Device_Port(port_num).is_ldphy_OLD) + " LINK UP";
//					}
//					Device.Get_Device_Port(port_num).Set_Is_LDPHY_OLD(Device.Get_Device_Port(port_num).is_ldphy);
//					DeviceManagement_Library.Write_Log(Device.Device.Device_ip, Str,
//							Integer.toString(DeviceManagement_Library.ERROR_NORMAL), Integer.toString(port_num),
//							Device.Device.getDevice_Mac());
//					Device.New_Alarm = true;
//				}
//			}
//			Port.Set_Is_LDPHY_OLD(Port.is_ldphy);
//
//			Port.Link_Stat = link_stat;
//			Port.Auto_nego = auto_Nego;
//			Port.Switch_Stat = speed;
//
//			Port.PoE_Control = EnPwr;
//
//			int Old_Stat = Port.PoEOut_Stat;
//			Port.PoEOut_Stat = pwr_stat;
//			switch (pwr_stat) {
//			case 0:
//				if (Old_Stat != 0) {
//					if (Device.Port_Error[port_num].GeneralError_Init()) {
//						Str = DeviceManagement_Library.Get_Slot_Port(port_num, Device.getModel_Index())
//								+ " Checking Cable";
//
//						DeviceManagement_Library.Write_Log(Device.Device.Device_ip, Str, Integer.toString(pwr_stat),
//								Integer.toString(port_num), Device.Device.getDevice_Mac());
//						Device.New_Alarm = true;
//					}
//				}
//				break;
//			case 1:
//				if (Device.Port_Error[port_num].GeneralError_OK()) {
//					Str = DeviceManagement_Library.Get_Slot_Port(port_num, Device.getModel_Index()) + " OK";
//					DeviceManagement_Library.Write_Log(Device.Device.Device_ip, Str, Integer.toString(pwr_stat),
//							Integer.toString(port_num), Device.Device.getDevice_Mac());
//					Device.New_Alarm = true;
//				}
//				break;
//			case 2:
//				Str = DeviceManagement_Library.Get_Slot_Port(port_num, Device.getModel_Index()) + " Open";
//				if (Device.Port_Error[port_num].Add_Error(pwr_stat, Str)) {
//					DeviceManagement_Library.Write_Log(Device.Device.Device_ip, Str, Integer.toString(pwr_stat),
//							Integer.toString(port_num), Device.Device.getDevice_Mac());
//					if (HOME_MAP != null) {
//						Platform.runLater(new Runnable() {
//							@Override
//							public void run() {
//								HOME_MAP.Change_MAP(Device);
//							}
//						});
//					} else {
//						System.out.println("Home Map NuLL"); /* BY INGU */
//					}
//				}
//				break;
//			case 3:
//				Str = DeviceManagement_Library.Get_Slot_Port(port_num, Device.getModel_Index()) + " Short";
//				if (Device.Port_Error[port_num].Add_Error(pwr_stat, Str)) {
//					DeviceManagement_Library.Write_Log(Device.Device.Device_ip, Str, Integer.toString(pwr_stat),
//							Integer.toString(port_num), Device.Device.getDevice_Mac());
//					if (HOME_MAP != null) {
//						Platform.runLater(new Runnable() {
//							@Override
//							public void run() {
//								HOME_MAP.Change_MAP(Device);
//							}
//						});
//					} else {
//						System.out.println("Home Map NuLL"); /* BY INGU */
//					}
//				}
//				break;
//			case 4:
//				Str = DeviceManagement_Library.Get_Slot_Port(port_num, Device.getModel_Index()) + " Overcurrent";
//				if (Device.Port_Error[port_num].Add_Error(pwr_stat, Str)) {
//					DeviceManagement_Library.Write_Log(Device.Device.Device_ip, Str, Integer.toString(pwr_stat),
//							Integer.toString(port_num), Device.Device.getDevice_Mac());
//					if (HOME_MAP != null) {
//						Platform.runLater(new Runnable() {
//							@Override
//							public void run() {
//								HOME_MAP.Change_MAP(Device);
//							}
//						});
//					} else {
//						System.out.println("Home Map NuLL"); /* BY INGU */
//					}
//				}
//				break;
//			}
//		}
//	}
//
//	private static void Process_Currnet(String DMDP02, String DMDP03, DeviceManagement_DeviceThread Device,
//			int port_num) {
//		int Current;
//		String val;
//		val = DeviceManagement_Library.BinaryToString(DMDP03.toCharArray(), 4, 8) + DMDP02;
//		Current = (int) (Integer.parseInt(val, 2) * 0.05);
//
//		if (Device != null) {
//			DeviceManagement_Device_Port Port = Device.Get_Device_Port(port_num);
//			if (Port == null) {
//				return;
//			}
//
//			Port.Electric_Power = Current;
//		}
//		// System.out.println("Current : " + Current); /* BY INGU */
//
//	}
//
//	private static void Process_Bitrate(String DMDP03, String DMDP04, String DMDP05,
//			DeviceManagement_DeviceThread Device, int port_num) {
//		int Bitrate;
//		String val;
//		val = DeviceManagement_Library.BinaryToString(DMDP05.toCharArray(), 0, 4)
//				+ DeviceManagement_Library.BinaryToString(DMDP05.toCharArray(), 4, 8)
//				+ DeviceManagement_Library.BinaryToString(DMDP04.toCharArray(), 0, 4)
//				+ DeviceManagement_Library.BinaryToString(DMDP04.toCharArray(), 4, 8)
//				+ DeviceManagement_Library.BinaryToString(DMDP03.toCharArray(), 0, 4);
//
//		Bitrate = Integer.parseInt(val, 2);
//		if (Device != null) {
//			DeviceManagement_Device_Port Port = Device.Get_Device_Port(port_num);
//			if (Port == null) {
//				return;
//			}
//			if (Device.getModel_Index() == DeviceManagement_Library.IPS24GFS
//					|| Device.getModel_Index() == DeviceManagement_Library.IPC0708H
//					|| Device.getModel_Index() == DeviceManagement_Library.IPC0708HU) {
//				Port.setBitrate_In(Bitrate);
//			} else {
//				if (Device.getIS_LDPHY_CHIP(port_num)) {
//					Port.setBitrate_In(Bitrate);
//				} else {
//					Port.setBitrate_In(Bitrate * DeviceManagement_Device_IPJ24M.Bitrate);
//				}
//
//			}
//
//			if (Port.bitrate_warning) {
//				if (Port.PoE_Control && Port.Link_Stat) {
//					String Str = DeviceManagement_Library.Get_Slot_Port(port_num, Device.getModel_Index(),
//							Device.Get_Device_Port(port_num).is_ldphy) + " Low Bitrate Detected";
//					Device.Port_Error[port_num].Add_Error(DeviceManagement_Library.ERROR_AUTOPOE, Str);
//					DeviceManagement_Library.Write_Log(Device.Device.Device_ip, Str,
//							Integer.toString(DeviceManagement_Library.ERROR_AUTOPOE), Integer.toString(port_num),
//							Device.Device.getDevice_Mac());
//					DeviceManagement_UDP_Queue UDP_Data = DeviceManagement_Protocol
//							.Make_Set_DM_Data(Device.Device.getDevice_Mac(), port_num + 1, 1, 2);
//
//					Device.Device_Controller.Add_UDP_Data(UDP_Data);
//
//					Port.Auto_Poe_Info_Reset();
//				}
//			}
//		}
//
//		// System.out.println("Bitrate : " + Bitrate); /* BY INGU */
//
//	}
//
//	public static void process_DM_Data02(byte[] Data, DeviceManagement_DeviceThread Device,
//			Controller_HOME_MAP HOME_MAP) {
//		byte[] PowerData = new byte[2 * 20];
//		byte[] SlotData = new byte[Device.IP_Board.size() * 4];
//
//		for (int i = 0; i < PowerData.length; i++) {
//			PowerData[i] = Data[8 + i];
//		}
//
//		for (int i = 0; i < SlotData.length; i++) {
//			SlotData[i] = Data[48 + i];
//		}
//
//		for (int i = 0; i < Device.Power_Board.size(); i++) {
//			IPS_PowerBoard PowerBoard = Device.Power_Board.get(i);
//			PowerBoard.setDeviceID(DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 0]));
//			PowerBoard.Voltage_12 = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 1]);
//			PowerBoard.Current_12 = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 2]);
//			PowerBoard.Voltage_54 = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 3]);
//			PowerBoard.Current_54 = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 4]);
//			PowerBoard.Power_stat = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 6]);
//			PowerBoard.Error_Count = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 7]);
//			PowerBoard.year = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 8]);
//			PowerBoard.Month = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 9]);
//			PowerBoard.Day = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 10]);
//			PowerBoard.Hour = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 11]);
//			PowerBoard.Min = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 12]);
//			PowerBoard.Sec = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 13]);
//			PowerBoard.Year_S = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 14]);
//			PowerBoard.Month_S = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 15]);
//			PowerBoard.Day_S = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 16]);
//			PowerBoard.Hour_S = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 17]);
//			PowerBoard.Min_S = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 18]);
//			PowerBoard.Sec_S = DeviceManagement_Library.byteToInteger(PowerData[i * 20 + 19]);
//
//		}
//
//		for (int i = 0; i < Device.IP_Board.size(); i++) {
//			IPS_IPBoard IPBoard = Device.IP_Board.get(i);
//			IPBoard.setIPBoard_IndexForUDP(DeviceManagement_Library.byteToInteger(SlotData[i * 4 + 0]));
//			IPBoard.Port_Type = DeviceManagement_Library.byteToInteger(SlotData[i * 4 + 1]);
//			String DMDP01;
//			DMDP01 = DeviceManagement_Library.byteArrayToBinaryString(SlotData[i * 4 + 2]);
//			if (DMDP01.charAt(7) == '1') {
//				IPBoard.PowerControl = true;
//			} else {
//				IPBoard.PowerControl = false;
//			}
//
//			if (DMDP01.charAt(6) == '1') {
//				IPBoard.Bandwidth_Control = true;
//			} else {
//				IPBoard.Bandwidth_Control = false;
//			}
//
//			if (DMDP01.charAt(5) == '1') {
//				IPBoard.Paring_Control = true;
//			} else {
//				IPBoard.Paring_Control = false;
//			}
//
//			IPBoard.Max_Controllable = DeviceManagement_Library.byteToInteger(SlotData[i * 4 + 3]);
//		}
//
//	}

    public static String Get_EM_LOCK_Why(byte val) {
        switch (val) {
            case 0:
                return "알수 없는 이유로 ";
            case 1:
                return "NFC 카드로 인해 ";
            case 2:
                return "비밀번호로 인해 ";
            case 3:
                return "응급상황으로 인해 ";
            case 4:
                return "웹 동작으로 인해 ";
            case 5:
                return "Device Manager로 인해 ";
            case 6:
                return "외부 스위치로 인해 ";
            case 7:
                return "배터리 방전으로 인해";
        }

        return "";
    }


}
