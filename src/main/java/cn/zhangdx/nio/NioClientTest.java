package cn.zhangdx.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

@Slf4j
public class NioClientTest {

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8080));
        log.debug("waiting...");
        socketChannel.write(Charset.defaultCharset().encode("测试发送的文本\n你好\n"));
        System.in.read();
    }
}
