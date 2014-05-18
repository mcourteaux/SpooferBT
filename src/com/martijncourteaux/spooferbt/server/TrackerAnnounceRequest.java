package com.martijncourteaux.spooferbt.server;

public class TrackerAnnounceRequest
{
	
	public TrackerAnnounceRequest()
	{
		this(null, null, null, 0, 0, 0, 0, "", -1);
	}
	
	public TrackerAnnounceRequest(String peer_id, String info_hash, String ip, int port, long uploaded, long downloaded, long left, String event, int numwant)
	{
		super();
		this.peer_id = peer_id;
		this.info_hash = info_hash;
		this.ip = ip;
		this.port = port;
		this.downloaded = downloaded;
		this.uploaded = uploaded;
		this.left = left;
		this.event = event;
		this.numwant = numwant;
	}

	public String peer_id;
	public String info_hash;
	public String ip;
	public int port;
	public long uploaded;
	public long downloaded;
	public long left;
	public String event;
	public int numwant;
	

}
