package server;

import constants.TCPConstants;
import constants.UDPConstants;
import clink.utils.ByteUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.logging.Logger;

class UDPProvider {

    private static Provider PROVIDER_INSTANCE;
    private final static int TCP_SERVER_PORT = TCPConstants.PORT_SERVER;
    private static Logger log = Logger.getLogger("UDPProvider");

    static void start() {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        Thread thread = new Thread(provider);
        thread.start();
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
        private DatagramSocket ds = null;
        // 存储消息的Buffer
        final byte[] buffer = new byte[128];

        Provider(String sn) {
            super();
            this.sn = sn.getBytes();
            this.port = TCP_SERVER_PORT;
        }

        @Override
        public void run() {
            log.info("UDP Provider started");
            try {
                // 监听20000 端口
                ds = new DatagramSocket(UDPConstants.SERVER_PORT);
                // 接收消息的Packet
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {

                    ds.receive(receivePack);

                    String clientIp = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLen = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);
                    log.info("UDPProvider receive form ip:" + clientIp
                            + "\tport:" + clientPort + "\tdataValid:" + isValid);
                    if (!isValid) {
                        // 无效继续
                        continue;
                    }
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));

                    // 判断合法性
                    if (cmd == 1 && responsePort > 0) {
                        // 构建一份回送数据
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short) 2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();
                        // 直接根据发送者构建一份回送信息
                        DatagramPacket responsePacket = new DatagramPacket(buffer,
                                len,
                                receivePack.getAddress(),
                                responsePort);
                        ds.send(responsePacket);
                        log.info("UDP Provider response to:" + clientIp +
                                "\tport:" + responsePort + "\tdataLen:" + len);
                    } else {
                        log.info("UDP Provider receive cmd nonsupport; cmd:"
                                + cmd + "\tport:" + port);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                close();
            }
            log.info("UDP Provider finished");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }
        void exit() {
            done = true;
            close();
        }
    }
}
