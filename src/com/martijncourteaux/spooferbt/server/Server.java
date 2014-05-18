package com.martijncourteaux.spooferbt.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

import com.martijncourteaux.spooferbt.common.Utils;

public class Server implements Runnable
{

	private ServerSocket serverSocket;
	private volatile boolean running;
	private String passwordMD5;

	public static void start(int port, String password) throws IOException
	{
		Server server = new Server(port, password);
		Thread t = new Thread(server, "Server");
		t.start();
	}

	private Server(int port, String password) throws IOException
	{
		this.serverSocket = new ServerSocket(port);
		this.passwordMD5 = Utils.md5(password);
		if (password.isEmpty())
			System.out.println("Using blank halt password!");
	}

	@Override
	public void run()
	{
		running = true;
		System.out.println("Server is running at port: " + serverSocket.getLocalPort());
		try
		{
			serverSocket.setSoTimeout(3000);

			while (running)
			{
				try
				{
					Socket socket = serverSocket.accept();
					System.out.println();
					System.out.println(new Date());
					System.out.println("Connection created: " + socket);
					ClientHandler handler = new ClientHandler(this, socket);
					Thread t = new Thread(handler);
					t.start();
				} catch (SocketTimeoutException soe)
				{
					if (!running)
					{
						System.out.println("Bye!");
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			new RuntimeException(e);
		}
	}

	public void stop()
	{
		running = false;
		System.out.println("Halting within 3 seconds...");
	}

	public void exceptionFromHandler(ClientHandler handler, Exception e)
	{
		e.printStackTrace();
	}

	public String getHaltPasswordMD5()
	{
		return passwordMD5;
	}
}
