package com.nuevatel.mcsim.server;

import com.nuevatel.common.Processor;
import com.nuevatel.common.util.CSVUtil;
import com.nuevatel.common.util.IntegerUtil;
import com.nuevatel.common.util.LongUtil;
import com.nuevatel.common.util.StrSubstitutor;
import com.nuevatel.mcsim.AllocatorService;
import com.nuevatel.mcsim.DialogService;
import com.nuevatel.mcsim.McMessageFactory;
import com.nuevatel.mcsim.ServerProcessor;
import com.nuevatel.mcsim.domain.Action;
import com.nuevatel.mcsim.domain.AssertAction;
import com.nuevatel.mcsim.domain.Config;
import com.nuevatel.mcsim.domain.Dialog;
import com.nuevatel.mcsim.domain.MessageAction;
import com.nuevatel.mcsim.io.OutputFileAppender;
import com.nuevatel.mcsim.util.SimpleStrSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Ariel D. Salazar H.
 *
 */
public class ServerAppProcessor implements Processor {
    
    private static Logger logger = LogManager.getLogger(ServerAppProcessor.class);

    public static final int IDX_DIALOG_ID = 1;
    public static final int IDX_SEQ_ID = 2;
    public static final int IDX_TYPE = 3;
    public static final int IDX_COMMAND_NAME = 4;
    public static final int IDX_METADATA = 5;

    private Map<Long, Dialog>dialogMap = Collections.synchronizedMap(new LinkedHashMap<>());

    private ExecutorService service = null;

    private Config config;

    private String inputFile;

    private String outputFile;

    private DialogService dialogService;

    private ServerProcessor[] processors;

    private OutputFileAppender outFileAppender;

    private ExecutorService outFileService = Executors.newSingleThreadExecutor();

    private OutputFileAppender fileAppender;

    private StrSubstitutor strSubstitutor = new SimpleStrSubstitutor();

    public ServerAppProcessor() {
        config = AllocatorService.getConfig();
        service = Executors.newFixedThreadPool(config.getConcurrencyLevel());
        // It is populated when the processors are creating.
        processors = new ServerProcessor[config.getConcurrencyLevel()];
        this.inputFile = config.getInputFilePath();
        this.outputFile = config.getOutputFilePath();
        strSubstitutor.load(config.getProperties());
    }

    @Override
    public void execute() {
        logger.info("Load dialogs from {} output {}", inputFile, outputFile);
        try {
            initializeOutputFileService();
            logger.info("Initialized OutputFileService...");
            while (config.isCyclicExecution()
                   && (config.getnTimes() == 0 || config.getExecutionCount() < config.getnTimes())) {
                config.incExecutionCount();
                loadDialogs(inputFile);
                dialogService = new DialogService(dialogMap);
                processDialogs();
                // sleep if is cyclic process
                if (config.isCyclicExecution() && config.getExecutionCount() < config.getnTimes()) {
                    Thread.sleep(config.getPeriod());
                    dialogMap.clear();
                }
            }
            // await 60 sec to finish
            shutdown(60);
        } catch (Throwable ex) {
            logger.error("Failed to start ServerAppProcessor...", ex);
        }
    }

    @Override
    public void shutdown(int i) {
        try {
            service.shutdown();
            service.awaitTermination(i, TimeUnit.SECONDS);
            logger.info("Shutdown ServerAppProcessor...");
            shutdownOutputFileService();
            logger.info("Shutdown OutputFileAppender...");
            // Finish AppConnServer
            AllocatorService.interruptAppServer();
        } catch (InterruptedException ex) {
            logger.error("Failed to shutdown ServerAppProcessor...", ex);
        }
    }

