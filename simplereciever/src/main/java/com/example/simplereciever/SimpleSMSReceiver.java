package com.example.simplereciever;
import org.smpp.Data;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.BindReceiver;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.PDU;

public class SimpleSMSReceiver {

    /**
     * Parameters used for connecting to SMSC (or  SMPPSim)
     */
    private Session session = null;
    private String ipAddress = "172.16.144.189";
    private String systemId = "pavel";
    private String password = "dfsew";
    private int port = 2775;


    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Sms receiver starts");

        SimpleSMSReceiver objSimpleSMSReceiver = new SimpleSMSReceiver();
        objSimpleSMSReceiver.bindToSmsc();

        while(true) {

            objSimpleSMSReceiver.receiveSms();
//            System.out.println("Sms receiving");
        }
    }

    private void bindToSmsc() {
        try {
            // setup connection
            TCPIPConnection connection = new TCPIPConnection(ipAddress, port);
            connection.setReceiveTimeout(20 * 1000);
            session = new Session(connection);

            // set request parameters
            BindRequest request = new BindReceiver();
            request.setSystemId(systemId);
            request.setPassword(password);

            // send request to bind
            BindResponse response = session.bind(request);
            if (response.getCommandStatus() == Data.ESME_ROK) {
                System.out.println("Sms receiver is connected to SMPPSim.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveSms() {
        try {

            PDU pdu = session.receive(1500);

            if (pdu != null) {
                DeliverSM sms = (DeliverSM) pdu;

                System.out.println("From: " + sms.getSourceAddr());
                System.out.println("To: " + sms.getDestAddr());
                if ((int)sms.getDataCoding() == 0 ) {
                    //message content is English
                    System.out.println("***** New Message Received *****");
                    System.out.println("From: " + sms.getSourceAddr().getAddress());
                    System.out.println("To: " + sms.getDestAddr().getAddress());
                    System.out.println("Content: " + sms.getShortMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}