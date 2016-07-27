package omq.supervisor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import omq.exception.RetryException;
import omq.supervisor.Supervisor;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.rabbitmq.client.Channel;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public abstract class Provisioner extends Thread {

    protected static final Logger logger = Logger.getLogger(Provisioner.class.getName());
    protected boolean killed = false;

    protected String objReference;
    protected long sleep;
    protected Supervisor supervisor;

    protected double avgServiceTime = 0, varServiceTime = 0, varInterArrivalTime = 0, reqArrivalRate = 0;

    public Provisioner(String objReference, Supervisor supervisor) throws IOException {
        this.objReference = objReference;
        this.supervisor = supervisor;
    }

    public HasObject[] getHasList() {
        try {
            return supervisor.getHasList();
        } catch (RetryException e) {
            return null;
        }
    }

    public abstract void action(double a, double b);

    public void setNumServersNeeded(int numServersNeeded) {
        supervisor.setNumServersNeeded(numServersNeeded);
    }

    public void addBindings(Object[] bindings) {
        for (int i = 0; i < bindings.length; i++) {
            try {
                System.out.println("binding reference = " + bindings[i].toString());
                supervisor.createBindings(bindings[i].toString());
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Provisioner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public List<HasObject> whoHasObject(HasObject[] hasList, boolean condition) throws RetryException {
        List<HasObject> list = new ArrayList<HasObject>();
        for (HasObject h : hasList) {
            if (h.hasObject() == condition) {
                list.add(h);
            }
        }

        return list;
    }

    protected int getStatus(String vhost, String queue) throws IOException {
        URL url = new URL("http://10.21.1.2:15672/api/queues/" + vhost + "/" + queue);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        String userpass = "guest" + ":" + "guest";
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());

        connection.setRequestProperty("Authorization", basicAuth);

        connection.connect();

        int status = connection.getResponseCode();
        //System.out.println(status);
        if (200 >= status && status < 300) {

            BufferedReader buff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String json = "", line;
            while ((line = buff.readLine()) != null) {
                json += line + "\n";
            }
            buff.close();

            JsonParser parser = new JsonParser();
            JsonObject jsonObj = parser.parse(json).getAsJsonObject();
            try {
                return jsonObj.get("message_stats").getAsJsonObject().get("deliver_get").getAsInt();
            } catch (NullPointerException e) {
                return 0;
            }
        } else {
            throw new IOException("Queue does not exist");
        }

    }
   
    
    /**
     * This method returns the queue's size.
     */
    protected int getQueueLength(String vhost, String queue) throws IOException {
        URL url = new URL("http://10.21.1.2:15672/api/queues/" + vhost + "/" + queue);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        String userpass = "guest" + ":" + "guest";
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());

        connection.setRequestProperty("Authorization", basicAuth);

        connection.connect();

        int status = connection.getResponseCode();
        //System.out.println(status);
        if (200 >= status && status < 300) {

            BufferedReader buff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String json = "", line;
            while ((line = buff.readLine()) != null) {
                json += line + "\n";
            }
            buff.close();

            JsonParser parser = new JsonParser();
            JsonObject jsonObj = parser.parse(json).getAsJsonObject();
            try {
                return jsonObj.get("messages").getAsInt();
            } catch (NullPointerException e) {
                return 0;
            }
        } else {
            throw new IOException("Queue does not exist");
        }

    }
   /**
     * This method return a list of users who are sending heavy workload, First we must add a  tracing though @ the rabbit machine.
     */
    protected Object[] getHeavyWorkloadUsers(String vhost, String queue) throws IOException {
        URL url = new URL("http://10.21.1.2:15672/api/trace-files" + "/" + queue + ".log");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        Map<String, AtomicInteger> users = new HashMap<String, AtomicInteger>();
        connection.setRequestMethod("GET");

        String userpass = "guest" + ":" + "guest";
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
        ArrayList<String> heavyWorkloadUsers = new ArrayList<String>();

        connection.setRequestProperty("Authorization", basicAuth);

        connection.connect();
        JsonParser parser = new JsonParser();
        String user = "";
        int status = connection.getResponseCode();
        //System.out.println(status);
        if (200 >= status && status < 300) {

            BufferedReader buff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = buff.readLine()) != null) {
                //System.out.println("line");
                //sum++;
                JsonObject jsonObj = parser.parse(line).getAsJsonObject();
                user = jsonObj.get("properties").getAsJsonObject().get("headers").getAsJsonObject().get("userID").getAsString();
                //System.out.println(user);
                
                if (users.containsKey(user)) {
                    users.get(user).incrementAndGet();
                } else {
                    users.put(user, new AtomicInteger(1));
                }
            }
            try {
                // clear the logs that are no longer needed located in /var/tmp/rabbitmq-tracing/
                clearLogs(queue);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Provisioner.class.getName()).log(Level.SEVERE, null, ex);
            }
            // avg = sum / users.size();
            buff.close();

            String key;
            AtomicInteger value;
            for (Map.Entry<String, AtomicInteger> entry : users.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();
                // just to test it with a static threshhold = 600 commits per 2 minutes 
                if (value.get() > 600) {
                    System.out.println(key+ "value = " + value);
                    heavyWorkloadUsers.add(key);
                } 

            }
        } else {
            System.out.println("queue = " + queue);
            throw new IOException("Queue does not exist");
        }
        return heavyWorkloadUsers.toArray();
    }
    
    public void clearLogs(String queue) throws Exception{
                                JSch jsch = new JSch();
            Session session = jsch.getSession("milax", "10.21.1.2", 22);

            session.setPassword("milax");
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            //String command="mkdir /data";
            String command = "sudo bash -c  \"> /var/tmp/rabbitmq-tracing/ISyncService.log\"";
            String sudo_pass = null;

            sudo_pass = "milax";

            com.jcraft.jsch.Channel channel = session.openChannel("exec");

            ((ChannelExec) channel).setCommand("sudo -S -p '' " + command);

            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            ((ChannelExec) channel).setErrStream(System.err);

            channel.connect();
        out.write((sudo_pass + "\n").getBytes());
        out.flush();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                System.out.print(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
            } catch (Exception ee) {
            }
            channel.disconnect();
            session.disconnect();
}
    }

    public String getObjReference() {
        return objReference;
    }

    public void setObjReference(String objReference) {
        this.objReference = objReference;
    }

    public long getSleep() {
        return sleep;
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

}
