package mq.Consumer.SimpleDemo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

public class MQClientConsumerVerticle extends AbstractVerticle {
    private final static String QUEUE_NAME = "queue-test";
    private final static String ADDRESS = QUEUE_NAME + ".address";
    protected RabbitMQClient client;
    protected Channel channel;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        //创建连接
        RabbitMQOptions config = new RabbitMQOptions();
        config.setPort(5672);
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
        consumeWithManualAck(vertx,client);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }
    //声明队列并绑定消费者
    public void consumeWithManualAck(Vertx vertx, RabbitMQClient client) {
        this.client.queueDeclare(QUEUE_NAME, false, false, true, null);
        // Create the event bus handler which messages will be sent to
        this.vertx.eventBus().consumer(ADDRESS, msg -> {
            JsonObject json = (JsonObject) msg.body();
            System.out.println("Got message: " + json.getString("body"));
            // ack
            this.client.basicAck(json.getLong("deliveryTag"), false, asyncResult -> {
            });
        });

        // Setup the link between rabbitmq consumer and event bus address
        this.client.basicConsume(QUEUE_NAME, ADDRESS, false, consumeResult -> {
            if (consumeResult.succeeded()) {
                System.out.println("RabbitMQ consumer created !");
            } else {
                consumeResult.cause().printStackTrace();
            }
        });
    }
}
