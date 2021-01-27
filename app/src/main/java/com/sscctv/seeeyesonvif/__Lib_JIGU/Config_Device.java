package com.sscctv.seeeyesonvif.__Lib_JIGU;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Config_Device {
	DatagramSocket Datagram_send , Datagram_receive , Datagram_send_Info;
	
    private Thread UDP_Send_Thread;
    private boolean UDP_Send_Flag;
    
    private Thread UMP_Receive_Thread;
    private boolean UMP_Receive_Flag;

    public boolean Success ; 
    
    private byte[] buffer; 
    
    Device_Struct Set_Device;
    public Config_Device() {
    	UDP_Send_Flag = false; 
    	UMP_Receive_Flag = false;
    	
    	Success = false;
    }

    public void Set_Device(Device_Struct Set_Device) {
    	this.Set_Device = Set_Device; 
    }
    
    public void Set_Buffer(byte[] buffer) {
    	this.buffer = buffer; 
    	Success = false; 
    }
    
    public void SEND_RESET() {
		try {
			System.out.println("Device RESET : " + Set_Device.Get_DeviceMac()); /* BY INGU */
			Datagram_send = new DatagramSocket();
	
			//MainConfig.Debug_log.error("SEND Port : " + Datagram_send.getLocalPort()); 

			InetAddress ia = InetAddress.getByName(JIGU_Protocol.BroadCast_IP);
										
			byte[] send_buf = JIGU_Protocol.Make_Set_Reset(Set_Device.Get_DeviceMac());
			
			DatagramPacket dp = new DatagramPacket(
					send_buf,send_buf.length,ia, JIGU_Protocol.SEND_PORTNUM);
			Datagram_send.send(dp);    
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Datagram_send.close();
		}
	
    	
    }
    
    public void Start_Device_Config() {    	
    	Send_BroadCast_GETINFO();
    	UDP_Send_Flag = true ; 
    	UDP_Send_Thread.start();
    	
    	Receive_BroadCast_GETINFO(); 
    	UMP_Receive_Flag = true ;
    	UMP_Receive_Thread.start();
    }
    
    public void Stop_Device_Config() {
    	if(UMP_Receive_Thread != null) {
    		UMP_Receive_Thread.interrupt();
    		UMP_Receive_Flag = false ;
    	}
    	
    	if(UDP_Send_Thread != null) {
    		UDP_Send_Thread.interrupt();
    		Datagram_receive.close();
    		UDP_Send_Flag = false ;
    	}
    }
    
	private void Send_BroadCast_GETINFO() {
		UDP_Send_Thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while(UDP_Send_Flag) {
						try {
							Datagram_send = new DatagramSocket();
							//MainConfig.Debug_log.error("SEND Port : " + Datagram_send.getLocalPort()); 

							InetAddress ia = InetAddress.getByName(JIGU_Protocol.BroadCast_IP);
														
							byte[] send_buf = buffer;
							
							DatagramPacket dp = new DatagramPacket(
									send_buf,send_buf.length,ia, JIGU_Protocol.SEND_PORTNUM);
							Datagram_send.send(dp);
							
							Thread.sleep(2000);
							
							System.out.println("SEND_BUFFER"); /* BY INGU */
							UDP_Send_Flag = false; 
						}catch(IOException | InterruptedException e) {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}finally {
							if(Datagram_send != null) {
								Datagram_send.close();
							}
						}
					}
				}finally {
					if(Datagram_send != null) {
						Datagram_send.close();
					}
					
				}
			}

		});
	}
	
	private void Receive_BroadCast_GETINFO() {
		try {
			Datagram_receive = new DatagramSocket(JIGU_Protocol.RECEIVE_PORTNUM);
		} catch(SocketException e) {
			return;
		}
		
		UMP_Receive_Thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(UMP_Receive_Flag) {
					
					try {
						
						byte[] receciveBuffer = new byte[512];
						
						byte[] JiGuKey = new byte[4];
						JiGuKey[0] = 0;
						
						DatagramPacket data_receive = new DatagramPacket(receciveBuffer, receciveBuffer.length);
				
						Datagram_receive.receive(data_receive);
						
						int data_length = data_receive.getLength();
						byte[] result_buffer = new byte[data_length];
						
						data_receive.setData(receciveBuffer, 0, data_length);
						//System.out.println(DeviceManagement_Library.byteArrayToHexString(rececive_buffer, 0, data_length)); 
						
						byte error = JIGU_Protocol.JiGu_Decode(receciveBuffer, data_length,result_buffer,JiGuKey);
						switch(error) {
						case JIGU_Protocol.JiGu_Error_OK :
							process_Data(result_buffer , data_receive.getAddress().toString());
							break;
						case JIGU_Protocol.JiGu_Error_Header :
							System.out.println("Error Header"); 
							break;
						case JIGU_Protocol.JiGu_Error_CheckSum1  :
							System.out.println("Error_CheckSum1"); 
							break;
						case JIGU_Protocol.JiGu_Error_CheckSum2 :
							System.out.println("Error_CheckSum2"); 
							break;
						}
						
					}catch(IOException ignored) {
						
					}
					
				}
			}
		});
	}
	
	private void process_Data(byte[] Data , String IP)
	{
		int Mode = JIGU_Library.HexByteToInteger(Data, 1, 1);

		try {
			switch(Mode){

			case JIGU_Protocol.SET_IP_RESPONE :
				Process_IPResone(Data);
				break;
				
			case JIGU_Protocol.SET_MAC_RESPONE :
				Process_MACResone(Data);
				break;
			}
			
		}catch(Exception e) {
			System.out.println(e.getMessage()); /* BY INGU */
			
		}
		
	}
	
	private void Process_IPResone(byte[] Data) {
		String Mac = JIGU_Library.byteArrayToMacAddress(Data, 2, 7).trim();
		String IP = JIGU_Library.byteArrayToIPAddress(Data, 8, 11).trim();
		String GW = JIGU_Library.byteArrayToIPAddress(Data, 12, 15).trim();
		String NM = JIGU_Library.byteArrayToIPAddress(Data, 16, 19).trim();
		
		System.out.println("MAC : " + Mac + " IP : " + IP + " GW : " + GW + " NM : "+ NM + " / IP RESPONE"); /* BY INGU */	
		if (Set_Device != null) {
			if (Set_Device.Compare_Netwrok(IP, GW, NM)) {
				
				Success = true ;
				System.out.println("MAC : " + Mac + " / IP RESPONE Success"); /* BY INGU */				
				
			}else {
				Success = false ; 
				System.out.println("MAC : " + Mac + " / IP RESPONE Fail"); /* BY INGU */	
			}
		}
	}
	
	private void Process_MACResone(byte[] Data) {
		String Mac_Address01 = JIGU_Library.byteArrayToMacAddress(Data,2,7).trim();
		String Mac_Address02 = JIGU_Library.byteArrayToMacAddress(Data,8,13).trim();
		
		System.out.println("MAC : " + Mac_Address01 + " MAC02 : " + Mac_Address02 + "/ MAC RESPONE"); /* BY INGU */	
		if (Set_Device != null) {
			if (Set_Device.Compare_MAC(Mac_Address02)) {
				
				Success = true ;
				System.out.println("MAC : " + Mac_Address01 + " MAC02 : " + Mac_Address02 + " / IP RESPONE Success"); /* BY INGU */				
				
			}else {
				Success = false ; 
				System.out.println("MAC : " + Mac_Address01 + " MAC02 : " + Mac_Address02 + " / IP RESPONE Fail"); /* BY INGU */	
			}
		}
	}	
}
