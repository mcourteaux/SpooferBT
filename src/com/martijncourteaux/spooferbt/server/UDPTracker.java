package com.martijncourteaux.spooferbt.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.util.Arrays;
import java.util.Random;

import com.martijncourteaux.spooferbt.common.Peer;
import com.martijncourteaux.spooferbt.common.Utils;

public class UDPTracker extends Tracker
{

	private String host;
	private int port;
	private InetSocketAddress address;
	private DatagramSocket socket;

	private long connection_timestamp;
	private long current_connection_id;
	private int current_transaction_id;

	/* Do not change order!! */
	private static enum Event
	{
		NONE, COMPLETED, STARTED, STOPPED
	}

	public UDPTracker(String host, int port) throws Exception
	{
		this.host = host;
		this.port = port;
		this.address = new InetSocketAddress(host, port);
		if (this.address.isUnresolved())
		{
			throw new UnresolvedAddressException();
		}
		this.socket = new DatagramSocket();
		this.socket.connect(address);
		this.socket.setSoTimeout(7500);
		System.out.println("Datagram socket created at port: " + socket.getLocalPort());
	}

	private boolean isConnectionValid()
	{
		long now = System.currentTimeMillis();
		return (now < connection_timestamp + 60000L);
	}
	
	private void requestConnectionID() throws IOException
	{
		System.out.println("Requesting new connection ID...");
		int transaction_id = new Random().nextInt();		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		dos.writeLong(0x41727101980L);
		dos.writeInt(0);
		dos.writeInt(transaction_id);

		dos.close();

		DatagramPacket packet = new DatagramPacket(baos.toByteArray(), baos.size(), address);
		DatagramPacket response = new DatagramPacket(new byte[16], 16);
		socket.send(packet);
		socket.receive(response);
		
		System.out.println("Tracker responded from UDP port: " + response.getPort());

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(response.getData()));
		int response_action = dis.readInt();
		int response_transaction_id = dis.readInt();
		long response_connection_id = dis.readLong();

		if (response_transaction_id != transaction_id)
		{
			System.out.println("invalid response transaction id!");
		} else if (response_action != 0)
		{
			System.out.println("invalid response action!");
		} else
		{
			connection_timestamp = System.currentTimeMillis();
			
			current_connection_id = response_connection_id;
			current_transaction_id = response_transaction_id;
		}
	}

	public TrackerAnnounceResponse request(TrackerAnnounceRequest tr) throws Exception
	{
		/* Request a connection_id */
		if (!isConnectionValid())
		{
			requestConnectionID();
		}

		/* Announce */
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);

			dos.writeLong(current_connection_id);
			dos.writeInt(1);
			dos.writeInt(current_transaction_id);
			dos.write(Utils.encodeInfoHash(tr.info_hash));
			dos.write(Utils.getPaddedBytes(tr.peer_id, 20));
			dos.writeLong(tr.downloaded);
			dos.writeLong(tr.left);
			dos.writeLong(tr.uploaded);
			dos.writeInt(Event.valueOf(tr.event.toUpperCase()).ordinal());
			dos.write(InetAddress.getByName(tr.ip).getAddress());
			dos.writeInt(0); // key?
			dos.writeInt(tr.numwant);
			dos.writeShort(tr.port);

			dos.close();

			socket.setSoTimeout(5000);

			DatagramPacket packet = new DatagramPacket(baos.toByteArray(), baos.size(), address);
			DatagramPacket response = new DatagramPacket(new byte[200], 200);
			
			socket.send(packet);
			System.out.println("Announce sent...");
			System.out.println("Waiting for response...");
			socket.receive(response);
			System.out.println("Got response! " + response.getLength());

			TrackerAnnounceResponse tres = new TrackerAnnounceResponse();

			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(response.getData(), 0, response.getLength()));
			tres.action = dis.readInt();
			tres.transaction_id = dis.readInt();
			tres.interval = dis.readInt();
			tres.leechers = dis.readInt();
			tres.seeders = dis.readInt();

			int response_peer_count = (response.getLength() - 20) / 6;

			for (int i = 0; i < response_peer_count; ++i)
			{
				byte[] ipAddress = new byte[4];
				dis.read(ipAddress);
				int port = dis.readShort();
				tres.peers.add(new Peer(ipAddress, port));
			}

			return tres;
		}

	}
	
	public byte[] forwardPacket(byte[] buffer, int offset, int length) throws IOException
	{
		DatagramPacket packet = new DatagramPacket(buffer, offset, length, address);
		DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
		socket.send(packet);
		socket.receive(response);
		socket.close();
		
		return Arrays.copyOfRange(response.getData(), response.getOffset(), response.getLength());
	}

	public InetSocketAddress getAddress()
	{
		return address;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}
	
	public void close()
	{
		socket.disconnect();
		socket.close();
	}
}
