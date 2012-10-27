package jmar.games.tank;

import jmar.games.net.BytesChunker;
import jmar.games.net.OutputChannelChunker;

public class PuckClientStateBuilder {
	public BytesChunker tcpInputChunker;
	public OutputChannelChunker tcpOutputChunker;	
	
	public PuckClientStateBuilder(BytesChunker tcpInputChunker, OutputChannelChunker tcpOutputChunker) {
		this.tcpInputChunker = tcpInputChunker;
		this.tcpOutputChunker = tcpOutputChunker;
	}
}
