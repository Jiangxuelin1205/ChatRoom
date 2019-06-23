package server;

import client.UDPSearcherException;
import clink.utils.ByteUtils;
import constants.UDPConstants;

public class Asserts {

    private final static int SERVER_COMMAND = 2;

    public static void assertTotalLength(int totalLength) {
        if (totalLength < UDPConstants.MIN_LENGTH) {
            throw new UDPSearcherException("package length is invalid");
        }
    }

    public static void assertHeader(byte[] data) {
        if (!ByteUtils.startsWith(data, UDPConstants.HEADER)) {
            throw new UDPSearcherException("header is invalid");
        }
    }

    public static void assertCommand(short cmd) {
        if (cmd != SERVER_COMMAND) {
            throw new UDPSearcherException("command is invalid");
        }
    }

    public static void assertTCPServerPort(int tcpServerPort) {
        if (tcpServerPort <= 0) {
            throw new TCPServerException("tcp server port is invalid");
        }
    }

}
