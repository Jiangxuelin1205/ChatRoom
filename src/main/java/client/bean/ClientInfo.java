package client.bean;

import java.net.InetAddress;

public class ClientInfo {

    public InetAddress ip;
    public int port;

    public ClientInfo(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}