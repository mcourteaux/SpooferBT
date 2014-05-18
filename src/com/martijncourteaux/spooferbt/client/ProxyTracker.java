package com.martijncourteaux.spooferbt.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

import com.martijncourteaux.spooferbt.common.Utils;
import com.martijncourteaux.spooferbt.server.ClientHandler;

public class ProxyTracker implements Runnable
{

	private InetSocketAddress localhost;
	private volatile boolean running;
	private InetSocketAddress host;
	private String tracker;
	private int tracker_port;
	private DatagramSocket socket;

	public ProxyTracker(int port, InetSocketAddress host) throws IOException
	{
		this.host = host;
		this.localhost = new InetSocketAddress("127.0.0.1", port);
		this.socket = new DatagramSocket(localhost.getPort());
		this.socket.setSoTimeout(10000);
	}

	@Override
	public void run()
	{
		running = true;
		try
		{
			byte[] buffer = new byte[1024];

			while (running)
			{
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

				System.out.println("UDP Socket ready at: " + socket.getLocalSocketAddress());

				System.out.println("Waiting for packet...");
				packet.setLength(buffer.length);
				boolean received = false;
				do
				{
					try
					{
						socket.receive(packet);
						received = true;
					} catch (SocketTimeoutException ste)
					{

					}
				} while (running && !received);
				if (!running)
				{
					break;
				}
				System.out.println(new Date());
				System.out.println("Received packet: " + packet.getAddress() + ":" + packet.getPort() + " ; Length: " + packet.getLength());

				System.out.println("Connecting to server: " + host.getAddress() + ":" + host.getPort());
				Socket hostSocket = new Socket(host.getAddress(), host.getPort());
				DataOutputStream hostOut = Utils.wrapDOS(hostSocket.getOutputStream());
				DataInputStream hostIn = Utils.wrapDIS(hostSocket.getInputStream());

				hostOut.writeInt(ClientHandler.ACTION_PROXY);
				hostOut.writeUTF(tracker);
				hostOut.writeInt(tracker_port);
				hostOut.writeInt(packet.getLength());
				hostOut.write(buffer, packet.getOffset(), packet.getLength());
				hostOut.flush();

				System.out.println("Connected, packet fowarded to host...");

				int responseLength = hostIn.readInt();
				if (responseLength == -1)
				{
					System.out.println("Tracker apparently did not respond...");
					System.out.println("Sending nothing back to the Torrent Application");
				} else
				{
					hostIn.readFully(buffer, 0, responseLength);

					System.out.println("Response from host: " + responseLength);
					InetSocketAddress responseAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
					System.out.println("Sending back to Torrent application (" + responseAddress + ")");
					DatagramPacket response = new DatagramPacket(buffer, responseLength, responseAddress);
					socket.send(response);
				}

				hostSocket.close();

				System.out.println();

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void stop()
	{
		socket.close();
		System.out.println("\nShutting down...");
		running = false;
	}

	public void setTracker(String tracker_host, int tracker_port)
	{
		this.tracker = tracker_host;
		this.tracker_port = tracker_port;
	}

}
