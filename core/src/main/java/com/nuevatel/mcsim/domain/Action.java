package com.nuevatel.mcsim.domain;

import com.nuevatel.common.appconn.Message;
import com.nuevatel.mc.appconn.McMessage;
import com.nuevatel.mcsim.McMsgToMetadataPredicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/13/15.
 */
public abstract class Action {

    private static Logger logger = LogManager.getLogger(Action.class);

    private int seqId;

    private Type type;

    private ConcurrencyType concurrencyType;

    private McMessage msg;

    private McMsgToMetadataPredicate toMetadataPredicate;

    public Action(int seqId,
                  Type type,
                  ConcurrencyType concurrencyType,
                  McMsgToMetadataPredicate toMetadataPredicate,
                  McMessage msg) {
        this.seqId = seqId;
        this.type = type;
        this.concurrencyType = concurrencyType;
        this.toMetadataPredicate = toMetadataPredicate;
        this.msg = msg;
    }

    public String getMcMsgMetadata(Message mcMsg) {
        try {
            return toMetadataPredicate.toMetadata(mcMsg);
        } catch (Throwable ex) {
            logger.warn("Failed to get metadata from Message.", ex);
            return null;
        }
    }

    public int getSeqId() {
        return seqId;
    }

    public Type getType() {
        return type;
    }

    public McMessage getMsg() {
        return msg;
    }

    public ConcurrencyType getConcurrencyType() {
        return concurrencyType;
    }

    /**
     * Types of commands.
     * <br/>
     * <li><code>req</code> indicates that the message is send</li>
     * <li><code>resp</code> the message is awaiting, and an assertion is executing</li>
     */
    public enum Type  {
        send, // message to dispatch
        receive, // assertion
        ;
    }

    public enum ConcurrencyType {
        async,
        sync,
        ;
    }
}
