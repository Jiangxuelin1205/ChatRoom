package clink.utils;

import client.bean.ServerInfo;
import client.exception.UDPSearcherException;
import constants.UDPConstants;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

public class ResponseUtil {

    private static Logger log = Logger.getLogger("ResponseUtil");
    private final static int MIN_LENGTH = UDPConstants.MIN_LENGTH;
    private final static byte[] HEADER = UDPConstants.HEADER;
    private final static int SERVER_COMMAND=2;

    public static ServerInfo unwrap(byte[] buffer, DatagramPacket receivePacket) throws UDPSearcherException {
        int totalLength = receivePacket.getLength();
        String ip = receivePacket.getAddress().getHostAddress();
        int udpServerPort = receivePacket.getPort();
        byte[] data = receivePacket.getData();
        boolean isValid = totalLength >= MIN_LENGTH && ByteUtils.startsWith(data, HEADER);
        log.info("UDPSearcher receive form ip:" + ip//todo:如何处理每一个if 都要抛异常的情况
                + "\tport:" + udpServerPort + "\tdataValid:" + isValid);
        if (!isValid) {
            throw new UDPSearcherException("Server response is invalid");
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, HEADER.length, totalLength);

        final short cmd = byteBuffer.getShort();
        if (cmd != SERVER_COMMAND) {
            throw new UDPSearcherException("cmd is invalid");
        }

        final int tcpServerPort = byteBuffer.getInt();
        if (invalidPort(tcpServerPort)) {
            throw new UDPSearcherException("tcpServerPort is invalid");
        }
        String sn = new String(buffer, MIN_LENGTH, totalLength - MIN_LENGTH);
        return new ServerInfo(tcpServerPort, ip, sn);
    }

    private static boolean invalidPort(int tcpServerPort) {
        return tcpServerPort < 0;
    }

    public static ByteBuffer wrap(byte[] buffer,int port,byte[] sn) {
        ByteBuffer sendBuffer = ByteBuffer.wrap(buffer);
        sendBuffer.put(HEADER);
        sendBuffer.putShort((short) 2);
        sendBuffer.putInt(port);
        sendBuffer.put(sn);
        return sendBuffer;
    }
}
