package clink.utils;


import client.bean.ClientInfo;
import client.exception.UDPSearcherException;
import constants.UDPConstants;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RequestUtil {

    private final static int MIN_LENGTH=UDPConstants.MIN_LENGTH;
    private final static byte[] HEADER=UDPConstants.HEADER;

    public static byte[] wrap(int listenPort) {
        ByteBuffer sendBuffer = ByteBuffer.allocate(HEADER.length + Short.BYTES + Integer.BYTES);
        sendBuffer.put(HEADER);
        sendBuffer.putShort((short) 1);
        sendBuffer.putInt(listenPort);
        return Arrays.copyOf(sendBuffer.array(), sendBuffer.position() + 1);
    }

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
}
