package com.martijncourteaux.spooferbt.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class Utils
{

	public static DataInputStream wrapDIS(InputStream is)
	{
		return new DataInputStream(new BufferedInputStream(is));
	}

	public static DataOutputStream wrapDOS(OutputStream os)
	{
		return new DataOutputStream(new BufferedOutputStream(os));
	}

	public static void pipe(InputStream in, OutputStream os) throws IOException
	{
		byte[] buffer = new byte[8192];
		int n;
		while ((n = in.read(buffer)) != -1)
		{
			os.write(buffer, 0, n);
		}
	}

	public static byte[] getPaddedBytes(String str, int size)
	{
		try
		{
			byte[] bytes = new byte[size];
			byte[] strBytes = str.getBytes("UTF-8");
			System.arraycopy(strBytes, 0, bytes, size - strBytes.length, strBytes.length);
			return bytes;
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	public static byte[] encodeInfoHash(String info_hash)
	{
		byte[] bytes = new byte[20];
		for (int i = 0; i < 20; ++i)
		{
			String p = info_hash.substring(i * 2, i * 2 + 2);
			bytes[i] = (byte) Integer.parseInt(p, 16);
		}
		return bytes;
	}

	public static boolean arraysEqualsAtStart(byte[] data1, byte[] data2, int length)
	{
		for (int i = 0; i < length; ++i)
		{
			if (data1[i] != data2[i])
			{
				return false;
			}
		}
		return true;
	}

	public static String getHost(String str)
	{
		return str.split(":")[0];
	}

	public static int getPort(String str)
	{
		return Integer.parseInt(str.split(":")[1]);
	}

	public static String md5(String password)
	{
		try
		{
			MessageDigest d = MessageDigest.getInstance("MD5");
			d.update(password.getBytes("UTF-8"));
			byte[] hash = d.digest();
			BigInteger bi = new BigInteger(hash);
			return bi.toString(16);
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}
}
