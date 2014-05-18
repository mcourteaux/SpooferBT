package com.martijncourteaux.spooferbt.common;

public class Peer
{
	
	public Peer()
	{
	}
	
	public Peer(byte[] address, int port)
	{
		this.address = address;
		this.port = port;
	}

	public byte[] address;
	public int port;
	
	@Override
	public String toString()
	{
		return String.format("%d.%d.%d.%d:%d", address[0] & 0xFF, address[1] & 0xFF, address[2] & 0xFF, address[3] & 0xFF, port & 0xFFFF);
	}
	
}
