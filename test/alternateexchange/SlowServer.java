/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alternateexchange;

import test.CalculatorImpl;
import java.util.Properties;
import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;

/**
 *
 * @author milax
 */
public class SlowServer {
        public static void main(String args[]) throws Exception {
        String slowBrokerSet = "slowBrokerSet";
        String brokerSet = "brokerSet";
        
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

        CalculatorImpl calc = new CalculatorImpl();

        Broker broker = new Broker(env);
        broker.allowRemoteBindingAllocation(slowBrokerSet, "slowbroker");
        
    }

}
