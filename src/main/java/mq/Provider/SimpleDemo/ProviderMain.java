package mq.Provider.SimpleDemo;

import io.vertx.core.Vertx;

public class ProviderMain {
    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(MQClientProviderVerticle.class.getName());

    }
}
