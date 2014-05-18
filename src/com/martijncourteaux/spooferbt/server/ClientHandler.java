package com.martijncourteaux.spooferbt.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.UnresolvedAddressException;

import com.martijncourteaux.spooferbt.common.Peer;
import com.martijncourteaux.spooferbt.common.Utils;

public class ClientHandler implements Runnable
{

	public static final int ACTION_STOP = -1;
	public static final int ACTION_ANNOUNCE = 0;
	public static final int ACTION_PROXY = 1;

	private Server server;
	private Socket socket;

	public ClientHandler(Server server, Socket socket)
	{
		this.server = server;
		this.socket = socket;
	}

	@Override
	public void run()
	{
		try
		{
			handle();
		} catch (Exception e)
		{
			server.exceptionFromHandler(this, e);
		}
	}

	private void handle() throws Exception
	{
		DataInputStream dis = Utils.wrapDIS(socket.getInputStream());
		DataOutputStream dos = Utils.wrapDOS(socket.getOutputStream());

		int action = dis.readInt();

		if (action == ACTION_STOP)
		{
			System.out.println("Request to stop the server from: " + socket);
			String passwordMD5 = dis.readUTF();
			if (passwordMD5.equals(server.getHaltPasswordMD5()))
			{
				System.out.println("Supplied valid password");
				server.stop();
				dos.writeBoolean(true);
			} else
			{
				System.out.println("Invalid password!");
				dos.writeBoolean(false);
			}
			dos.close();
			dis.close();
			socket.close();
		} else if (action == ACTION_ANNOUNCE)
		{
			System.out.println("Announcing...");
			TrackerAnnounceRequest request = new TrackerAnnounceRequest();

			request.info_hash = dis.readUTF();
			request.peer_id = dis.readUTF();
			request.ip = dis.readUTF();
			request.port = dis.readInt();
			request.uploaded = dis.readInt();
			request.downloaded = dis.readInt();
			request.left = dis.readInt();
			request.event = dis.readUTF();
			request.numwant = dis.readInt();

			String tracker_protocol = dis.readUTF();
			String tracker_host = dis.readUTF();
			int tracker_port = dis.readInt();

			TrackerAnnounceResponse response = null;

			if (tracker_protocol.equals("HTTP"))
			{
				System.out.println("Announce to: http://" + tracker_host);

				HTTPTracker tc = new HTTPTracker(tracker_host);
				response = tc.request(request);
			}
			if (tracker_protocol.equals("UDP"))
			{
				System.out.println("Announce to: udp://" + tracker_host + ":" + tracker_port);
				try
				{
					UDPTracker tracker = new UDPTracker(tracker_host, tracker_port);
					response = tracker.request(request);
				} catch (UnresolvedAddressException uae)
				{
					System.out.println("Unresolved address: " + tracker_host);
				} catch (SocketTimeoutException soe)
				{
					System.out.println("Tracker did not respond.");
				}
			}

			dos.writeInt(response.interval);
			dos.writeInt(response.leechers);
			dos.writeInt(response.seeders);
			dos.writeInt(response.peers.size());
			for (int i = 0; i < response.peers.size(); ++i)
			{
				Peer p = response.peers.get(i);
				dos.write(p.address);
				dos.writeShort(p.port);
			}

			dos.close();
			dis.close();
			socket.close();
		} else if (action == ACTION_PROXY)
		{
			System.out.println("Proxying a UDP package");
			String tracker_host = dis.readUTF();
			int tracker_port = dis.readInt();
			System.out.println("Requested tracker: " + tracker_host + ":" + tracker_port);

			int length = dis.readInt();
			byte[] buffer = new byte[length];
			dis.readFully(buffer);
			System.out.println("Package size: " + length);

			try
			{
				UDPTracker tracker = new UDPTracker(tracker_host, tracker_port);
				byte[] response = tracker.forwardPacket(buffer, 0, buffer.length);
				
				System.out.println("Tracker responsed: " + response.length);

				dos.writeInt(response.length);
				dos.write(response);


			} catch (SocketTimeoutException soe)
			{
				System.out.println("Tracker did not respond...");
				dos.writeInt(-1);
			}
			dos.close();
			dis.close();
			socket.close();

		} else
		{
			System.out.println("Unknown action! " + action);
		}
	}

}
