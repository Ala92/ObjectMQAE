/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package omq.supervisor.broker;

import java.util.Properties;
import java.util.Set;
import omq.Remote;
import omq.client.annotation.MultiMethod;
import omq.client.annotation.SyncMethod;
import omq.exception.RetryException;
import omq.server.RemoteObject;
import omq.supervisor.util.HasObject;

/**
 *
 * @author ala
 */
public interface SlowRemoteBroker extends Remote {

    public void spawnBinding(String reference, String className, String userID, Properties env) throws Exception;

    public void spawnBinding(String reference, String className, String userID) throws Exception;

//    public boolean hasSlowObject(String reference) throws RetryException;
//
//    public HasObject hasSlowObjectInfo(String reference) throws RetryException;
}
