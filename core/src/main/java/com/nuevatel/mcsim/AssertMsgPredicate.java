package com.nuevatel.mcsim;

import com.nuevatel.common.appconn.Message;
import com.nuevatel.mc.appconn.McMessage;

/**
 * Assertion strategy for an action.
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/16/15.
 */
public interface AssertMsgPredicate {

    boolean doAssert(McMessage expected, Message msg);
}
