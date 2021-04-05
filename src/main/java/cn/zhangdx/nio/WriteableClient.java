package cn.zhangdx.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
public class WriteableClient {

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8081));
        int count = 0;
        while (true) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
            int read = socketChannel.read(byteBuffer);
            count += read;
            System.out.println(count);
            byteBuffer.clear();
        }
    }
}
