SpooferBT
=========
Relay UDP torrent tracker traffic via TCP.

Intro
-----

Normally, tracker communication goes through UDP. But in
some companies or schools, all UDP traffic is blocked by
the network admins. However, TCP/IP traffic is of course
not blocked. Hopefully you can connect with any external
host. If so, we can setup a proxy system that will allow
you to do the tracker communication by using TCP instead
of UDP, which hopefully not blocked.

So normally you have:
```
  [Torrent Client]     <--- UDP --->     [Tracker]
```
But this application does this:
```
  [Torrent Client]  <-X- UDP Blocked --->  [Tracker]
          |                                    |
          |                                    |
     Local UDP                                UDP
          |                                    |
          |                                    |
  [SpooferBT Proxy]    <--- TCP --->  [SpooferBT Server]
                                           or Host
```

So, once this setup is created. You should add the proxy
to the torrent client's list of trackers. Peers will now
be acquired by this application.

Good luck!

Example run commands
--------------------
Run the localhost "fake" proxy tracker:
```
java -jar SpooferBT.jar --proxy --proxy-port 12345 --host [ip_here]:[portA] --tracker tracker.publicbt.com:80
```
Run the auxiliary server application that runs on IP address ```[ip_here]```:
```
java -jar SpooferBT.jar --server --server-port [portA]
```
Now you can add ```udp://localhost:12345``` to your tracker list in the torrent client.
