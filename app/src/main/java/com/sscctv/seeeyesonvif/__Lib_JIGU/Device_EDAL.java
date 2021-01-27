package com.sscctv.seeeyesonvif.__Lib_JIGU;


import java.util.ArrayList;
import java.util.List;


public class Device_EDAL {
	
	
	public boolean EDAL_Status ;
	public boolean SET_EDAL_Status ;
	
	public boolean Main_Power_Status ; 
	public boolean Fire_Detect ; 
	public boolean EME_Button_Detect ; 
	public boolean LINE_Status ; 
	
	public int Battery_Size ; 
	public boolean Alarm_01_Detect ; 
	public boolean Alarm_02_Detect ;
	
	public int EMLOCK_Mode ; 
	public int EMLOCK_Time ;
	public int Set_EMLOCK_Mode ; 
	public int Set_EMLOCK_Time ;
	
	public boolean Siren_Mode ;
	public boolean Set_Siren_Mode ;
	public int Siren_Time ;
	public int Set_Siren_Time ;
	
	public boolean Case_Stat ; 
	 
	public boolean EME_Call_Stat ; 
	public boolean Siren_Stat ; 
	
	public boolean Battery_Status ; 
	public boolean Battery_Short ;
	public boolean Battery_System_Overtage ;
	public boolean Battery_System_Overtage_Protection ;
	public boolean Battery_Input_Overtage ;
	public boolean Battery_Input_Overtage_Protection ;
	public boolean Battery_Fault ;
	public boolean Battery_Charging ;
	
	public boolean Password_Change_Success ; 
	public String  Password ;
	
	public boolean Web_Password_Change_Success ; 
	public String  Web_Password ;
	
	public List<NFC_List> General_NFC_ID ;   
	public List<NFC_List> Patrol_NFC_ID ; 
	public List<NFC_List> Master_NFC_ID ; 
	
	public int Set_NFC_ID_Result = -1 ; 
	
	public Device_EDAL() {
		EDAL_Status = false ;
		Main_Power_Status = true ;
		
		Battery_Status = true ; 
		Battery_Short = false;
		Battery_System_Overtage = false;
		Battery_System_Overtage_Protection = false;
		Battery_Input_Overtage = false;
		Battery_Input_Overtage_Protection = false;
		Battery_Fault = false;
		Fire_Detect = false ;
		EME_Button_Detect = false ;
		LINE_Status = true ;
		Alarm_01_Detect = false ;
		Alarm_02_Detect = false ; 
		Case_Stat = false;
		EME_Call_Stat = false;
		Siren_Stat = false;
		
		 Password = "";
		 Web_Password = "" ;
		 
//		 Master_Mac= "";
		 
//		 USE_SNMP = false ;
		 
		 General_NFC_ID = new ArrayList<Device_EDAL.NFC_List>();
		 Patrol_NFC_ID = new ArrayList<Device_EDAL.NFC_List>();
		 Master_NFC_ID = new ArrayList<Device_EDAL.NFC_List>();
		 
	}
	

//
//	@Override
//	public void setDevice(DeviceManagement_Device Device) {
//		if(this.Device == null ) {
//			this.Device = Device;
//		}
//	}
	
//	public void setMainConfig(MainConfig main) {
//		this.main = main;
//	}
	
//	public void setPrimaryStage(Stage primaryStage) {
//		this.primaryStage = primaryStage;
//	}

	public void init_NFC_ID(int mode) {
		if(mode == 0) {
			General_NFC_ID.clear();
		}else if(mode == 1){
			Master_NFC_ID.clear();
		}else if(mode == 2) {
			Patrol_NFC_ID.clear();						
		}
	}
	
	public void Add_NFC_ID(int mode , byte[] ID) {
		if(mode == 0) {
			General_NFC_ID.add(new NFC_List(ID));
		}else if(mode == 1){
			Master_NFC_ID.add(new NFC_List(ID));	
		}else if(mode == 2) {
			Patrol_NFC_ID.add(new NFC_List(ID));
		}	
	}
	
	public class NFC_List{
		public byte[] NFC_ID ; 
		public boolean enable ; 
		
		public NFC_List(byte[] ID) {
			NFC_ID = new byte[20];
			enable = true;
			
			for(int i = 0 ; i < 20 ; i ++) {
				NFC_ID[i] = ID[i] ; 
			}
		}
	}
	
}
