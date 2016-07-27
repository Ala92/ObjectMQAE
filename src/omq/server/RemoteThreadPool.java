package omq.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */

public class RemoteThreadPool {
	private static final Logger logger = Logger.getLogger(RemoteThreadPool.class.getName());
	private List<InvocationThread> workers;
	private MultiInvocationThread multiWorker;
        private String userID;
	private int numThreads;
	private RemoteObject obj;
        private boolean slow; 
        
	public RemoteThreadPool(int numThreads, RemoteObject obj, String userID, boolean slow) {
		this.obj = obj;
		this.numThreads = numThreads;
                this.slow = slow;
		workers = new ArrayList<InvocationThread>(numThreads);
                this.userID = userID;
	}

	public void startPool() {

		logger.info("ObjectMQ reference: " + obj.getRef() + ", creating: " + numThreads);

		try {
			multiWorker = new MultiInvocationThread(obj, userID, slow);
			multiWorker.start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		for (int i = 0; i < numThreads; i++) {
			try {
				InvocationThread iThread = new InvocationThread(obj, userID, slow);
				workers.add(iThread);
				iThread.start();
			} catch (Exception e) {
				logger.error("Error while creating pool threads", e);
				e.printStackTrace();
			}
		}
                
                
	}

	public void kill() throws IOException {
		multiWorker.kill();
		for (InvocationThread iThread : workers) {
			iThread.kill();
		}
	}

	public List<InvocationThread> getWorkers() {
		return workers;
	}

	public void setWorkers(List<InvocationThread> workers) {
		this.workers = workers;
	}

}