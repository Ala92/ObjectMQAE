package omq.supervisor.util;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import omq.supervisor.Supervisor;

public class QueueProvisioner extends Provisioner {

    public QueueProvisioner(String objReference, Supervisor supervisor) throws IOException {
        super(objReference, supervisor);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run() {
        while (!killed) {
            try {
                Object [] users = getHeavyWorkloadUsers("%2f", objReference);
                super.addBindings(users);
                for (int i = 0; i < users.length; i++) {
                FileWriterProvisioner.write(new Date(), users[i].toString(), avgServiceTime, avgServiceTime, avgServiceTime, avgServiceTime);
                }
                //double length = getQueueLength("%2f", objReference);
                Thread.sleep(sleep);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void action(double length, double b) {
        // TODO Auto-generated method stub
        try {
//            HasObject[] hasList = getHasList();
//            List<HasObject> serversWithObject = whoHasObject(hasList, true);
//            // Ask how many servers are
//            int numServersNow = serversWithObject.size();
//            int numServersNeeded = 0;
//            if (length >= 500 && numServersNow < 5) {
//                numServersNeeded = numServersNow + 1;
//                super.setNumServersNeeded(numServersNeeded);
//                FileWriterProvisioner.write(new Date(), "QueueProvisioner", length,0 , 0, 0,
//                        0, 0, numServersNeeded, numServersNow);
//
//            } else if (length <= 50 && numServersNow > 1) {
//                numServersNeeded = numServersNow - 1;
//                super.setNumServersNeeded(numServersNeeded);
//                FileWriterProvisioner.write(new Date(), "QueueProvisioner", 0, length, 0, 0,
//                        0, 0, numServersNeeded, numServersNow);
//            }

        } catch (Exception e) {
            logger.error("Object: " + objReference, e);
            e.printStackTrace();
        }
    }

}
