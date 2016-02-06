package com.nuevatel.mcsim.domain;

import com.nuevatel.mc.appconn.McMessage;
import com.nuevatel.mcsim.McMsgToMetadataPredicate;

/**
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/13/15.
 */
public class MessageAction extends Action {


    public MessageAction(int seqId,
                         Type type,
                         ConcurrencyType concurrencyType,
                         McMsgToMetadataPredicate toMetadataPredicate,
                         McMessage msg) {
        super(seqId, type, concurrencyType, toMetadataPredicate, msg);
    }
}
