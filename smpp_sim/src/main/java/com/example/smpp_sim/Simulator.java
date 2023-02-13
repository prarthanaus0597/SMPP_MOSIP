package com.example.smpp_sim;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

import org.smpp.SmppObject;
import org.smpp.debug.*;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.WrongLengthOfStringException;

import org.smpp.smscsim.util.Table;
import org.smpp.smscsim.SMSCListener;
import org.smpp.smscsim.PDUProcessorGroup;
import org.smpp.smscsim.DeliveryInfoSender;
import org.smpp.smscsim.SMSCListenerImpl;
import org.smpp.smscsim.SMSCSession;


public class Simulator {


    /**
     * Name of file with user (client) authentication information.
     */
    static String usersFileName = System.getProperty("usersFileName", "etc/users.txt");

    /**
     * Directory for creating of debug and event files.
     */
    static final String dbgDir = "./";

    /**
     * The debug object.
     */
    static Debug debug = new FileDebug(dbgDir, "sim.dbg");

    /**
     * The event object.
     */
    static Event event = new FileEvent(dbgDir, "sim.evt");

    public static final int DSIM = 16;
    public static final int DSIMD = 17;
    public static final int DSIMD2 = 18;

    static BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

    boolean keepRunning = true;

    private SMSCListener smscListener = null;
    private SimulatorPDUProcessorFactory factory = null;
    private PDUProcessorGroup processors = null;
    private ShortMessageStore messageStore = null;
    private DeliveryInfoSender deliveryInfoSender = null;
    private Table users = null;
    private boolean displayInfo = true;


    private Simulator() {
    }


    public static void main(String args[]) throws IOException {
        SmppObject.setDebug(debug);
        SmppObject.setEvent(event);
        debug.activate();
        event.activate();
        debug.deactivate(SmppObject.DRXTXD2);
        debug.deactivate(SmppObject.DPDUD);
        debug.deactivate(SmppObject.DCOMD);
        debug.deactivate(DSIMD2);
        Simulator s = new Simulator();
        s.start();
        int v=0;
        Scanner sc=new Scanner(System.in);
        while (s.keepRunning) {
            v = sc.nextInt();
            if (v == 1) {
//            s.listClients();
                s.messageList();
            }
        }

    }


    protected void start() throws IOException {
        if (smscListener == null) {
//            System.out.print("Enter port number> ");
            int port =2775;// Integer.parseInt(keyboard.readLine());
            System.out.print("Starting listener... ");
            smscListener = new SMSCListenerImpl(port, true);
            processors = new PDUProcessorGroup();
            messageStore = new ShortMessageStore();
            deliveryInfoSender = new DeliveryInfoSender();
            deliveryInfoSender.start();
            users = new Table(usersFileName);
            factory = new SimulatorPDUProcessorFactory(processors, messageStore, deliveryInfoSender, users);
            factory.setDisplayInfo(displayInfo);//displays information of connection
            smscListener.setPDUProcessorFactory(factory);
            smscListener.start();
            System.out.println("started.");


        } else {
            System.out.println("Listener is already running.");
        }

    }

    protected void messageList() {
        if (smscListener != null) {

            Hashtable<String, ShortMessageValue> messages=messageStore.getMessage();
            if (messages.size() != 0) {
                ShortMessageValue sMV;
                Enumeration<String> keys = messages.keys();
                Object key;
                System.out.println("------------------------------------------------------------------------");
                System.out.println("| Msg Id   |Sender     |ServT|Source address |Dest address   |Message   ");
                System.out.println("------------------------------------------------------------------------");
                while (keys.hasMoreElements()) {
                    key = keys.nextElement();
                    sMV = (ShortMessageValue) messages.get(key);
                    printMessage(key, sMV);
                }
            } else {
                System.out.println("There is no message in the message store.");
            }



        } else {
            System.out.println("You must start listener first.");
        }
    }
    private void printMessage(Object key, ShortMessageValue sMV) {
        String messageId, systemId, serviceType, sourceAddr, destAddr, shortMessage;
        try {
            messageId = key.toString();
            systemId = pad(sMV.systemId, 11);
            if (sMV.serviceType.equals("")) {
                serviceType = "null";
            } else {
                serviceType = sMV.serviceType;
            }
            serviceType = pad(serviceType, 5);
            sourceAddr = pad(sMV.sourceAddr, 15);
            destAddr = pad(sMV.destinationAddr, 15);
            shortMessage = sMV.shortMessage;
            System.out.println(
                    "- "
                            + messageId
                            + " |"
                            + systemId
                            + "|"
                            + serviceType
                            + "|"
                            + sourceAddr
                            + "|"
                            + destAddr
                            + "|"
                            + shortMessage);
            sendMessage(sMV.sourceAddr, sMV.destinationAddr, shortMessage);
        }
        catch(Exception e){
System.out.println(e);
        }
    }
    private String pad(String data, int length) {
        if (data == null) {
            data = "";
        }

        String result;
        if (data.length() > length) {
            result = data.substring(1, length + 1);
        } else {
            int padCount = length - data.length();
            result = data;

            for(int i = 1; i <= padCount; ++i) {
                result = result + " ";
            }
        }

        return result;
    }


    protected void listClients() {
        if (smscListener != null) {
            synchronized (processors) {
                int procCount = processors.count();
                if (procCount > 0) {
                    SimulatorPDUProcessor proc;
                    for (int i = 0; i < procCount; i++) {
                        proc = (SimulatorPDUProcessor) processors.get(i);
                        System.out.print(proc.getSystemId());
                        if (!proc.isActive()) {
                            System.out.println(" (inactive)");
                        } else {
                            System.out.println();
                        }
                    }
                } else {
                    System.out.println("No client connected.");
                }
            }
        } else {
            System.out.println("You must start listener first.");
        }
    }


    protected void sendMessage(String sourceAddr,String destAddr, String shortMessage) throws IOException {
        if (smscListener != null) {
            int procCount = processors.count();
            if (procCount > 0) {
                String client;
                SimulatorPDUProcessor proc;


                client=destAddr;
                for (int i = 0; i < procCount; i++) {
                    proc = (SimulatorPDUProcessor) processors.get(i);
//                    System.out.println(proc.getSystemId()+" "+client);
                    if (proc.getSystemId().equals(client)) {
                        if (proc.isActive()) {
//                            System.out.print("Type the message> ");
//                            String message = keyboard.readLine();
                            String message= shortMessage;
                            DeliverSM request = new DeliverSM();
                            try {
                                request.setSourceAddr(sourceAddr);
                                request.setDestAddr(destAddr);
                                request.setShortMessage(message);
                                proc.serverRequest(request);
                                System.out.println("Message sent.");
                            } catch (WrongLengthOfStringException e) {
                                System.out.println("Message sending failed");
                                event.write(e, "");
                            } catch (IOException ioe) {
                            } catch (PDUException pe) {
                            }
                        } else {
                            System.out.println("This session is inactive.");
                        }
                    }
                    else{
//                        System.out.println("not this");
                    }
                }
            } else {
                System.out.println("No client connected.");
            }
        } else {
            System.out.println("You must start listener first.");
        }
    }
}