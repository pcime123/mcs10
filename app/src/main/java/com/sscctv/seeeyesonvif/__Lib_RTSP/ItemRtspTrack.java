package com.sscctv.seeeyesonvif.__Lib_RTSP;

public class ItemRtspTrack {

	public String Track ; 
	public boolean Sendonly; 
	public int Track_Num ; 
	
	public boolean Send;
	
	public String Client_Port_01;
	public String Client_Port_02;
	
	public String Server_Port_01;
	public String Server_Port_02;
	
	public boolean is_audio ; 
	public boolean is_vedio ;
	
	public ItemRtspTrack(String Track , Boolean SendOnly) {
		this.Track = Track ; 
		this.Sendonly = SendOnly;
		this.Track_Num = Integer.parseInt(this.Track.substring(5 ,this.Track.length() )); 
		
		this.Send = false; 
		this.is_audio = false; 
		this.is_vedio = false; 
	}
	
	public void setClientPort(String Port) {
		if(!Port.contains("-")) {
			Client_Port_01 = Port;
			Client_Port_02 = null ; 
//			System.out.println("Client_Port_01 : " + Client_Port_01); /* BY INGU */
		}else {
			String[] Client = Port.split("-");
			Client_Port_01 = Client[0]; 
			Client_Port_02 = Client[1];
//			System.out.println("Client_Port_01 : " + Client_Port_01 + " / " + "Client_Port_02 : " + Client_Port_02 ); /* BY INGU */
		}
	}
	
	public void setServerPort(String Port) {
		if(!Port.contains("-")) {
			Server_Port_01 = Port;
			Server_Port_02 = null ; 
			System.out.println("Server_Port_01 : " + Server_Port_01); /* BY INGU */
		}else {
			String[] Server = Port.split("-");
			Server_Port_01 = Server[0]; 
			Server_Port_02 = Server[1];
			System.out.println("Server_Port_01 : " + Server_Port_01 + " / " + "Server_Port_02 : " + Server_Port_02 ); /* BY INGU */
		}
		
	}
	
	public void Set_Audio_Video(boolean audio , boolean video ) {
		this.is_audio = audio ; 
		this.is_vedio = video ; 
	}
}
