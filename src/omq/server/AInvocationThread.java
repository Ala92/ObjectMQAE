package omq.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import omq.common.broker.Broker;
import omq.common.broker.StatisticList;
import omq.common.message.Request;
import omq.common.message.Response;
import omq.common.util.ParameterQueue;
import omq.common.util.Serializer;
import omq.exception.OmqException;
import omq.exception.SerializerException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

/**
 *
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 *
 */
public abstract class AInvocationThread extends Thread {

    private static final Logger logger = Logger.getLogger(AInvocationThread.class.getName());

    // RemoteObject
    protected RemoteObject obj;
    protected String reference;
    protected String UID;
    protected String userID;
    protected Properties env;

    // Broker
    protected Broker broker;
    protected Serializer serializer;

    // Consumer
    protected Channel channel;
    protected QueueingConsumer consumer;
    protected boolean killed = false;

    // Statistics
    private StatisticList statsLists;
    private boolean slow;

    public AInvocationThread(RemoteObject obj, String userID, boolean slow) throws Exception {
        this.obj = obj;
        this.UID = obj.getUID();
        this.reference = obj.getRef();
        this.env = obj.getEnv();
        this.broker = obj.getBroker();
        this.serializer = broker.getSerializer();
        this.statsLists = new StatisticList();
        this.userID = userID;
        this.slow = slow;
    }

    @Override
    public void run() {
        while (!killed) {
            try {
                // Get the delivery
                Delivery delivery = consumer.nextDelivery();

                long arrival = System.currentTimeMillis();

                executeTask(delivery);

                long end = System.currentTimeMillis();

                if (broker.getStatisticsMap() != null && broker.getStatisticsThread(reference) != null) {
                    statsLists.setInfo(arrival, end - arrival);
                }

            } catch (InterruptedException i) {
                logger.error(i);
            } catch (ShutdownSignalException e) {
                logger.error(e);
                try {
                    if (channel.isOpen()) {
                        channel.close();
                    }
                    if (isSlow()) {
                        startSlowQueues();
                    } else {
                        startQueues();
                    }
                } catch (Exception e1) {
                    try {
                        long milis = Long.parseLong(env.getProperty(ParameterQueue.RETRY_TIME_CONNECTION, "2000"));
                        Thread.sleep(milis);
                    } catch (InterruptedException e2) {
                        logger.error(e2);
                    }
                    logger.error(e1);
                }
            } catch (ConsumerCancelledException e) {
                logger.error(e);
            } catch (SerializerException e) {
                logger.error(e);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
            }

        }
        logger.info("ObjectMQ ('" + obj.getRef() + "') InvocationThread " + Thread.currentThread().getId() + " is killed");
    }

    @Override
    public synchronized void start() {
        try {
            if (userID.equals("broker")){
                startBrokerQueues();
            }
            else {
            if (slow == true) {
                startSlowQueues();
            } else {
                startQueues();
            }
                    }
            super.start();
        } catch (Exception e) {
            logger.error("Cannot start a remoteObject", e);
        }

    }

    /**
     * This method starts the queues using the information got in the
     * environment.
     *
     * @throws Exception
     */
    protected abstract void startQueues() throws Exception;

    protected abstract void startSlowQueues() throws Exception;

    protected abstract void startBrokerQueues() throws Exception;
    
    public abstract String getType(); 

    protected void executeTask(Delivery delivery) throws Exception {
        String serializerType = delivery.getProperties().getType();

        // Deserialize the request
        Request request = serializer.deserializeRequest(serializerType, delivery.getBody(), obj);
        String methodName = request.getMethod();
        String requestID = request.getId();

        logger.debug("Object: " + obj.getRef() + ", method: " + methodName + " corrID: " + requestID + ", serializerType: " + serializerType);

        // Invoke the method
        Object result = null;
        OmqException error = null;
        try {
            result = obj.invokeMethod(request.getMethod(), request.getParams());
        } catch (InvocationTargetException e) {
            Throwable throwable = e.getTargetException();
            logger.error("Object: " + obj.getRef() + " at method: " + methodName + ", corrID" + requestID, throwable);
            error = new OmqException(throwable.getClass().getCanonicalName(), throwable.getMessage());
        } catch (NoSuchMethodException e) {
            logger.error("Object: " + obj.getRef() + " cannot find method: " + methodName);
            error = new OmqException(e.getClass().getCanonicalName(), e.getMessage());
        }

        // Reply if it's necessary
        if (!request.isAsync()) {
            Response resp = new Response(request.getId(), obj.getRef(), result, error);

            BasicProperties props = delivery.getProperties();

            BasicProperties replyProps = new BasicProperties.Builder().appId(obj.getRef()).correlationId(props.getCorrelationId()).type(getType()).build();

            byte[] bytesResponse = serializer.serialize(serializerType, resp);
            channel.basicPublish("", props.getReplyTo(), replyProps, bytesResponse);
            logger.debug("Publish sync response -> Object: " + obj.getRef() + ", method: " + methodName + " corrID: " + requestID + " replyTo: "
                    + props.getReplyTo());
        }

        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }

    public void kill() throws IOException {
        logger.info("Killing objectmq: " + reference + " thread id");
        killed = true;
        interrupt();
        channel.close();
    }

    public RemoteObject getObj() {
        return obj;
    }

    public void setObj(RemoteObject obj) {
        this.obj = obj;
    }

    public StatisticList getAndRemoveStatsLists() {
        StatisticList aux = statsLists;
        statsLists = new StatisticList();
        return aux;
    }

    public boolean isSlow() {
        return slow;
    }

    public void setSlow(Boolean slow) {
        this.slow = slow;
    }

}