
import static io.netty.buffer.Unpooled.copiedBuffer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

//
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
public class NettyHttpServer
{
  private ChannelFuture channel;
  private final EventLoopGroup masterGroup;
  private final EventLoopGroup slaveGroup;
    
  public NettyHttpServer()

  
  {
	  String reqURL="";
    masterGroup = new NioEventLoopGroup();
    slaveGroup = new NioEventLoopGroup();        
  }

  public void start() // #1
  {
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      @Override
      public void run() { shutdown(); }
    });
        
    try
    {
      // #3
      final ServerBootstrap bootstrap =
        new ServerBootstrap()
          .group(masterGroup, slaveGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() // #4
          {
            @Override
            public void initChannel(final SocketChannel ch)
              throws Exception
            {
              ch.pipeline().addLast("codec", new HttpServerCodec());
              ch.pipeline().addLast("aggregator",
                new HttpObjectAggregator(512*1024));
              ch.pipeline().addLast("request",
                new ChannelInboundHandlerAdapter() // #5
              {

				@Override
                public void channelRead(ChannelHandlerContext ctx, Object msg)
                  throws Exception
                {
                  if (msg instanceof FullHttpRequest)
                  {
                    final FullHttpRequest request = (FullHttpRequest) msg;
                 String  reqURL=request.getUri();
                                     System.out.println("REQUEST IS ==>"+reqURL)      ; 
                    final String responseMessage = "Hai this is karthikeyan, i just created the netty server using NETTY!";
                
                                            
                    FullHttpResponse response = new DefaultFullHttpResponse(
                      HttpVersion.HTTP_1_1,
                      HttpResponseStatus.OK,
                      copiedBuffer(responseMessage.getBytes())
                    );
    
                    if (HttpHeaders.isKeepAlive(request))
                    {
                      response.headers().set(
                        HttpHeaders.Names.CONNECTION,
                        HttpHeaders.Values.KEEP_ALIVE
                      );
                    }
                    response.headers().set(HttpHeaders.Names.CONTENT_TYPE,"text/plain");
                    response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,responseMessage.length());
                                        
                    ctx.writeAndFlush(response);
                	
                   // putDataInKafka();
                  //  putDataIntoKafka(request.getUri());
                    
                    Kafkadata kdata=new Kafkadata(request.getUri());
                                     }
                  else
                  {
                    super.channelRead(ctx, msg);
                  }
                }
    
				

			



				@Override
                public void channelReadComplete(ChannelHandlerContext ctx)
                  throws Exception
                {
                  ctx.flush();
                }
    
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx,
                  Throwable cause) throws Exception
                {
                  ctx.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    copiedBuffer(cause.getMessage().getBytes())
                  ));
                }                                    
              });
            }
          })
          .option(ChannelOption.SO_BACKLOG, 8090)
          .childOption(ChannelOption.SO_KEEPALIVE, true);
      channel = bootstrap.bind(7004).sync();
    }
    catch (final InterruptedException e) { }
  }
    
  public void shutdown() // #2
  {
    slaveGroup.shutdownGracefully();
    masterGroup.shutdownGracefully();

    try
    {
      channel.channel().closeFuture().sync();
    }
    catch (InterruptedException e) { }
  }

  public static void main(String[] args)
  {
    new NettyHttpServer().start();
    
    
   
  }


}
