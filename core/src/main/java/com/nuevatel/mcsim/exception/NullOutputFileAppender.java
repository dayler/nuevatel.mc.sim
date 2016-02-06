package com.nuevatel.mcsim.exception;

import com.nuevatel.common.exception.OperationRuntimeException;

/**
 * OutputFileAppender was not initialized
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/18/15.
 */
public class NullOutputFileAppender extends OperationRuntimeException {
    
    private static final long serialVersionUID = 20160129L;

    /**
     * {@inheritDoc}
     */
    public NullOutputFileAppender() {
        super("OutputFileAppender is not initialized.");
    }
}
