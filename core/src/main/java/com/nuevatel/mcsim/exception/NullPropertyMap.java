package com.nuevatel.mcsim.exception;

import com.nuevatel.common.exception.OperationRuntimeException;

/**
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/23/15.
 */
public class NullPropertyMap extends OperationRuntimeException {
    
    private static final long serialVersionUID = 20160129L;
    
    /**
     * {@inheritDoc}
     */
    public NullPropertyMap() {
        super("Null Property Map...");
    }
}
