package server;

import client.bean.ClientInfo;
import client.exception.UDPSearcherException;
import constants.UDPConstants;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ServerUtil {

    private final static int MIN_LENGTH = UDPConstants.MIN_LENGTH;
    private final static byte[] HEADER = UDPConstants.HEADER;

    public static ClientInfo unwrap(DatagramPacket receivedPacket) throws UDPSearcherException {
        if (receivedPacket.getData().length < MIN_LENGTH) {
            throw new UDPSearcherException("Received packet is invalid");
        }
        byte[] data = receivedPacket.getData();
        ByteBuffer receivedBuffer = ByteBuffer.wrap(data, 0, data.length);
        byte[] header = new byte[HEADER.length];
        receivedBuffer.get(header, 0, HEADER.length);
        if (!Arrays.equals(header, HEADER)) {
            throw new UDPSearcherException("Received packet header is invalid");
        }
        return new ClientInfo(receivedPacket.getAddress().getHostAddress(), receivedPacket.getPort());
    }

    static ByteBuffer wrap(byte[] buffer, int port, byte[] sn) {
        ByteBuffer sendBuffer = ByteBuffer.wrap(buffer);
        sendBuffer.put(HEADER);
        sendBuffer.putShort((short) 2);
        sendBuffer.putInt(port);
        sendBuffer.put(sn);
        return sendBuffer;
    }
}
