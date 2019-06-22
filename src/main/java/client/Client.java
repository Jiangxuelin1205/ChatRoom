package client;

import client.bean.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class Client {

    private static Logger log = Logger.getLogger("Client");

    public static void main(String[] args) {
        int timeout = 10000;
        ServerInfo info = UDPSearcher.searchServer(timeout);
        log.info("Server:"+info);

        if (info != null) {
            TCPClient tcpClient = null;
            try {
                tcpClient = TCPClient.startWith(info);
                write(tcpClient);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (tcpClient != null) {
                    tcpClient.exit();
                }
            }
        }
    }

    private static void write(TCPClient tcpClient) throws IOException {
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        String str;
        do {
            str = input.readLine();
            tcpClient.send(str);
        } while (!str.equalsIgnoreCase("00bye00"));
    }

}

