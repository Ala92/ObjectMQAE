package omq.supervisor.broker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import omq.common.broker.Measurement;
import omq.exception.RetryException;
import omq.server.RemoteObject;
import omq.supervisor.util.HasObject;

import org.apache.log4j.Logger;

/**
 *
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 *
 */
public class RemoteBrokerImpl extends RemoteObject implements RemoteBroker {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(RemoteBrokerImpl.class.getName());

    @Override
    public Set<String> getRemoteObjects() {
        return getBroker().getRemoteObjs().keySet();
    }


    @Override
    public void spawnObject(String reference, String className, Properties env) throws Exception {
        logger.info("Broker " + this.getUID() + "will spawn " + reference);
        try {
            RemoteObject remote = (RemoteObject) Class.forName(className).newInstance();
            getBroker().bind(reference, remote, "no_id", false, env);
        } catch (Exception e) {
            logger.error("Could not spawn object " + reference, e);
            // Throw the exception to the supervisor
            throw e;
        }
    }

    @Override
    public void spawnObject(String reference, String className) throws Exception {
        logger.info("Broker " + this.getUID() + "will spawn " + reference);
        try {
                        BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("bindings.txt", true));
            } catch (IOException ex) {
            }
            writer.write(new Date() + ". reference= " + reference + " - TotalTime: " + "\n");
            writer.flush();
            RemoteObject remote = (RemoteObject) Class.forName(className).newInstance();
            getBroker().bind(reference, remote, "no_id", false);
        } catch (Exception e) {
            logger.error("Could not spawn object " + reference, e);
            // Throw the exception to the supervisor
            throw e;
        }
    }


    @Override
    public void deleteObject(String reference) throws Exception {
        logger.info("Broker " + this.getUID() + "will delete " + reference);
        try {
            getBroker().unbind(reference);
        } catch (Exception e) {
            logger.error("Could not delete object " + reference, e);
            // Throw the exception to the supervisor
            throw e;
        }
    }

    @Override
    public boolean hasObject(String reference) throws RetryException {
        return getBroker().getRemoteObjs().containsKey(reference);
    }

    @Override
    public HasObject hasObjectInfo(String reference) throws RetryException {
        Measurement m = null;
        if (getBroker().getRemoteObjs().containsKey(reference)) {
            // Now with measurements!!!
            if (getBroker().getStatisticsThread(reference) != null) {
                m = getBroker().getStatisticsThread(reference).getMeasurement();
            }
            return new HasObject(this.getUID(), reference, true, m);
        }
        return new HasObject(this.getUID(), reference, false, m);
    }


}
