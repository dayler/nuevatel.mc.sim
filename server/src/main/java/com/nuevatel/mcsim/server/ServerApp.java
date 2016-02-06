package com.nuevatel.mcsim.server;

import com.nuevatel.common.Processor;
import com.nuevatel.common.ShutdownHook;
import com.nuevatel.common.appconn.TaskSet;
import com.nuevatel.common.util.IntegerUtil;
import com.nuevatel.mc.appconn.McMessage;
import com.nuevatel.mcsim.AllocatorService;
import com.nuevatel.mcsim.PropName;
import com.nuevatel.mcsim.server.appconn.task.ForwardShortMessageMtAsyncCallTask;
import com.nuevatel.mcsim.server.appconn.task.SendRoutingInfoForShortMessageAsyncCallTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 *
 * @author Ariel D. Salazar H.
 */
public class ServerApp
{
    private static Logger logger = LogManager.getLogger(ServerApp.class);

    public static void main( String[] args )
    {
        String propVal = System.getProperty("log4j.configurationFile");
        System.out.println("Log4j2: " + propVal);
        logger.info("Log4j2: {}", propVal);

        try {
            if (args.length < 3) {
                logger.warn("No args, default properties is loading...");
                logger.warn("Usage: ServerApp mc.sim-server.properties input.csv output.csv");
            }
            // load properties
            Properties properties = loadProperties(args);
            AllocatorService.loadConfig(properties, getInputFile(args)/* input */, getOutputFile(args) /* output */);
            // initialize app server
            TaskSet taskSet = makeTaskSet();
            Integer appId = IntegerUtil.tryParse(properties.getProperty(PropName.appId.property()));
            AllocatorService.startAppServer(appId, taskSet, properties);
            logger.info("AppServer {} was initialized...", appId);
            // wait 1 minute to finish all process, 1 thread to do.
            ShutdownHook hook = new ShutdownHook(60, 1);
            Processor appProcessor = new ServerAppProcessor();
            hook.appendProcess(appProcessor);
            logger.info("ServerApp was started...");
            // Start server app processor
            appProcessor.execute();
            // register hook
            Runtime.getRuntime().addShutdownHook(hook);
        } catch (Throwable ex) {
            logger.fatal("Failed to initialize ServerApp...", ex);
            System.exit(-1);
        }
    }

    private static TaskSet makeTaskSet() {
        TaskSet taskSet = new TaskSet();
        taskSet.add(McMessage.SRIFSM_RET_ASYNC_CALL,
                    new SendRoutingInfoForShortMessageAsyncCallTask());
        taskSet.add(McMessage.FORWARD_SM_O_RET_ASYNC_CALL,
                    new ForwardShortMessageMtAsyncCallTask());
        return taskSet;
    }

    private static String getInputFile(String[] args) {
        if (args.length < 3) {
            return null;
        }
        return args[1];
    }

    private static String getOutputFile(String[] args) {
        if (args.length < 3) {
            return "/tmp/output.csv";
        }
        return args[2];
    }

    static Properties loadProperties(String[] args) throws IOException {
        try (InputStream is = getPropertiesInputStream(args)) {
            Properties prop = new Properties();
            prop.load(is);
            return prop;
        }
    }

    private static InputStream getPropertiesInputStream(String[] args) throws FileNotFoundException {
        if (args.length < 3) {
            logger.warn("Default properties /mc.sim-server.properties");
            return ServerApp.class.getResourceAsStream("/mc.sim-server.properties");
        }
        logger.info("Properties from {}", args[0]);
        return new FileInputStream(args[0]);
    }
}
