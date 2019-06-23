package server;

import client.bean.ClientInfo;
import client.UDPSearcherException;
import clink.utils.ByteUtils;
import constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

class ServerUtil {

    private final static byte[] HEADER = UDPConstants.HEADER;

    static ClientInfo unwrap(DatagramSocket datagramSocket) throws UDPSearcherException, IOException {
        final byte[] buffer = new byte[128];
        DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(receivePack);
        int totalLength = receivePack.getLength();
        //noinspection unused
        String clientIp = receivePack.getAddress().getHostAddress();
        //noinspection unused
        int clientPort = receivePack.getPort();
        byte[] data = receivePack.getData();
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, HEADER.length, totalLength);
        final short cmd = byteBuffer.getShort();
        final int responsePort = byteBuffer.getInt();
          /*  int index = HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));

                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));*/

        if (isValid(totalLength, data, cmd, responsePort)) {
            return new ClientInfo(receivePack.getAddress(), responsePort);
        } else {
            throw new UDPProviderException("inform invalid");
        }
    }

    private static boolean isValid(int totalLength, byte[] data, short cmd, int responsePort) {
        boolean isValidTotalLength = totalLength >= UDPConstants.MIN_LENGTH;
        boolean isValidHeader = ByteUtils.startsWith(data, HEADER);
        if (!isValidHeader && isValidTotalLength) {
            return false;
        }
        if (cmd == 1 && responsePort > 0) {
            return true;
        }
        return true;
    }

    static ByteBuffer wrap(int port, byte[] sn) {
        byte[] buffer=new byte[128];
        ByteBuffer sendBuffer = ByteBuffer.wrap(buffer);
        sendBuffer.put(HEADER);
        sendBuffer.putShort((short) 2);
        sendBuffer.putInt(port);
        sendBuffer.put(sn);
        return sendBuffer;
    }
}
