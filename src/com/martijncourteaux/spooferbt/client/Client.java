package com.martijncourteaux.spooferbt.client;

import java.net.InetSocketAddress;

public class Client
{
	private ProxyTracker tracker;
	private InetSocketAddress host;

	public static void start(int port, String host, int host_port, String tracker, int tracker_port)
	{
		Client client = new Client(port, host, host_port);
		client.serveAs(tracker, tracker_port);
	}

	public Client(int port, String host, int host_port)
	{
		this.host = new InetSocketAddress(host, host_port);
		if (this.host.isUnresolved())
		{
			System.out.println("Unresovled host: " + host);
			System.exit(0);
		}
		try
		{
			this.tracker = new ProxyTracker(port, this.host);
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				Client.this.tracker.stop();
			}
		});
		
	}

	public void serveAs(String tracker_host, int tracker_port)
	{
		try
		{
			tracker.setTracker(tracker_host, tracker_port);
			new Thread(this.tracker, "Proxy Server").start();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
