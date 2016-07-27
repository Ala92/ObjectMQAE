/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package omq.supervisor;

import java.util.Properties;
import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.MultiMethod;
import omq.client.annotation.SyncMethod;
import omq.exception.RetryException;
import omq.supervisor.util.HasObject;

/**
 *
 * @author ala
 */
public interface SlowRemoteBroker extends Remote {

    @AsyncMethod
    public void spawnBinding(String reference, String className, String userID, Properties env) throws Exception;


    @AsyncMethod
    public void spawnBinding(String reference, String className, String userID) throws Exception;

//    @MultiMethod
//    @SyncMethod(retry = 1, timeout = 1000)
//    public boolean[] hasSlowObject(String reference) throws RetryException;
//
//    @MultiMethod
//    @SyncMethod(retry = 1, timeout = 1000)
//    public HasObject[] hasSlowObjectInfo(String reference) throws RetryException;
}
