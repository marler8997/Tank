package jmar;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MacAddress {
	private static MacAddress localHostMacAddress = null;
	public static MacAddress getLocalHostMacAddress() {
		if(localHostMacAddress == null) {
			try {
				localHostMacAddress= new MacAddress(NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress());
			} catch(SocketException e) {
				throw new RuntimeException(String.format("Got a socket exception while trying to get the MacAddress of the local host?"));
			} catch(UnknownHostException e) {
				throw new RuntimeException(String.format("InetAddress.getLocalHost() threw an UnknownHostException?"));				
			}
			// Hack to override mac address
			//localHostMacAddress = new MacAddress(new byte[]{(byte)0x0B,(byte)0xCD,(byte)0xEE,(byte)0x33,(byte)0x11,(byte)0x84});
		}
		return localHostMacAddress;
	}
	
	
	private byte[] bytes;
	public MacAddress(byte[] bytes) {
		this.bytes = new byte[6];
		this.bytes[0] = bytes[0];
		this.bytes[1] = bytes[1];
		this.bytes[2] = bytes[2];
		this.bytes[3] = bytes[3];
		this.bytes[4] = bytes[4];
		this.bytes[5] = bytes[5];
	}
	
	public boolean equals(MacAddress mac) {
		return
				this.bytes[0] == mac.bytes[0] &&
				this.bytes[1] == mac.bytes[1] &&
				this.bytes[2] == mac.bytes[2] &&
				this.bytes[3] == mac.bytes[3] &&
				this.bytes[4] == mac.bytes[4] &&
				this.bytes[5] == mac.bytes[5] ;
	}
	
	public String toSmallString() {
		return String.format("%02X%02X%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]);
	}
	
	public String toString() {
		return String.format("%02X-%02X-%02X-%02X-%02X-%02X", bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]);
	}	
}
