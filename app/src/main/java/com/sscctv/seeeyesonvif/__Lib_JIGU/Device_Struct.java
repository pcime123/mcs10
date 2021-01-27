package com.sscctv.seeeyesonvif.__Lib_JIGU;


public class Device_Struct {

	private final String Name ;
	private String IP ;
	private String MAC ;
	private String GW ; 
	private String NM ; 
	
	private String CH_IP ; 
	private String CH_GW ;
	private String CH_NM ;
	private String CH_MAC ;
	
	private Boolean Change; 
	
	public Device_Struct(String IP , String Mac , String Name , String GW , String NM) {
		this.IP = IP; 
		this.MAC = Mac;
		this.Name = Name;
		this.GW = GW ; 
		this.NM = NM ; 
		Set_Change(false); 
	}
	
	public void Set_Change_Network(String IP , String GW , String NM) {
		Set_Change(false); 
		
		this.CH_IP = IP ; 
		this.CH_GW = GW ;
		this.CH_NM = NM ;
	}

	public void Set_MAC(String MAC) {
		Set_Change(false); 
		
		this.MAC = MAC ; 
	}
	
	public void Set_Change_MAC(String MAC) {
		Set_Change(false); 
		
		this.CH_MAC = MAC ; 
	}
	
	public void Set_Change(boolean val) {
		this.Change = val; 
	}
	
	public Boolean Get_Change() {
		return this.Change; 
	}	
	
	public boolean Compare_Netwrok(String IP , String GW , String NM) {
		
		
		System.out.println(CH_IP + " / " + IP  ); /* BY INGU */
		System.out.println(CH_GW + " / " + GW  ); /* BY INGU */
		System.out.println(CH_NM + " / " + NM  ); /* BY INGU */
		if(CH_IP.equals(IP) && CH_GW.equals(GW) && CH_NM.equals(NM)) {
			
			this.IP = IP; 
			this.GW = GW ; 
			this.NM = NM ;
			Set_Change(true);
			return true; 
		}else {
			Set_Change(false);
			return false; 
		}
		
	}

	public boolean Compare_MAC(String MAC) {
		
		
		System.out.println(CH_MAC + " / " + MAC  ); /* BY INGU */

		if(CH_MAC.equals(MAC)) {
			Set_Change(true);
			return true; 
		}else {
			Set_Change(false);
			return false; 
		}
		
	}
	public String Get_DeviceIP() {
		return IP ; 
	}
	

	public String Get_DeviceMac() {
		return MAC ; 
	}

	public String Get_DeviceName() {
		return Name ; 
	}
	
	public String Get_DeviceGW() {
		return GW ; 
	}
	
	public String Get_DeviceNM() {
		return NM ; 
	}
}
