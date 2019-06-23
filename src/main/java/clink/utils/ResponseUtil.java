package clink.utils;

import client.bean.ServerInfo;
import client.exception.UDPSearcherException;
import constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class ResponseUtil {

    private static Logger log = Logger.getLogger("ResponseUtil");
    private final static int MIN_LENGTH = UDPConstants.MIN_LENGTH;
    private final static byte[] HEADER = UDPConstants.HEADER;
    private final static int SERVER_COMMAND=2;

    public static ServerInfo unwrap(DatagramSocket ds) throws UDPSearcherException, IOException {
        byte[] buffer=new byte[128];
        DatagramPacket receivedPacket=new DatagramPacket(buffer,buffer.length);
        ds.receive(receivedPacket);
        int totalLength = receivedPacket.getLength();
        String ip = receivedPacket.getAddress().getHostAddress();
        int udpServerPort = receivedPacket.getPort();
        byte[] data = receivedPacket.getData();
        boolean isValid = totalLength >= MIN_LENGTH && ByteUtils.startsWith(data, HEADER);//todo:分开条件

        log.info("UDPSearcher receive form ip:" + ip
                + "\tport:" + udpServerPort + "\tdataValid:" + isValid);//todo:assert类处理异常

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
