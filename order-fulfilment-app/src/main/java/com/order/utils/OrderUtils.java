package com.order.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

public class OrderUtils {
	private static final String macInfo=getMacInfo();
	private static final String processId=getProcessId();
	private static final Random random= new Random();
	
	
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
 	
 	public static String getAppId(){
 	  	StringBuilder sb= new StringBuilder(); 
 		return sb.append(processId).append(macInfo).reverse().toString();
 	}
 	
 	public static void randomWaitTime(int min,int max){
 		int randomWait=getRandom(min, max);
 		try {
			Thread.sleep(randomWait * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
 	
 	private static int getRandom(int min,int max){
 		int maxWait=max;
 		int minWait=min;
 		int randomWaitTime=random.nextInt((maxWait-minWait)+1)+minWait;
 		return randomWaitTime;
 	}

}
