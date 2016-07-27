/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package omq.supervisor.broker;

import java.util.Date;
import java.util.Properties;
import java.util.Set;
import omq.exception.RetryException;
import omq.server.RemoteObject;
import omq.supervisor.util.HasObject;
import org.apache.log4j.Logger;

/**
 *
 * @author ala
 * a slow remote broker enables to spawns bindings between the
 * the main exchange and the slow queue
 */
/*TODO: add remove binding method */
public class SlowRemoteBrokerImpl extends RemoteObject implements SlowRemoteBroker {

    private static final Logger logger = Logger.getLogger(RemoteBrokerImpl.class.getName());

    @Override
    public void spawnBinding(String reference, String className, String userID, Properties env) throws Exception {
        System.out.println(new Date() + " . Broker " + this.getUID() + " will spawn " + reference+ " with user ID "+userID);
        try {
            RemoteObject remote = (RemoteObject) Class.forName(className).newInstance();
            //System.out.println("userID = " + userID);
            getBroker().bind(reference, remote, userID, true, env);
        } catch (Exception e) {
            logger.error("Could not spawn object " + reference, e);
            // Throw the exception to the supervisor
            throw e;
        }

    }

    @Override
    public void spawnBinding(String reference, String className, String userID) throws Exception {
                System.out.println("Broker " + this.getUID() + " will spawn " + reference+ " with user ID "+userID);
        try {
            RemoteObject remote = (RemoteObject) Class.forName(className).newInstance();
            //System.out.println("userID = " + userID);
            getBroker().bind(reference, remote, userID, true);
        } catch (Exception e) {
            logger.error("Could not spawn object " + reference, e);
            // Throw the exception to the supervisor
            throw e;
        }
    }
    

//    @Override
//    public boolean hasSlowObject(String reference, String userID) throws RetryException {
//            if (getBroker().getRemoteObjs().containsKey(reference))
//        return getBroker().getSlowQueues().contains(userID);
//            else return false;
//    }
//
//    @Override
//    public HasObject hasSlowObjectInfo(String reference) throws RetryException {
//        
//    }

}
