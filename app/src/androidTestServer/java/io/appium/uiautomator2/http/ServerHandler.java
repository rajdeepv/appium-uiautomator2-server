package io.appium.uiautomator2.http;

import java.util.List;
import java.util.logging.Level;

import io.appium.uiautomator2.http.impl.NettyHttpRequest;
import io.appium.uiautomator2.http.impl.NettyHttpResponse;
import io.appium.uiautomator2.utils.Logger;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ServerHandler.class.getName());
    private List<io.appium.uiautomator2.http.IHttpServlet> httpHandlers;

    public ServerHandler(List<io.appium.uiautomator2.http.IHttpServlet> handlers) {
        this.httpHandlers = handlers;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Logger.info("channel read invoked!");
        if (!(msg instanceof FullHttpRequest)) {
            return;
        }

        FullHttpRequest request = (FullHttpRequest) msg;
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().add("Connection", "close");

        Logger.info("channel read: " + request.getMethod().toString() + " " + request.getUri());

        io.appium.uiautomator2.http.IHttpRequest httpRequest = new NettyHttpRequest(request);
        io.appium.uiautomator2.http.IHttpResponse httpResponse = new NettyHttpResponse(response);

        for (io.appium.uiautomator2.http.IHttpServlet handler : httpHandlers) {
            handler.handleHttpRequest(httpRequest, httpResponse);
            if (httpResponse.isClosed()) {
                break;
            }
        }

        if (!httpResponse.isClosed()) {
            httpResponse.setStatus(404);
            httpResponse.end();
        }

        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.log(Level.SEVERE, "Error handling request", cause);
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }
}
