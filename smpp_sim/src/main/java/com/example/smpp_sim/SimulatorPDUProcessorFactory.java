package com.example.smpp_sim;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//



import org.smpp.debug.FileLog;
import org.smpp.smscsim.*;
import org.smpp.smscsim.util.Table;

public class SimulatorPDUProcessorFactory implements PDUProcessorFactory {
    private PDUProcessorGroup procGroup;
    private org.smpp.smscsim.ShortMessageStore messageStore;
    private DeliveryInfoSender deliveryInfoSender;
    private Table users;
    private boolean displayInfo = false;

    public SimulatorPDUProcessorFactory(PDUProcessorGroup procGroup, ShortMessageStore messageStore, DeliveryInfoSender deliveryInfoSender, Table users) {
        this.procGroup = procGroup;
        this.messageStore = messageStore;
        this.deliveryInfoSender = deliveryInfoSender;
        this.users = users;
    }

    public PDUProcessor createPDUProcessor(SMSCSession session) {
    SimulatorPDUProcessor simPDUProcessor = new SimulatorPDUProcessor(session, (ShortMessageStore) this.messageStore, this.users);
        simPDUProcessor.setDisplayInfo(this.getDisplayInfo());
        simPDUProcessor.setGroup(this.procGroup);
        simPDUProcessor.setDeliveryInfoSender(this.deliveryInfoSender);
        this.display("new connection accepted");
        return simPDUProcessor;
    }

    public void setDisplayInfo(boolean on) {
        this.displayInfo = on;
    }

    public boolean getDisplayInfo() {
        return this.displayInfo;
    }

    private void display(String info) {
        if (this.getDisplayInfo()) {
            System.out.println(FileLog.getLineTimeStamp() + " [sys] " + info);
        }

    }
}
