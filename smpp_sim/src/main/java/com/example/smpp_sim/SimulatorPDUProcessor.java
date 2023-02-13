package com.example.smpp_sim;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//



import java.io.IOException;
import org.smpp.SmppObject;
import org.smpp.debug.Debug;
import org.smpp.debug.Event;
import org.smpp.debug.FileLog;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.CancelSM;
import org.smpp.pdu.DataSMResp;
import org.smpp.pdu.DeliverSMResp;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.QuerySM;
import org.smpp.pdu.QuerySMResp;
import org.smpp.pdu.ReplaceSM;
import org.smpp.pdu.Request;
import org.smpp.pdu.Response;
import org.smpp.pdu.SubmitMultiSMResp;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import org.smpp.pdu.WrongLengthOfStringException;
import org.smpp.smscsim.DeliveryInfoSender;
import org.smpp.smscsim.PDUProcessor;
import org.smpp.smscsim.SMSCSession;
import org.smpp.smscsim.util.Record;
import org.smpp.smscsim.util.Table;

public class SimulatorPDUProcessor extends PDUProcessor {
    private SMSCSession session = null;
    private org.smpp.smscsim.ShortMessageStore messageStore = null;
    private DeliveryInfoSender deliveryInfoSender = null;
    private Table users = null;
    private boolean bound = false;
    private String systemId = null;
    private boolean displayInfo = false;
    private static int intMessageId = 2000;
    private static final String SYSTEM_ID = "Smsc Simulator";
    private static final String SYSTEM_ID_ATTR = "name";
    private static final String PASSWORD_ATTR = "password";
    private Debug debug = SmppObject.getDebug();
    private Event event = SmppObject.getEvent();

    public SimulatorPDUProcessor(SMSCSession session, ShortMessageStore messageStore, Table users) {
        this.session = session;
        this.messageStore = messageStore;
        this.users = users;
    }

    public void stop() {
    }

    public void clientRequest(Request request) {
        this.debug.write("SimulatorPDUProcessor.clientRequest() " + request.debugString());
        int commandId = request.getCommandId();

        try {
            this.display("client request pdu: " + request.debugString());
            Response response;
            if (!this.bound) {
                if (commandId != 2 && commandId != 1 && commandId != 9) {
                    if (request.canResponse()) {
                        response = request.getResponse();
                        response.setCommandStatus(4);
                        this.serverResponse(response);
                    }

                    this.session.stop();
                } else {
                    int commandStatus = this.checkIdentity((BindRequest)request);
                    if (commandStatus == 0) {
                        BindResponse bindResponse = (BindResponse)request.getResponse();
                        bindResponse.setSystemId("Smsc Simulator");
                        this.serverResponse(bindResponse);
                        this.bound = true;
                    } else {
                        response = request.getResponse();
                        response.setCommandStatus(commandStatus);
                        this.serverResponse(response);
                        this.session.stop();
                    }
                }
            } else if (request.canResponse()) {
                response = request.getResponse();
                switch (commandId) {
                    case 3:
                        QuerySM queryRequest = (QuerySM)request;
                        QuerySMResp queryResponse = (QuerySMResp)response;
                        this.display("querying message in message store");
                        queryResponse.setMessageId(queryRequest.getMessageId());
                        break;
                    case 4:
                        SubmitSMResp submitResponse = (SubmitSMResp)response;
                        submitResponse.setMessageId(this.assignMessageId());
                        this.display("putting message into message store");
                        this.messageStore.submit((SubmitSM)request, submitResponse.getMessageId(), this.systemId);
                        byte registeredDelivery = (byte)(((SubmitSM)request).getRegisteredDelivery() & 3);
                        if (registeredDelivery == 1) {
                            this.deliveryInfoSender.submit(this, (SubmitSM)request, submitResponse.getMessageId());
                        }
                        break;
                    case 5:
                        DeliverSMResp deliverResponse = (DeliverSMResp)response;
                        deliverResponse.setMessageId(this.assignMessageId());
                    case 6:
                    default:
                        break;
                    case 7:
                        ReplaceSM replaceRequest = (ReplaceSM)request;
                        this.display("replacing message in message store");
                        this.messageStore.replace(replaceRequest.getMessageId(), replaceRequest.getShortMessage());
                        break;
                    case 8:
                        CancelSM cancelRequest = (CancelSM)request;
                        this.display("cancelling message in message store");
                        this.messageStore.cancel(cancelRequest.getMessageId());
                        break;
                    case 33:
                        SubmitMultiSMResp submitMultiResponse = (SubmitMultiSMResp)response;
                        submitMultiResponse.setMessageId(this.assignMessageId());
                        break;
                    case 259:
                        DataSMResp dataResponse = (DataSMResp)response;
                        dataResponse.setMessageId(this.assignMessageId());
                }

                this.serverResponse(response);
                if (commandId == 6) {
                    this.session.stop();
                }
            }
        } catch (WrongLengthOfStringException var14) {
            this.event.write(var14, "");
        } catch (Exception var15) {
            this.event.write(var15, "");
        }

    }

    public void clientResponse(Response response) {
        this.debug.write("SimulatorPDUProcessor.clientResponse() " + response.debugString());
        this.display("client response: " + response.debugString());
    }

    public void serverRequest(Request request) throws IOException, PDUException {
        this.debug.write("SimulatorPDUProcessor.serverRequest() " + request.debugString());
        this.display("server request: " + request.debugString());
        this.session.send(request);
    }

    public void serverResponse(Response response) throws IOException, PDUException {
        synchronized(this) {
            try {
                this.wait(300L);
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

        this.debug.write("SimulatorPDUProcessor.serverResponse() " + response.debugString());
        this.display("server response: " + response.debugString());
        this.session.send(response);
    }

    private int checkIdentity(BindRequest request) {
        int commandStatus = 0;
        Record user = this.users.find("name", request.getSystemId());
        if (user != null) {
            String password = user.getValue("password");
            if (password != null) {
                if (!request.getPassword().equals(password)) {
                    commandStatus = 14;
                    this.debug.write("system id " + request.getSystemId() + " not authenticated. Invalid password.");
                    this.display("not authenticated " + request.getSystemId() + " -- invalid password");
                } else {
                    this.systemId = request.getSystemId();
                    this.debug.write("system id " + this.systemId + " authenticated");
                    this.display("authenticated " + this.systemId);
                }
            } else {
                commandStatus = 14;
                this.debug.write("system id " + this.systemId + " not authenticated. " + "Password attribute not found in users file");
                this.display("not authenticated " + this.systemId + " -- no password for user.");
            }
        } else {
            commandStatus = 15;
            this.debug.write("system id " + request.getSystemId() + " not authenticated -- not found");
            this.display("not authenticated " + request.getSystemId() + " -- user not found");
        }

        return commandStatus;
    }

    private String assignMessageId() {
        String messageId = "Smsc";
        ++intMessageId;
        messageId = messageId + intMessageId;
        return messageId;
    }

    public SMSCSession getSession() {
        return this.session;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public void setDisplayInfo(boolean on) {
        this.displayInfo = on;
    }

    public boolean getDisplayInfo() {
        return this.displayInfo;
    }

    public void setDeliveryInfoSender(DeliveryInfoSender deliveryInfoSender) {
        this.deliveryInfoSender = deliveryInfoSender;
    }

    private void display(String info) {
        if (this.getDisplayInfo()) {
            String sysId = this.getSystemId();
            if (sysId == null) {
                sysId = "";
            }

            System.out.println(FileLog.getLineTimeStamp() + " [" + sysId + "] " + info);
        }

    }
}

