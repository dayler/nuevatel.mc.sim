package com.nuevatel.mcsim.server.appconn.task;

import com.nuevatel.common.appconn.Conn;
import com.nuevatel.common.appconn.Message;
import com.nuevatel.common.appconn.Task;
import com.nuevatel.mc.appconn.McIe;
import com.nuevatel.mcsim.AllocatorService;
import com.nuevatel.mcsim.ServerProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handle <code>ForwardShortMessageMtAsyncCall</code> message.
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/15/15.
 */
public class ForwardShortMessageMtAsyncCallTask implements Task {

    private static Logger logger = LogManager.getLogger(ForwardShortMessageMtAsyncCallTask.class);

    @Override
    public Message execute(Conn conn, Message message) throws Exception {
        // messageId
        Long messageId = message.getLong(McIe.MESSAGE_ID_IE);
        // Find server processor
        ServerProcessor processor = AllocatorService.findProcessor(messageId);
        if (processor == null) {
            logger.warn("Not found processor for messageId:{}", messageId);
            return null;
        }
        // Notify to the processor
        processor.notifyResponse(message);
        return null;
    }
}
