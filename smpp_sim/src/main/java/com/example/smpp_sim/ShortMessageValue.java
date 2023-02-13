package com.example.smpp_sim;

/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */


import org.smpp.pdu.SubmitSM;


import java.io.UnsupportedEncodingException;

/**
 * Class for storing a subset of attributes of messages to a message store.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
class ShortMessageValue {
    String systemId;
    String serviceType;
    String sourceAddr;
    String destinationAddr;
    String shortMessage;

    /**
     * Constructor for building the object from <code>SubmitSM</code>
     * PDU.
     *
     * @param systemId system id of the client
     * @param submit the PDU send from the client
     */

    ShortMessageValue(String systemId, SubmitSM submit) throws UnsupportedEncodingException {
        this.systemId = systemId;
        serviceType = submit.getServiceType();
        sourceAddr = submit.getSourceAddr().getAddress();
        destinationAddr = submit.getDestAddr().getAddress();

        String encoding = DataCodingCharsetHandler.getCharsetName(submit.getDataCoding());
        shortMessage = submit.getShortMessage(encoding);
    }
}
/*
 * $Log: not supported by cvs2svn $
 */