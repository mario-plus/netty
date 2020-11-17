package chat.groupChat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class GroupChatClient {
    private final String host;
    private final int port;

    public GroupChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws InterruptedException {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast("decoder",new StringDecoder());//编码器
                pipeline.addLast("encoder",new StringEncoder());//解码器
                pipeline.addLast(new ClientHandler());
            }
        });
        ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
       // channelFuture.channel().closeFuture().sync();
        Channel channel = channelFuture.channel();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            channel.writeAndFlush(scanner.nextLine()+"\r\n");
        }
        eventExecutors.shutdownGracefully();
    }
    public static void main(String[] args) throws InterruptedException {
        new GroupChatClient("127.0.0.1",9600).run();
    }

}
