package constants;

public class UDPConstants {
    // 公用头部
    public final static byte[] HEADER = new byte[]{7, 7, 7, 7, 7, 7, 7, 7};
    // 服务器固化UDP接收端口
    public final static int SERVER_PORT = 30201;
    // 客户端回送端口
    public final static int CLIENT_RESPONSE_PORT = 30202;

    public final static int MIN_LENGTH = UDPConstants.HEADER.length + 2 + 4;
}
