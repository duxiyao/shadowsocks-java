package cn.liaozhonghao.www.shadowsocks.client.channelHandler.inbound;

import cn.liaozhonghao.www.shadowsocks.client.config.ClientContextConstant;
import cn.liaozhonghao.www.shadowsocks.common.cipher.AbstractCipher;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.util.IPAddressUtil;

import java.net.InetSocketAddress;

public class TransferFlowHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private Channel clientChannel;
    private Channel remoteChannel;
    /**
     * static logger
     */
     private static Logger logger = LoggerFactory.getLogger(TransferFlowHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if(clientChannel == null){
            this.clientChannel = ctx.channel(); // 本地通道
        }

        if(remoteChannel == null){
            // 远程通道
            remoteChannel = clientChannel.attr(ClientContextConstant.REMOTE_CHANNEL).get();
        }

        if(remoteChannel == null){
            closeChannel();
            logger.error("can't find proxy channel");
            return;
        }
        // 把从本地通道获取的数据加密后从远程通道发送出去
        remoteChannel.writeAndFlush(getCryptMessage(msg.retain())); // 增加消息引用计数
    }

    /**
     * close channel
     */
    private void closeChannel() {
        if (remoteChannel != null) {
            remoteChannel.close();
            remoteChannel = null;
        }

        if (clientChannel != null) {
            clientChannel.close();
            clientChannel = null;
        }
    }

    /**
     * 加密消息
     *
     * @param message message
     * @return byteBuf
     */
    private ByteBuf getCryptMessage(ByteBuf message) {
        try {
            // 缓存将要加密的数据
            ByteBuf willEncodeMessage = clientChannel.alloc().heapBuffer();

            boolean isFirstEncoding = clientChannel.attr(ClientContextConstant.FIRST_ENCODING).get() == null || clientChannel.attr(ClientContextConstant.FIRST_ENCODING).get();
            if (isFirstEncoding) {
                //获取请求的目的地址
                InetSocketAddress queryAddress = clientChannel.attr(ClientContextConstant.DST_ADDRESS).get();
                if (queryAddress == null) {
                    closeChannel();
                    throw new IllegalArgumentException("no remote address");
                }

                String queryHost = queryAddress.getHostName();
                if (IPAddressUtil.isIPv4LiteralAddress(queryHost)) {
                    willEncodeMessage.writeByte(0x01); //ipv4
                    willEncodeMessage.writeBytes(IPAddressUtil.textToNumericFormatV4(queryHost));
                    willEncodeMessage.writeShort(queryAddress.getPort());
                } else if (IPAddressUtil.isIPv6LiteralAddress(queryHost)) {
                    willEncodeMessage.writeByte(0x04);//ipv6
                    willEncodeMessage.writeBytes(IPAddressUtil.textToNumericFormatV6(queryHost));
                    willEncodeMessage.writeShort(queryAddress.getPort());
                } else {
                    willEncodeMessage.writeByte(0x03);//domain type
                    byte[] asciiHost = AsciiString.of(queryHost).array();
                    willEncodeMessage.writeByte(asciiHost.length);//domain type
                    willEncodeMessage.writeBytes(asciiHost);
                    willEncodeMessage.writeShort(queryAddress.getPort());
                }
            }

            byte[] payload = new byte[message.readableBytes()];
            message.readBytes(payload);
            willEncodeMessage.writeBytes(payload);

            return cryptMessage(willEncodeMessage);
        } finally {
            clientChannel.attr(ClientContextConstant.FIRST_ENCODING).setIfAbsent(false);
        }
    }

    /**
     * cryptMessage
     *
     * @param willEncodeMessage willEncoding Message
     * @return after entry message
     */
    private ByteBuf cryptMessage(ByteBuf willEncodeMessage) {
        try {
            ByteBuf result = clientChannel.alloc().heapBuffer();

            AbstractCipher cipher = clientChannel.attr(ClientContextConstant.SOCKS5_CLIENT_CIPHER).get();
            byte[] originBytes = new byte[willEncodeMessage.readableBytes()];
            willEncodeMessage.readBytes(originBytes);

            //entry
            byte[] secretBytes = cipher.encodeBytes(originBytes);

            result.writeBytes(secretBytes);
            return result;
        } finally {
            ReferenceCountUtil.release(willEncodeMessage);
        }
    }
}
