package com.martijncourteaux.spooferbt;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.UnresolvedAddressException;

import com.martijncourteaux.spooferbt.argumentsparser.ArgumentParser;
import com.martijncourteaux.spooferbt.client.Client;
import com.martijncourteaux.spooferbt.common.Utils;
import com.martijncourteaux.spooferbt.server.ClientHandler;
import com.martijncourteaux.spooferbt.server.HTTPTracker;
import com.martijncourteaux.spooferbt.server.Server;
import com.martijncourteaux.spooferbt.server.TrackerAnnounceRequest;
import com.martijncourteaux.spooferbt.server.TrackerAnnounceResponse;
import com.martijncourteaux.spooferbt.server.UDPTracker;

public class Main
{

	private static String createUsage()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);

		out.println();
		out.println("SpooferBT");
		out.println("---------");
		out.println();
		out.println("For info about the setup, run SpooferBT.jar --setup-help");
		out.println();
		out.println("Usage: java -jar SpooferBT.jar [mode] [options]");
		out.println();
		out.println("[ mode ]:");
		out.println(" --server       Start the server proxy component on the given port");
		out.println("   [ options ]:");
		out.println("   --server-port  <port>       Use the given port to run the server");
		out.println("   --password                  A password needed to halt the server on remote (none by default)");
		out.println();
		out.println(" --proxy        Start the client proxy component");
		out.println("   [ options ]:");
		out.println("   --proxy-port   <port>       The local port to run the local proxy tracker");
		out.println("   --host         <url:port>   The proxy server that serves this local proxy");
		out.println("   --tracker      <url:port>   The real tracker url or ip");
		out.println();
		out.println(" --announce     Do a simple announce for an info_hash");
		out.println("   [ options ]:");
		out.println("   --udp                       Use UDP announce protocol");
		out.println("   --http                      Use HTTP announce protocol");
		out.println("   --tracker      <url:port>   The tracker url or ip");
		out.println();
		out.println("   --ip           <ip>         The client's ip (default 0.0.0.0)");
		out.println("   --port         <port>       The client's port");
		out.println("   --peer-id      <str>        The client's id, basically random. (20 bytes max String)");
		out.println("   --info-hash    <hash>       The info hash to announce (40 char hex string: 20 bytes)");
		out.println("   --event        <str>        Specifies the event (default: started)");
		out.println("   --numwant                   Specifies the number of clients you want");
		out.println();
		out.println(" --stop-server  Request a server to halt");
		out.println("     --host      <url:port>    The server to halt");
		out.println("     --password  <str>         A password to confirm");
		out.println();
		out.println("Exaples:");
		out.println("SpooferBT.jar --server --server-port 9931");
		out.println("SpooferBT.jar --announce --udp --info-hash 123456789023456789012345678901234567890 --port 6887 --tracker some.tracker.com:80 --peer-id azerty12309 --numwant 30");

		out.close();
		return baos.toString();
	}

	private static void setupHelp()
	{
		System.out.println();
		System.out.println("Normally, tracker communication goes through UDP. But in");
		System.out.println("some companies or schools, all UDP traffic is blocked by");
		System.out.println("the network admins. However, TCP/IP traffic is of course");
		System.out.println("not blocked. Hopefully you can connect with any external");
		System.out.println("host. If so, we can setup a proxy system that will allow");
		System.out.println("you to do the tracker communication by using TCP instead");
		System.out.println("of UDP, which hopefully not blocked.");
		System.out.println();
		System.out.println("So normally you have:");
		System.out.println();
		System.out.println("  [Torrent Client]     <--- UDP --->     [Tracker]");
		System.out.println();
		System.out.println("But this application does this:");
		System.out.println();
		System.out.println("  [Torrent Client]  <-X- UDP Blocked --->  [Tracker]");
		System.out.println("          |                                    |");
		System.out.println("          |                                    |");
		System.out.println("     Local UDP                                UDP");
		System.out.println("          |                                    |");
		System.out.println("          |                                    |");
		System.out.println("  [SpooferBT Proxy]    <--- TCP --->  [SpooferBT Server]");
		System.out.println("                                           or Host");
		System.out.println();
		System.out.println("So, once this setup is created. You should add the proxy");
		System.out.println("to the torrent client's list of trackers. Peers will now");
		System.out.println("be acquired by this application.");
		System.out.println();
		System.out.println("Good luck!");
		System.out.println();

	}

	public static void main(String[] args) throws Exception
	{
		ArgumentParser p = new ArgumentParser();
		p.setUsage(createUsage());

		p.addArgument("--setup-help", null, null);

		p.addArgument("--server", null, null);
		p.addArgument("--server-port", Integer.class, null);
		p.addArgument("--password", String.class, "");

		p.addArgument("--proxy", null, null);
		p.addArgument("--proxy-port", Integer.class, null);
		p.addArgument("--host", String.class, null);

		p.addArgument("--announce", null, null);
		p.addArgument("--info-hash", String.class, null);
		p.addArgument("--peer-id", String.class, null);
		p.addArgument("--tracker", String.class, null);
		p.addArgument("--udp", null, null);
		p.addArgument("--http", null, null);
		p.addArgument("--numwant", Integer.class, -1);
		p.addArgument("--ip", String.class, "0.0.0.0");
		p.addArgument("--port", Integer.class, null);
		p.addArgument("--event", String.class, "started");
		
		p.addArgument("--stop-server", null, null);

		p.parse(args);

		if (p.isPresent("--setup-help"))
		{
			setupHelp();
			return;
		}

		int type = p.oneOfTheseOrUsage("--server", "--proxy", "--announce", "--stop-server");
		if (type == 0)
		{
			Server.start(p.getOrUsage("--server-port", Integer.class), p.getOrUsage("--password", String.class));
		} else if (type == 1)
		{
			String host_str = p.getOrUsage("--host", String.class);
			String tracker_str = p.getOrUsage("--tracker", String.class);

			String host = Utils.getHost(host_str);
			int host_port = Utils.getPort(host_str);

			String tracker = Utils.getHost(tracker_str);
			int tracker_port = Utils.getPort(tracker_str);

			Client.start(p.getOrUsage("--proxy-port", Integer.class), host, host_port, tracker, tracker_port);
		} else if (type == 2)
		{
			TrackerAnnounceRequest req = new TrackerAnnounceRequest();
			req.info_hash = p.getOrUsage("--info-hash", String.class);
			req.ip = p.getOrUsage("--ip", String.class);
			req.port = p.getOrUsage("--port", Integer.class);
			req.peer_id = p.getOrUsage("--peer-id", String.class);
			req.numwant = p.getOrUsage("--numwant", Integer.class);
			req.event = p.getOrUsage("--event", String.class);

			TrackerAnnounceResponse response;
			if (p.isPresent("--udp"))
			{
				String tracker_str = p.getOrUsage("--tracker", String.class);
				String tracker_host = Utils.getHost(tracker_str);
				int tracker_port = Utils.getPort(tracker_str);
				
				try
				{
					UDPTracker tracker = new UDPTracker(tracker_host, tracker_port);
					response = tracker.request(req);
				} catch (UnresolvedAddressException uae)
				{
					System.out.println("Unresolved address: " + tracker_host);
					return;
				} catch (SocketTimeoutException soe)
				{
					System.out.println("Tracker did not respond.");
					return;
				}
			} else if (p.isPresent("--http"))
			{
				HTTPTracker tracker = new HTTPTracker(p.getOrUsage("--tracker", String.class));
				response = tracker.request(req);
			} else
			{
				System.out.println("To announce, use either --udp or --http");
				p.usage();
				System.exit(0);
				return;
			}

			System.out.println("Announce Response:");
			System.out.println(response.toNiceString());
		} else if (type == 3)
		{
			try
			{
				String host_str = p.getOrUsage("--host", String.class);
				String passwordMD5 = Utils.md5(p.getOrUsage("--password", String.class));
				
				String host = Utils.getHost(host_str);
				int host_port = Utils.getPort(host_str);
				
				Socket socket = new Socket(host, host_port);
				DataOutputStream dos = Utils.wrapDOS(socket.getOutputStream());
				DataInputStream dis = Utils.wrapDIS(socket.getInputStream());
				
				dos.writeInt(ClientHandler.ACTION_STOP);
				dos.writeUTF(passwordMD5);
				dos.flush();
				
				boolean success = dis.readBoolean();
				dis.close();
				dos.close();
				socket.close();
				
				if (success)
				{
					System.out.println("Server was succesfully stopped");
				} else
				{
					System.out.println("Invalid halt password!");
				}
			} catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}

	}
}
