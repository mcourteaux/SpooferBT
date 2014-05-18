package com.martijncourteaux.spooferbt.server;

public abstract class Tracker
{

	public abstract TrackerAnnounceResponse request(TrackerAnnounceRequest tr) throws Exception;

}
