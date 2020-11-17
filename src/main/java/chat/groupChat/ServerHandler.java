package chat.groupChat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;


public class ServerHandler extends SimpleChannelInboundHandler {
    //channel组，管理所有的channel,GlobalEventExecutor.INSTANCE,一个单例，全局事件执行器
    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    //读取数据
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        channels.forEach(channel -> {
            if (channel!=channelHandlerContext.channel()){
                //不是当前的channel，直接转发
                channel.writeAndFlush("【client:"+channel.remoteAddress()+"】"+o.toString()+"\n");
            }else{
                channel.writeAndFlush(o.toString()+"------------发送成功！"+"\n");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    //表示连接建立，一旦连接，第一个被执行
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channels.writeAndFlush("client"+channel.remoteAddress()+"come in \n" );
        channels.add(channel);
    }
    //表示断开连接
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        Channel channel = ctx.channel();
        channels.writeAndFlush("client"+channel.remoteAddress()+"leave out \n" );
        //channels.remove(channel);不需要，自动移除
        System.out.println("channels size:"+channels.size());
    }

    //表示channel处于活动状态，提示xx上线
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+"login in");
    }

    //表示channel处于非活动状态，提示xx下线
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //super.channelInactive(ctx);
        System.out.println(ctx.channel().remoteAddress()+"login out");
    }

}
