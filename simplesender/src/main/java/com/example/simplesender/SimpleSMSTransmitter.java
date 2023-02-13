package com.example.simplesender;
import org.smpp.Connection;
import org.smpp.Data;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.BindTransmitterResp;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import java.util.*;
public class SimpleSMSTransmitter {

    /**
     * @param args
     */
    private Session session = null;
    private String ipAddress = "172.16.144.189";
    private String systemId = "jorge";
    private String password = "prtgljrg";
    private int port = 2775;
    private String shortMessage = "abcdefg";
    private String sourceAddress = "jorge";
    private String destinationAddress = "pavel";


    public static void main(String[] args) {

        SimpleSMSTransmitter objSimpleSMSTransmitter = new SimpleSMSTransmitter();

        Scanner sc= new Scanner(System.in);
        System.out.println("Enter the short message to be sent ");
        objSimpleSMSTransmitter.shortMessage= sc.nextLine();
        System.out.println(objSimpleSMSTransmitter.shortMessage);
        objSimpleSMSTransmitter.bindToSMSC();
        objSimpleSMSTransmitter.sendSingleSMS();
        System.out.println("Program terminated");
        System.exit(0);
    }

    public void bindToSMSC() {
        try {
            Connection conn = new TCPIPConnection(ipAddress, port);
            session = new Session(conn);

            BindRequest breq = new BindTransmitter();
            breq.setSystemId(systemId);
            breq.setPassword(password);
            BindTransmitterResp bresp = (BindTransmitterResp) session.bind(breq);

            if(bresp.getCommandStatus() == Data.ESME_ROK) {
                System.out.println("Connected to SMSC");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSingleSMS() {
        try {
            SubmitSM request = new SubmitSM();

            // set values
            request.setSourceAddr(sourceAddress);
            request.setDestAddr(destinationAddress);


            request.setShortMessage(shortMessage);

            // send the request
            SubmitSMResp resp = session.submit(request);

            if (resp.getCommandStatus() == Data.ESME_ROK) {
                System.out.println("Message submitted....");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to submit message....");
        }
    }
}