package cn.zhangdx.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

@Slf4j
public class WriteableServer {

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);
        serverSocketChannel.bind(new InetSocketAddress(8081));

        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = ssc.accept();
                    socketChannel.configureBlocking(false);
                    SelectionKey selectionKey1 = socketChannel.register(selector, SelectionKey.OP_READ, null);
                    String content = getContent();
                    ByteBuffer byteBuffer = Charset.defaultCharset().encode(content);

                    int write = socketChannel.write(byteBuffer);
                    System.out.println("当前写入：" + write);
                    // 如果没写入完成，继续监听写入事件
                    if (byteBuffer.hasRemaining()) {
                        selectionKey1.interestOps(selectionKey1.interestOps() + SelectionKey.OP_WRITE);
                        selectionKey1.attach(byteBuffer);
                    }
                } else if (selectionKey.isWritable()) {
                    ByteBuffer newByteBuffer = (ByteBuffer) selectionKey.attachment();
                    SocketChannel newSc = (SocketChannel) selectionKey.channel();
                    int write = newSc.write(newByteBuffer);
                    System.out.println("当前写入：" + write);
                    // 写完清理操作
                    if (!newByteBuffer.hasRemaining()) {
                        selectionKey.attach(null);
                        selectionKey.interestOps(selectionKey.interestOps() - SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }

    private static String getContent() {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < 5000000; i++) {
            stringBuffer.append("a");
        }
        return stringBuffer.toString();
    }
}