    /**
     * Initialize dialog processors.
     */
    private void processDialogs() throws InterruptedException, ExecutionException {
        ServerProcessor[] processors = new ServerProcessor[config.getConcurrencyLevel()];
        List<Callable<Integer>>callableList = new ArrayList<>();
        for (int i = 0; i < config.getConcurrencyLevel(); i++) {
            ServerProcessor processor = new ServerProcessor(dialogService);
            callableList.add(processor);
        }
        // Register processors
        callableList.toArray(processors);
        // register processors
        AllocatorService.setProcessors(processors);
        // Throw processors and await till it is finished
        List<Future<Integer>>futureList = service.invokeAll(callableList);

        // Await to finish all ServerProcessors
        for (Future<Integer>future : futureList) {
            Integer resp = future.get();
            logger.info("ServerProcessor was finished resp:[{}]", resp);
        }
    }

    private void initializeOutputFileService() {
        fileAppender = new OutputFileAppender(config.getOutputFilePath());
        AllocatorService.setOutputFileAppender(fileAppender);
        outFileService.execute(()->fileAppender.start());
    }

    private void shutdownOutputFileService() throws InterruptedException {
        fileAppender.shutdown();
        outFileService.shutdown();
        outFileService.awaitTermination(60L, TimeUnit.MILLISECONDS);
    }

    /**
     * Load all dialogs from input file.
     *
     * @param inputFile Path ro reach dialog definition.
     * @throws IOException
     */
    private void loadDialogs(String inputFile) throws IOException {
        try(InputStream in = getInputFileStream(inputFile)) {
            // ignore first row
            List<String[]>metadata = CSVUtil.read(in, 1, "#");
            metadata.forEach((m) -> processRow(m));
            logger.info("{} registers was loaded...", metadata.size());
        }
    }

    private InputStream getInputFileStream(String inputFile) throws FileNotFoundException {
        if (inputFile == null) {
            // get default from resources
            logger.warn("Default input /input.csv");
            return getClass().getResourceAsStream("/input.csv");
        }
        logger.info("Input from {}", inputFile);
        return new FileInputStream(inputFile);
    }

    /**
     * Take a row from input. Creates dialog if it does not exists, add the action related to row.
     *
     * @param data
     */
    private void processRow(String[] data) {
        if (data.length < 6) {
            // Needs 6 fields to process
            return;
        }
        // Resolve parametrized values
        for (int i = 0; i < data.length; i++) {
            data[i] = strSubstitutor.replace(data[i]);
        }

        Long dialogId = LongUtil.tryParse(data[IDX_DIALOG_ID]);
        // get dialog, or create it if it does not exists
        if (dialogId != null) {
            Dialog tmpDialog;
            if((tmpDialog = dialogMap.get(dialogId)) == null) {
                tmpDialog = new Dialog(dialogId);
                // registers dialog on dialog map
                dialogMap.put(dialogId, tmpDialog);
            }
            // get variables to make action
            int seqId = IntegerUtil.tryParse(data[IDX_SEQ_ID]);
            Action.Type type = Action.Type.valueOf(data[IDX_TYPE]);
            String cmdName = data[IDX_COMMAND_NAME];
            String rawMetadata = data[IDX_METADATA];
            // make action
            Action action;
            String[] metadata = rawMetadata.split("\\|");
            McMessageFactory factory = McMessageFactory.fromName(cmdName);
            if (Action.Type.send.equals(type)) {
                // sender
                action = new MessageAction(seqId,
                                           type,
                                           Action.ConcurrencyType.sync,
                                           factory.getMcMsgToMetadataPredicate(),
                                           factory.fromMetadata(metadata));
            } else {
                // assert
                action = new AssertAction(seqId, // sequence number
                                          type, // indicates send or assert
                                          factory.getConcurrencyType(), // async or sync message
                                          factory.getPredicate(), // define predicate to do assertion
                                          factory.getMcMsgToMetadataPredicate(), // predicate to get string representation for McMsg
                                          factory.fromMetadata(metadata)); // metadata to build message
            }
            // add action
            tmpDialog.addAction(action);
        }
    }
}
