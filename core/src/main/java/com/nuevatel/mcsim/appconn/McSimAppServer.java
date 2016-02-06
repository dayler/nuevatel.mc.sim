package com.nuevatel.mcsim.appconn;

import com.nuevatel.common.appconn.AppServer;
import com.nuevatel.common.appconn.Conn;
import com.nuevatel.common.appconn.Message;
import com.nuevatel.common.appconn.TaskSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Keep a register of each connected remote id. Dispatch messages with auto select remote id.
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/16/15.
 */
public class McSimAppServer extends AppServer {

    private static Logger logger = LogManager.getLogger(McSimAppServer.class);

    /**
     * Connected remote ids
     */
    private Set<Integer>remoteIds = Collections.synchronizedSet(new HashSet<>());

    /**
     * Iterate through remote ids.
     */
    private Iterator<Integer>iterator = null;

    /**
     * Indicates if remote ids collection was modified. This is a flag use to reset the iterator.
     */
    private boolean remoteIdsChanged = false;

    /**
     * {@inheritDoc}
     */
    public McSimAppServer(int localId, TaskSet taskSet, Properties properties) {
        super(localId, taskSet, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Conn conn) {
        if (conn == null) {
            return;
        }
        // register remote id
        remoteIds.add(conn.getRemoteId());
        remoteIdsChanged = true;
        super.add(conn);
        logger.info("Remote connection was appended remoteId:{}", conn.getRemoteId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Conn conn) {
        if (conn == null) {
            return;
        }
        super.remove(conn);
        // remote remote id and reset iterator
        remoteIds.remove(conn.getRemoteId());
        remoteIdsChanged = true;
        logger.info("Detach remote connection remoteId:{}", conn.getRemoteId());
    }

    /**
     * Dispatch message with automatic remote id selection.
     *
     * @param msg
     * @return
     * @throws Exception
     */
    public synchronized Message dispatch(Message msg) throws Exception {
        if (remoteIds.isEmpty()) {
            // No remote ids
            return null;
        }
        // select remote id
        if (remoteIdsChanged || iterator == null || !iterator.hasNext()) {
            // reset iterator
            iterator = remoteIds.iterator();
        }
        // dispatch message with next
        return super.dispatch(iterator.next(), msg);
    }

    /**
     *
     * @return <code>true</code> if no
     */
    public boolean isEmpty() {
        return remoteIds.isEmpty();
    }
}
