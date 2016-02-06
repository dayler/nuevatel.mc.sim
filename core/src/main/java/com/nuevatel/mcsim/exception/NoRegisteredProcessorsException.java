package com.nuevatel.mcsim.exception;

import com.nuevatel.common.exception.OperationRuntimeException;

/**
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/17/15.
 */
public class NoRegisteredProcessorsException extends OperationRuntimeException {
    
    private static final long serialVersionUID = 20160129L;
    
    public NoRegisteredProcessorsException() {
        super("No Registered server processors.");
    }
}
