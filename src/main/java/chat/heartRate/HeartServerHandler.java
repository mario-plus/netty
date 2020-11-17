package chat.heartRate;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt; //READER_IDLE,WRITER_IDLE, ALL_IDLE;
            String type = null;
            switch (idleStateEvent.state()){
                case ALL_IDLE:
                    type="读写空闲！";break;
                case READER_IDLE:
                    type="读空闲！";break;
                case WRITER_IDLE:
                    type="写空闲！";break;
            }
            System.out.println(ctx.channel().remoteAddress()+"-----timeout--------"+type);
           // ctx.channel().close();
        }
    }
}
