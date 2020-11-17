package chat.heartRate;

import chat.groupChat.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class HeartBeatServer {
    private int port;

    public HeartBeatServer(int port) {
        this.port = port;
    }
    public void run() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,128)
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        /* 当连接没有读写操作，会触发一个IdleStateEvent事件（心跳检测机制 ），处理空闲状态的处理器
                        * readerIdleTime:读，表示多长时间没有读，就会发送一个心跳检测包检测是否连接
                        * writerIdleTime:写，表示多长时间没有写，就会发送一个心跳检测包检测是否连接
                        * allIdleTime:读写
                        * 当IdleStateEvent触发后，就会传递给管道pipeline的下一个handler去处理（userEventTrigger）
                        * */
                        pipeline.addLast(new IdleStateHandler(3,5,7, TimeUnit.SECONDS));
                        pipeline.addLast(new HeartServerHandler());
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(port).sync();
        channelFuture.channel().closeFuture().sync();
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
    public static void main(String[] args) throws InterruptedException {
        new HeartBeatServer(9600).run();
    }
}
