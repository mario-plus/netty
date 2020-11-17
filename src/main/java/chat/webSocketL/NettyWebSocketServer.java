package chat.webSocketL;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyWebSocketServer {
    private int port;

    public NettyWebSocketServer(int port) {
        this.port = port;
    }
    public void run() throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,128)
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //基于Http协议，使用http的编码和解码器
                        pipeline.addLast(new HttpServerCodec());
                        //以块的方式写，添加ChunkedWriteHandler处理器
                        pipeline.addLast(new ChunkedWriteHandler());
                        /*
                        * http数据在传输过程中是分段，发送大量数据时就会发出多次http请求
                        * HttpObjectAggregator：将多个段聚合
                        * */
                        pipeline.addLast(new HttpObjectAggregator(8192));
                        /*
                        * 对于websocket，是以数据帧传输的（WebSocketFrame）
                        * WebSocketServerProtocolHandler：核心功能，将http协议升级为ws协议，保持长连接
                        * */
                        pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
                        pipeline.addLast(new NettyWsServerHandler());//自定义handler
                    }
                });
        System.out.println("netty server is started");
        ChannelFuture channelFuture = bootstrap.bind(port).sync();
        channelFuture.channel().closeFuture().sync();
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        new NettyWebSocketServer(9600).run();
    }
}
