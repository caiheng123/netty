package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println(
                    "Usage: " + EchoServer.class.getSimpleName() + "<port>"
            );
            int port = Integer.parseInt(args[0]);
            new EchoServer(port).start();
        }
    }

    public void start() throws Exception {
        final EchoServerHandler echoServerHandler = new EchoServerHandler();
        EventLoopGroup group = new NioEventLoopGroup();//1.创建EventLoopGroup
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();//2.创建ServerBootstrap
            serverBootstrap.group(group)
                    .channel(NioServerSocketChannel.class)//3.指定所使用NIO传输的Channel
                    .localAddress(new InetSocketAddress(port))//4.使用指定的端口设置套接字地址
                    .childHandler(new ChannelInitializer<SocketChannel>() {//5.添加一个EchoServerHandler到Channel的ChannelPipeline
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast();//EchoServerHandler被标注为@Shareable，所以我们可以总是使用同样的实例
                        }
                    });
            ChannelFuture sync = serverBootstrap.bind().sync();//6.异步绑定服务器，调用sync()方法阻塞等到直到绑定完成
            sync.channel().closeFuture().sync();//7.获取Channel的closeFuture,并且阻塞当前线程直至它完成
        }finally {
            group.shutdownGracefully().sync();//8.关闭 EventLoopGroup,释放所有的资源
        }
    }
}
