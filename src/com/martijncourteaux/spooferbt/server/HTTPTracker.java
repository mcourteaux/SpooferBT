package com.martijncourteaux.spooferbt.server;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.martijncourteaux.spooferbt.common.Utils;

public class HTTPTracker extends Tracker
{

	private String trackerURL;
	
	public HTTPTracker(String trackerURL)
	{
		if (!trackerURL.endsWith("/"))
			trackerURL += "/";
		this.trackerURL = trackerURL;

	}

	public TrackerAnnounceResponse request(TrackerAnnounceRequest tr) throws Exception
	{
		String enc = "UTF-8";

		StringBuilder requestURL = new StringBuilder();
		requestURL.append(trackerURL);
		requestURL.append("announce?");

		requestURL.append("info_hash=");
		requestURL.append(URLEncoder.encode(tr.info_hash, enc));
		requestURL.append("&peer_id=");
		requestURL.append(URLEncoder.encode(tr.peer_id, enc));
		requestURL.append("&ip=");
		requestURL.append(tr.ip);
		requestURL.append("&port=");
		requestURL.append(tr.port);
		requestURL.append("&uploaded=");
		requestURL.append(tr.uploaded);
		requestURL.append("&downloaded=");
		requestURL.append(tr.downloaded);
		requestURL.append("&left=");
		requestURL.append(tr.left);
		requestURL.append("&event=");
		requestURL.append(tr.event);
		requestURL.append("&numwant=");
		requestURL.append(tr.numwant);

		System.out.println("Request URL: " + requestURL.toString());
		URL url = new URL(requestURL.toString());
		URLConnection conn = url.openConnection();
		InputStream in = conn.getInputStream();
		System.out.println("Connected.");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Utils.pipe(in, baos);
		baos.close();
		in.close();

		String response = baos.toString("UTF-8");

		System.out.println("Tracker Response: " + response);

		return null;
	}

}
