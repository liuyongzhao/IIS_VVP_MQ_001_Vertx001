package mq.Consumer.SimpleDemo;

import io.vertx.core.Vertx;

public class ConsumerMain {
    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(MQClientConsumerVerticle.class.getName());
    }
}
