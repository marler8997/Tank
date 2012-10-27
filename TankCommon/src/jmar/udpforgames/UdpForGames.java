package jmar.udpforgames;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.ReverbType;

import jmar.games.tank.Constants;

//
// Different Types of Packets
//
//
//    Byte 0:  Packet Type
//     0     : Unreliable Packet (No Ack Required)
//     1     : Reliable Packet (Requires an ack)
//     2     : Requesting an ACK of the last sequence number not received
//   Byte 1-2: The last sequence number not received
// Reliable Packet Only
//   Byte 3-4: Send sequence number 



// Not Thread Safe
// Make sure that only one thread is calling methods on this class
public class UdpForGames {
	public static final int unreliablePacketHeaderLength = 3;
	public static final int reliablePacketHeaderLength = 5;
	
	public static final int windowPacketLength = 32;
	public static final int windowByteLength = (windowPacketLength+1) * Constants.maxTotalPacketSize;
	
	public final DatagramChannel channel;
	
	private int sendNextSequenceNumber,recvNextSequenceNumber;
	private int[] lostRecvPacketNumbersLowToHigh;
	private int lostRecvPacketsCount;
	
	private byte ackNumber;
	
	private byte[] window; // buffer of previously sent packets
	private int windowPosition;
	
	private int[] windowPacketOffsets;
	private int windowPacketCount;
	
	private int lastAckedSequenceNumber;
	
	
	private byte[] unreliablePacketBuffer;
	private ByteBuffer unreliablePacketByteBuffer;
	
	public UdpForGames(DatagramChannel channel) {
		this.channel = channel;
		
		this.ackNumber = 0;
		this.sendNextSequenceNumber = 0;
		this.recvNextSequenceNumber = 0;
		
		this.lostRecvPacketNumbersLowToHigh = new int[windowPacketLength];
		this.lostRecvPacketsCount = 0;
		
		this.window = new byte[windowByteLength];
		this.windowPosition = 0;
		this.windowPacketOffsets = new int[windowPacketLength];
		this.windowPacketCount = 0;
		
		this.lastAckedSequenceNumber = 0;
		
		
		// windowPacketOffsets[sequenceX] is windowPacketOffsets[sendNextSequenceNumber - windowPacketSequenceNumberDifference]
		
		
		this.unreliablePacketBuffer = new byte[Constants.maxTotalPacketSize];
		this.unreliablePacketBuffer[0] = 0;
		this.unreliablePacketByteBuffer = ByteBuffer.wrap(this.unreliablePacketBuffer);
	}
	
	// Returns the number of packets that have not been acknowledged
	public int tryResends() {
		return 0;
	}
	
	public void sendUnreliable(ByteBuffer sendBuffer) {
		byte[] sendArray = sendBuffer.array();
		int sendLength = sendBuffer.position();

		System.arraycopy(sendArray, 0, unreliablePacketBuffer, unreliablePacketHeaderLength, sendLength);

		// Fill in Ack
		int lastSequenceNumberNotReceived = 
				(this.lostRecvPacketsCount <= 0) ? recvNextSequenceNumber : lostRecvPacketNumbersLowToHigh[0];
		unreliablePacketBuffer[1] = (byte)(lastSequenceNumberNotReceived >> 8);
		unreliablePacketBuffer[2] = (byte)(lastSequenceNumberNotReceived     );		
	}
	
	
	public void sendReliable(ByteBuffer sendBuffer) throws NeedAcksException {
		// Check if window is full
		if(windowPacketCount >= windowPacketLength - 1) {
			// Send ACK REQUEST packets a few times until an ack is sent
			throw new NeedAcksException();
		}

		// Save window offset
		windowPacketOffsets[windowPacketCount++] = windowPosition;
		
		// Copy data to window
		byte[] sendArray = sendBuffer.array();
		int sendLength = sendBuffer.position();
		System.arraycopy(sendArray, 0, window, windowPosition + 5, sendLength);
		
		// Fill in Ack
		int lastSequenceNumberNotReceived = 
				(this.lostRecvPacketsCount <= 0) ? recvNextSequenceNumber : lostRecvPacketNumbersLowToHigh[0];
		window[windowPosition + 1] = (byte)(lastSequenceNumberNotReceived >> 8);
		window[windowPosition + 2] = (byte)(lastSequenceNumberNotReceived     );
		
		// Fill in sequence number
		window[windowPosition + 3] = (byte)(this.sendNextSequenceNumber >> 8);
		window[windowPosition + 4] = (byte)(this.sendNextSequenceNumber     );
		
		this.sendNextSequenceNumber++;
		if(this.sendNextSequenceNumber > 0xFFFF) this.sendNextSequenceNumber = 0;
	}
	
	public SocketAddress receive(ByteBuffer recvBuffer, int timeoutMillis) throws IOException, NeedResendsException {
		int sleepTime = timeoutMillis / 10;
		if(sleepTime < 0) sleepTime = 0;
		if(sleepTime > 500) sleepTime = 500;
	
		long timeoutNano = timeoutMillis * 1000;
		
		long startTimeNano = System.nanoTime();
		
		SocketAddress from;
		while(true) {
			from = channel.receive(recvBuffer);
			if(from != null) break;
			
			long nowNano = System.nanoTime();
			if(nowNano - startTimeNano > timeoutNano) {
				return null;
			}
			
			try { Thread.sleep(sleepTime); } catch(InterruptedException e) { }			
		}

		return handleReceive(from, recvBuffer);
	}
	
	
	public SocketAddress receiveNonBlocking(ByteBuffer recvBuffer) throws IOException, NeedResendsException {
		SocketAddress from = channel.receive(recvBuffer);
		if(from == null) return null;
		
		return handleReceive(from, recvBuffer);
	}
	
	
	public SocketAddress handleReceive(SocketAddress from, ByteBuffer recvBuffer) throws NeedResendsException {	
		byte[] packet = recvBuffer.array();
		int length = recvBuffer.position();
		
		if(length <= 0) return null; // packets of length 0 are invalid
		
		// Get ACK number
		int lastSequenceNumberNotReceived = (0xFF00 & (packet [1] << 8)) | (0xFF & packet[2]);
		if(lastSequenceNumberNotReceived >= sendNextSequenceNumber) {
			// clear the window
			this.windowPosition = 0;
			this.windowPacketCount = 0;
		} else if(lastSequenceNumberNotReceived > lastAckedSequenceNumber) {
			
			// fix the window
			throw new UnsupportedOperationException();
			
		}		
		
		
		// Get Packet Type
		byte packetType = packet[0];
		if(packetType == 0) return from;
		if(packetType == 1) {
			int sequenceNumber = (0xFF00 & (packet [3] << 8)) | (0xFF & packet[4]);  
			if(sequenceNumber > recvNextSequenceNumber + 1) {
				do {
					if(lostRecvPacketsCount >= windowPacketLength) {
						// need to request resends
						throw new NeedResendsException();
					}
					lostRecvPacketNumbersLowToHigh[lostRecvPacketsCount++] = recvNextSequenceNumber;
					recvNextSequenceNumber++;					
				} while(recvNextSequenceNumber < sequenceNumber);
				recvNextSequenceNumber++;									
			}
			
		}
		
		return from;
		
		
	}
	
	
}
