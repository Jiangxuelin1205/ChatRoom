package server;

import client.bean.ClientInfo;
import constants.TCPConstants;
import constants.UDPConstants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.logging.Logger;

class UDPProvider {

    private static Provider PROVIDER_INSTANCE;
    private final static int TCP_SERVER_PORT = TCPConstants.PORT_SERVER;
    private final static int UDP_SERVER_PORT = UDPConstants.SERVER_PORT;
    private static Logger log = Logger.getLogger("UDPProvider");

    static void start() {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        Thread providerThread = new Thread(provider);
        providerThread.start();
        PROVIDER_INSTANCE = provider;
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider implements Runnable {
        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;


        Provider(String sn) {
            super();
            this.sn = sn.getBytes();
            this.port = TCP_SERVER_PORT;
        }

        @Override
        public void run() {
            log.info("UDP Provider started");
            try {

                datagramSocket = new DatagramSocket(UDP_SERVER_PORT);


                while (!done) {
                    // 判断合法性
                    ClientInfo clientInfo = ServerUtil.unwrap(datagramSocket);
                    ByteBuffer sendBuffer = ServerUtil.wrap(port, sn);
                    int len = sendBuffer.position();
                    DatagramPacket responsePacket = new DatagramPacket(sendBuffer.array(),
                            len,
                            clientInfo.ip,
                            clientInfo.port);
                    datagramSocket.send(responsePacket);
                }
            } catch (
                    Exception ignored)

            {

            } finally

            {
                close();
            }
            log.info("UDP Provider finished");
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        void exit() {
            done = true;
            close();
        }
    }
}
