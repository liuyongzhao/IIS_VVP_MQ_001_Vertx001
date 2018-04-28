import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import mq.Consumer.SimpleDemo.MQClientConsumerVerticle;
import mq.Provider.SimpleDemo.MQClientProviderVerticle;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;

public class TestVerticle {
    @RunWith(VertxUnitRunner.class)
    public class TestHelloWorldVerticle {
        private Vertx vertx;
        private int port;

        @Rule
        public Timeout timeoutRule = Timeout.seconds(100);

        @Before
        public void setUp(TestContext context) throws IOException {
            ServerSocket socket = new ServerSocket(0);
            port = socket.getLocalPort();
            socket.close();

            DeploymentOptions options = new DeploymentOptions();
            options.setConfig(new JsonObject().put("http.port", port));

            vertx = Vertx.vertx();
            vertx.deployVerticle(MQClientProviderVerticle.class.getName(), options, context.asyncAssertSuccess());
            vertx.deployVerticle(MQClientConsumerVerticle.class.getName(), options, context.asyncAssertSuccess());
        }

        @After
        public void tearDown(TestContext context){
            vertx.close(context.asyncAssertSuccess());
        }

        @Test
        public void testHelloWorld(TestContext context){
            final Async async = context.async();
            System.out.println("["+ LocalDateTime.now().toString()+"]:<testHelloWorld> Start.");

            vertx.createHttpClient().getNow(port, "localhost", "/",
                    response -> {
                        response.bodyHandler(body -> {
                            context.assertEquals("Rabbit MQ 消息发送成功！",body.toString());
                            async.complete();
                            System.out.println("["+LocalDateTime.now().toString()+"]:<test> Success.");
                        });
                    });
        }
    }
}
