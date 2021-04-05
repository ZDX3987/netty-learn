package cn.zhangdx.nio;

import com.safframework.bytekit.bytes.ByteBufferBytes;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

@Slf4j
public class NioServerTest {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        SelectionKey selectionKey = serverSocketChannel.register(selector, 0, null);
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register key {}", selectionKey);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        while (true) {
            selector.select();
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey selectedKey = keyIterator.next();
                keyIterator.remove();
                log.debug("accept key {}", selectedKey);
                if (selectedKey.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) selectedKey.channel();
                    SocketChannel socketChannel = channel.accept();
                    socketChannel.configureBlocking(false);
                    log.debug("get channel {}", socketChannel);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                    SelectionKey scKey = socketChannel.register(selector, 0, byteBuffer);
                    scKey.interestOps(SelectionKey.OP_READ);
                } else if (selectedKey.isReadable()) {
                    try {
                        SocketChannel channel = (SocketChannel) selectedKey.channel();
                        ByteBuffer byteBuffer = (ByteBuffer) selectedKey.attachment();
                        int read = channel.read(byteBuffer);
                        if (read == -1) {
                            selectedKey.cancel();
                            log.info("连接结束");
                        } else {
                            splitBuffer(byteBuffer);
                            if (byteBuffer.position() == byteBuffer.limit()) {
                                ByteBuffer newByteBuffer = ByteBuffer.allocate(byteBuffer.capacity() * 2);
                                byteBuffer.flip();
                                newByteBuffer.put(byteBuffer);
                                selectedKey.attach(newByteBuffer);
                            }
                        }
                    } catch (IOException e) {
                        selectedKey.cancel();
                        log.info("连接异常断开");
                    }
                }
            }
        }
    }

    private static void splitBuffer(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            if (source.get(i) == '\n') {
                int length = i +1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                for (int j = 0; j < length; j++) {
                    target.put(source.get(j));
                }
                System.out.println("打印内容：" + ByteBufferBytes.create(target).toString(Charset.defaultCharset()));
            }
        }
        source.compact();
    }
}
