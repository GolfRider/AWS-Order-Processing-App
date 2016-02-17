package com.order.commons;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class OrderUtils {
	private static final String macInfo=getMacInfo();
	private static final String processId=getProcessId();
	public static final String appServerId;
	
    static{
    	appServerId=_getAppServerId();
    }
    
	private static String _getAppServerId(){
 	  	StringBuilder sb= new StringBuilder(); 
 		return sb.append(processId).append(macInfo).reverse().toString();
 	}
	
 	private static String getMacInfo(){
	    StringBuilder sb = new StringBuilder();
		  try {
			  InetAddress ip = InetAddress.getLocalHost();
			  NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			  byte[] mac = network.getHardwareAddress();
			  for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "" : ""));		
			  }	
		  } catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
			
		 } 
		 return sb.toString();
	}
	
 	private static String getProcessId(){
		System.out.println("MACC_INFO:"+macInfo);
		String processName=ManagementFactory.getRuntimeMXBean().getName();
		if(processName.indexOf("@")!=-1){
			return processName.split("@")[0];
		}
		else{
			return processName;
		}
	}
 	
}
