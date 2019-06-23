package client;

import client.bean.ServerInfo;
import constants.UDPConstants;
import server.Asserts;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;

class ClientUtil {

    private final static int MIN_LENGTH=UDPConstants.MIN_LENGTH;
    private final static byte[] HEADER=UDPConstants.HEADER;

    static byte[] wrap(@SuppressWarnings("SameParameterValue") int listenPort) {
        ByteBuffer sendBuffer = ByteBuffer.allocate(HEADER.length + Short.BYTES + Integer.BYTES);
        sendBuffer.put(HEADER);
        sendBuffer.putShort((short) 1);
        sendBuffer.putInt(listenPort);
        return Arrays.copyOf(sendBuffer.array(), sendBuffer.position() + 1);
    }

    static ServerInfo unwrap(DatagramSocket datagramSocket) throws UDPSearcherException, IOException {
        byte[] receivedPacketBuffer = new byte[128];
        DatagramPacket receivedPacket = new DatagramPacket(receivedPacketBuffer, receivedPacketBuffer.length);
        datagramSocket.receive(receivedPacket);
        int totalLength = receivedPacket.getLength();
        String ip = receivedPacket.getAddress().getHostAddress();

        @SuppressWarnings("unused")
        int udpServerPort = receivedPacket.getPort();
        byte[] data = receivedPacket.getData();
        ByteBuffer byteBuffer = ByteBuffer.wrap(receivedPacketBuffer, HEADER.length, totalLength);
        final short cmd = byteBuffer.getShort();
        final int tcpServerPort = byteBuffer.getInt();

        Asserts.assertTotalLength(totalLength);
        Asserts.assertHeader(data);
        Asserts.assertCommand(cmd);
        Asserts.assertTCPServerPort(tcpServerPort);

        String sn = new String(receivedPacketBuffer, MIN_LENGTH, totalLength - MIN_LENGTH);
        return new ServerInfo(tcpServerPort, ip, sn);
    }
}
