package com.nuevatel.mcsim;

import com.nuevatel.common.appconn.Message;

/**
 * Transform McMessage on its metadata representation
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/18/15.
 */
public interface McMsgToMetadataPredicate {

    String toMetadata(Message mcMsg);

}
