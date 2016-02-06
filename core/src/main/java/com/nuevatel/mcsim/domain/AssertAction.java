package com.nuevatel.mcsim.domain;

import com.nuevatel.common.appconn.Message;
import com.nuevatel.mc.appconn.McMessage;
import com.nuevatel.mcsim.AssertMsgPredicate;
import com.nuevatel.mcsim.McMsgToMetadataPredicate;

/**
 * Assert action to verify appconn message.
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/13/15.
 */
public class AssertAction extends Action {

    private AssertMsgPredicate predicate;

    public AssertAction(int seqId,
                        Action.Type type,
                        ConcurrencyType concurrencyType,
                        AssertMsgPredicate predicate,
                        McMsgToMetadataPredicate toMetadataPredicate,
                        McMessage msg) {
        super(seqId, type, concurrencyType, toMetadataPredicate, msg);
        this.predicate = predicate;
    }

    /**
     * Execute assertion predicate
     *
     * @param msg Message to check.
     * @return <code>true</code> for assert successful. <code>false</code> for the opposite case.
     */
    public boolean doAssert(Message msg) {
        return predicate.doAssert(getMsg(), msg);
    }
}
