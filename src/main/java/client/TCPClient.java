package client;

import client.bean.ServerInfo;
import clink.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

class TCPClient {

    private static Logger log = Logger.getLogger("TCPClient");

    private final Socket socket;
    private final ReadHandler readHandler;
    private final PrintStream printStream;

    private TCPClient(Socket socket, ReadHandler readHandler) throws IOException {
        this.socket = socket;
        this.readHandler = readHandler;
        this.printStream = new PrintStream(socket.getOutputStream());
    }

    void exit() {
        readHandler.exit();
        CloseUtils.close(printStream);
        CloseUtils.close(socket);
    }

    void send(String msg) {
        printStream.println(msg);
    }

    static TCPClient startWith(ServerInfo info) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()), 3000);

        log.info("已发起服务器连接，并进入后续流程～");
        log.info("客户端信息：" + socket.getLocalAddress() + " P:" + socket.getLocalPort());
        log.info("服务器信息：" + socket.getInetAddress() + " P:" + socket.getPort());

        try {
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();
            return new TCPClient(socket, readHandler);
        } catch (Exception e) {
            CloseUtils.close(socket);
            throw new TCPClientException("连接异常");
        }
    }


    static class ReadHandler extends Thread {
        private boolean done = false;
        private final InputStream inputStream;

        ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            super.run();
            try {
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    String str;
                    try {
                        str = socketInput.readLine();
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                    if (str == null) {
                        log.info("连接已关闭，无法读取数据！");
                        break;
                    }
                    System.out.println(str);
                } while (!done);
            } catch (Exception e) {
                throw new TCPClientException("连接异常断开：" + e.getMessage());
            } finally {
                CloseUtils.close(inputStream);
            }
        }

        void exit() {
            done = true;
            CloseUtils.close(inputStream);
        }
    }
}
