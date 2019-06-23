package server;

import constants.TCPConstants;
import clink.core.IoContext;
import clink.impl.IoSelectorProvider;
import server.exception.TCPServerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {

    private final static int TCP_SERVER_PORT=TCPConstants.PORT_SERVER;
    
    public static void main(String[] args) throws IOException {
        IoContext.setup()
                .ioProvider(new IoSelectorProvider())
                .start();

        TCPServer tcpServer = new TCPServer(TCP_SERVER_PORT);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
           throw new TCPServerException("tcp server start unsucceed");
        }
        UDPProvider.start();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();
            tcpServer.broadcast(str);
        } while (!str.equalsIgnoreCase("00bye00"));

        UDPProvider.stop();
        tcpServer.stop();

        IoContext.close();
    }
}

