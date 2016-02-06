package com.nuevatel.mcsim;

import com.nuevatel.common.appconn.TaskSet;
import com.nuevatel.common.util.IntegerUtil;
import com.nuevatel.common.util.LongUtil;
import com.nuevatel.common.util.Parameters;
import com.nuevatel.mcsim.appconn.McSimAppServer;
import com.nuevatel.mcsim.domain.Config;
import com.nuevatel.mcsim.exception.NoRegisteredProcessorsException;
import com.nuevatel.mcsim.exception.NullOutputFileAppender;
import com.nuevatel.mcsim.io.OutputFileAppender;

import java.util.Properties;

/**
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/14/15.
 */
public final class AllocatorService {

    private static McSimAppServer appServer = null;

    private static Config config = null;

    private static ServerProcessor[] processors;

    private static OutputFileAppender outputFileAppender = null;

    public static void setOutputFileAppender(OutputFileAppender outputFileAppender) {
        AllocatorService.outputFileAppender = outputFileAppender;
    }

    public static OutputFileAppender getOutputFileAppender() {
        if (outputFileAppender == null) {
            throw new NullOutputFileAppender();
        }
        return outputFileAppender;
    }

    public static Config getConfig() {
        return config;
    }

    public static ServerProcessor findProcessor(Long dialogId) {
        if (processors == null) {
            throw new NoRegisteredProcessorsException();
        }
        for (ServerProcessor p : processors) {
            if ((long)dialogId == p.getDialogId()) {
                // Processor found
                return p;
            }
        }
        // No processor found
        return null;
    }

    public static void setProcessors(ServerProcessor[] processors) {
        AllocatorService.processors = processors;
    }

    public static void startAppServer(Integer id, TaskSet taskSet, Properties prop) throws Exception {
        if (appServer != null) {
            interruptAppServer();
        }
        appServer = new McSimAppServer(id, taskSet, prop);
        appServer.start();
    }

    public synchronized static void interruptAppServer() {
        if (appServer != null) {
            appServer.interrupt();
            appServer = null;
        }
    }

    public static McSimAppServer getAppServer() {
        return appServer;
    }

    public static void loadConfig(Properties prop, String input, String output) {
        Parameters.checkNull(prop, "prop");

        Config cfg = new Config();
        cfg.setPeriod(LongUtil.tryParse(prop.getProperty(PropName.period.property())));
        cfg.setnTimes(IntegerUtil.tryParse(prop.getProperty(PropName.nTimes.property())));
        cfg.setProperties(prop);
        cfg.setConcurrencyLevel(IntegerUtil.tryParse(prop.getProperty(PropName.concurrencyLevel.property())));
        cfg.setInputFilePath(input);
        cfg.setOutputFilePath(output);

        cfg.setCyclicExecution(Boolean.parseBoolean(prop.getProperty(PropName.cyclicExecution.property())));
        //
        AllocatorService.config = cfg;
    }
}
