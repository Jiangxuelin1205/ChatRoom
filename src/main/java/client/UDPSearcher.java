package client;

import client.bean.ServerInfo;
import client.exception.UDPSearcherException;
import clink.utils.RequestUtil;
import clink.utils.ResponseUtil;
import constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class UDPSearcher {

    private static Logger log = Logger.getLogger("udpSearcher");
    private static final int CLIENT_RESPONSE_PORT = UDPConstants.CLIENT_RESPONSE_PORT;
    private static final int SERVER_PORT = UDPConstants.SERVER_PORT;

    static ServerInfo searchServer(int timeout) {
        log.info("UDPSearcher Started");
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener;
        try {
            listener = new Listener(CLIENT_RESPONSE_PORT, startLatch, receiveLatch);
            Thread listening = new Thread(listener);
            listening.start();
            startLatch.await();
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new UDPSearcherException("broadcast failed");
        }
        log.info("UDPSearcher Finished.");
        return listener.getServerAndClose();
    }

    private static void sendBroadcast() throws IOException {
        log.info("UDPSearcher sendBroadcast started.");
        // 作为搜索方，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket();
        byte[] header = RequestUtil.wrap(CLIENT_RESPONSE_PORT);
        DatagramPacket requestPacket = new DatagramPacket(header, header.length);
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPacket.setPort(SERVER_PORT);
        ds.send(requestPacket);
        ds.close();
        log.info("UDPSearcher sendBroadcast finished.");
    }

    private static class Listener implements Runnable {

        private final int listenPort;
        private final CountDownLatch startDownLatch;
        private final CountDownLatch receiveDownLatch;
        ServerInfo serverInfo;
        private boolean done = false;
        private DatagramSocket ds;

        private Listener(int listenPort, CountDownLatch startDownLatch, CountDownLatch receiveDownLatch) {
            super();
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveDownLatch;
        }

        @Override
        public void run() {
            startDownLatch.countDown();
            try {
                ds = new DatagramSocket(listenPort);

                while (!done) {
                   serverInfo =ResponseUtil.unwrap(ds);
                    receiveDownLatch.countDown();
                }
            } catch (IOException ignore) {
//                throw new UDPSearcherException("socket invalid");
            } finally {
                close();
            }
            log.info("UDPSearcher listener finished.");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        ServerInfo getServerAndClose() {
            done = true;
            close();
            return serverInfo;
        }
    }
}

