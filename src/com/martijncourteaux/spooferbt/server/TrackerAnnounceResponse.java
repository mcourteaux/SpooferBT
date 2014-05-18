package com.martijncourteaux.spooferbt.server;

import java.util.ArrayList;
import java.util.List;

import com.martijncourteaux.spooferbt.common.Peer;

public class TrackerAnnounceResponse
{

	public int action;
	public int transaction_id;
	public int interval;
	public int leechers;
	public int seeders;
	public List<Peer> peers;
	
	public TrackerAnnounceResponse()
	{
		peers = new ArrayList<Peer>();
	}

	@Override
	public String toString()
	{
		return "TrackerAnnounceResponse [action=" + action + ", transaction_id=" + transaction_id + ", interval=" + interval + ", leechers=" + leechers + ", seeders=" + seeders + ", peers=" + peers + "]";
	}
	

	public String toNiceString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("  Action: "); sb.append(action); sb.append('\n');
		sb.append("  Transaction: "); sb.append(transaction_id); sb.append('\n');
		sb.append("  Interval: "); sb.append(interval); sb.append('\n');
		sb.append("  Leechers: "); sb.append(leechers); sb.append('\n');
		sb.append("  Seeders: "); sb.append(seeders); sb.append('\n');
		sb.append("  Peers: "); sb.append(peers.size());
		for (Peer p : peers)
		{
			sb.append("\n    ");
			sb.append(p.toString());
		}
		return sb.toString();
	}
}
