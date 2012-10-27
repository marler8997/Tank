package jmar.games.tank;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TcpUdpAddressPortPair {
	public static long getAddressPortKey(Inet4Address address, int port) {
		byte[] clientAddressBytes = address.getAddress();
		return
				(0x0000FF0000000000L & (clientAddressBytes[0] << 40)) |
				(0x000000FF00000000L & (clientAddressBytes[1] << 32)) |
				(0x00000000FF000000L & (clientAddressBytes[2] << 24)) |
				(0x0000000000FF0000L & (clientAddressBytes[3] << 16)) |
				(0x000000000000FF00L & (port                       )) |
				(0x00000000000000FFL & (port                       )) ;
	}

	public final Inet4Address inet4Address;
	public final String inet4AddressString;
	private final long addressPartOfKeys;

	public String tcpKeyString,udpKeyString;

	public int tcpSourcePort,udpSourcePort;
	public long tcpAddressPortKey,udpAddressPortKey;
	public InetSocketAddress tcpSocketAddress,udpSocketAddress;

	public TcpUdpAddressPortPair(Inet4Address inet4Address) {
		this.inet4Address = inet4Address;
		byte[] addressBytes = inet4Address.getAddress();
		this.inet4AddressString = String.format("%d.%d.%d.%d",
				addressBytes[0], addressBytes[1], addressBytes[2], addressBytes[3]);
		this.addressPartOfKeys =
				(0x0000FF0000000000L & (addressBytes[0] << 40)) |
				(0x000000FF00000000L & (addressBytes[1] << 32)) |
				(0x00000000FF000000L & (addressBytes[2] << 24)) |
				(0x0000000000FF0000L & (addressBytes[3] << 16)) ;
		this.tcpSourcePort = -1;
		this.udpSourcePort = -1;
	}
	public boolean tcpPortIsSet() {return tcpSourcePort > 0; }
	public boolean udpPortIsSet() {return udpSourcePort > 0; }
	public void setTcpSourcePort(int tcpSourcePort) {
		this.tcpSourcePort = tcpSourcePort;
		this.tcpAddressPortKey = addressPartOfKeys | 
				(0x000000000000FF00L & (tcpSourcePort)) |
				(0x00000000000000FFL & (tcpSourcePort)) ;		
		this.tcpSocketAddress = new InetSocketAddress(inet4Address, tcpSourcePort);
		this.tcpKeyString = String.format("%s:%d", inet4AddressString, tcpSourcePort);
	}
	public void setUdpSourcePort(int udpSourcePort) {
		this.udpSourcePort = udpSourcePort;
		this.udpAddressPortKey = addressPartOfKeys |
				(0x000000000000FF00L & (udpSourcePort)) |
				(0x00000000000000FFL & (udpSourcePort)) ;
		this.udpSocketAddress = new InetSocketAddress(inet4Address, udpSourcePort);
		this.udpKeyString = String.format("%s:%d", inet4AddressString, udpSourcePort);
	}
}
