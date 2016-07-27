package test;

import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.MultiMethod;
import omq.client.annotation.RemoteInterface;
import omq.client.annotation.SyncMethod;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */

@RemoteInterface
public interface Calculator extends Remote {
        
        @AsyncMethod
	public void add(int x, int y);

	@AsyncMethod
	public void mult(int x, int y);

	@SyncMethod(timeout = 1500)
	public int divideByZero();
        
        @AsyncMethod
        public void fact(int x);
                
}
