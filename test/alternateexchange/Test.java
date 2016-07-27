/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alternateexchange;

import test.CalculatorImpl;
import java.io.FileReader;
import java.util.Properties;
import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;
import omq.supervisor.OmqSettings;
import omq.supervisor.SlowRemoteBroker;
import omq.supervisor.Supervisor;

/**
 *
 * @author milax
 */
public class Test {
                private static String slowBrokerSet = "slowBrokerSet";
        private static  String brokerSet = "brokerSet";

        public static void main( String[] args ) throws Exception
    {
                Properties env = new Properties();
        env.setProperty(ParameterQueue.USER_NAME, "guest");
        env.setProperty(ParameterQueue.USER_PASS, "guest");

        // Get host info of rabbimq (where it is)
        env.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
        env.setProperty(ParameterQueue.RABBIT_PORT, "5672");
        env.setProperty(ParameterQueue.DURABLE_QUEUE, "false");
        env.setProperty(ParameterQueue.ENABLE_COMPRESSION, "false");

        // Set info about where the message will be sent
        env.setProperty(ParameterQueue.RPC_EXCHANGE, "rpc_exchange");
        env.setProperty(ParameterQueue.RPC_ALTERNATE_EXCHANGE, "rpc_alternate_exchange");

        env.setProperty(ParameterQueue.RETRY_TIME_CONNECTION, "2000");

	Broker broker = new Broker(env);
        SlowRemoteBroker rb = broker.lookup(slowBrokerSet, SlowRemoteBroker.class);
        rb.spawnBinding("calculator", CalculatorImpl.class.getCanonicalName(), "ala",env);
    }
}
