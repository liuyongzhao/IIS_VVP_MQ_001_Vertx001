package mq.Provider.SimpleDemo;

import com.rabbitmq.client.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

//import org.junit.Test;

public class MQClientProviderVerticle extends AbstractVerticle {
    private final static String QUEUE_NAME = "queue-test";
    public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
    protected RabbitMQClient client;
    protected Connection connection;
    protected Channel channel;
    private static final io.vertx.core.logging.Logger LOGGER = LoggerFactory.getLogger(MQClientProviderVerticle.class);

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.get("/").handler(this::connect);

        int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
        server
            .requestHandler(router::accept)
            .listen(portNumber, ar -> {
        if (ar.succeeded()) {
               LOGGER.info("HTTP server running on port " + portNumber);
               System.out.println("http服务在端口监听请求");
               startFuture.complete();
        } else {
               LOGGER.error("Could not start a HTTP server", ar.cause());
               startFuture.fail(ar.cause());
           }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }

    //创建连接
   public void connect(RoutingContext context){
        if (client != null) {
            throw new IllegalStateException("Client already started");
        }
        RabbitMQOptions config = new RabbitMQOptions();
        config.setPort(5672);
        config.setHost("localhost");
        client = RabbitMQClient.create(vertx, config);
        CompletableFuture<Void> latch = new CompletableFuture<>();
        client.start(ar -> {
            if (ar.succeeded()) {
                latch.complete(null);
            } else {
                latch.completeExceptionally(ar.cause());
            }
        });

        ConnectionFactory factory = new ConnectionFactory();
        try {
            channel = factory.newConnection().createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        System.out.println("Connection succeeded!");
        basicPublishTest(client);
       context.response()
               .putHeader("content-type", "application/json; charset=utf-8")
               .end("Rabbit MQ 消息发送成功！");
    }

    //声明队列
    public void basicPublishTest(RabbitMQClient client) {
        client.queueDeclare(QUEUE_NAME,false,false,true,null);

        System.out.println("QueueDeclare succeeded");
        JsonObject message = new JsonObject().put("body", "Hello RabbitMQ, from Vert.x !");
        client.basicPublish("", QUEUE_NAME, message, pubResult -> {
            if (pubResult.succeeded()) {
                System.out.println("Message published !");
            } else {
                pubResult.cause().printStackTrace();
            }
        });
        client.stop(stopResult->{
            if (stopResult.succeeded()) {
                System.out.println("Provider stopped!"+client);
            } else {
                stopResult.cause().printStackTrace();
            }
        });
    }

}






