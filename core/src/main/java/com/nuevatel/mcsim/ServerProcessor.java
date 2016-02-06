package com.nuevatel.mcsim;

import com.nuevatel.common.appconn.Message;
import com.nuevatel.common.thread.SimpleMonitor;
import com.nuevatel.common.util.Parameters;
import com.nuevatel.mcsim.appconn.McSimAppServer;
import com.nuevatel.mcsim.domain.Action;
import com.nuevatel.mcsim.domain.ActionResult;
import com.nuevatel.mcsim.domain.AssertAction;
import com.nuevatel.mcsim.domain.Dialog;
import com.nuevatel.mcsim.domain.DialogTx;
import com.nuevatel.mcsim.domain.MessageAction;
import com.nuevatel.mcsim.io.OutputFileAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

import static com.nuevatel.common.util.Util.*;

/**
 * Process the dialogs. Handle all lifecycle.
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/15/15.
 */
public class ServerProcessor implements Callable<Integer> {

    public static final long TIMEOUT_AWAIT_ASYNC_RESPONSE = 10000L;
    private static Logger logger = LogManager.getLogger(ServerProcessor.class);

    private SimpleMonitor sync = new SimpleMonitor();

    private DialogService dialogService;

    private Long dialogId;

    private Message currentMessage;

    private McSimAppServer appServer = AllocatorService.getAppServer();

    private OutputFileAppender outputFileAppender = AllocatorService.getOutputFileAppender();

    public ServerProcessor(DialogService dialogService) {
        Parameters.checkNull(dialogService, "dialogService");
        this.dialogService = dialogService;
    }

    private void start() {
        try {
            logger.info("Start ServerProcessor...");
            // await by client connection
            while (appServer.isEmpty()) {
                logger.info("Awaiting by client connections...");
                sleep(5000L); // sleep 5 seconds
            }
            // while has dialogs to process
            Dialog dialog = dialogService.next(); // try to pickup next dialog
            while (dialog != null) {
                takeDialogId(dialog.getDialogId());
                // Process all of its actions
                processDialog(dialog);
                logger.info("processDialog({})", dialogId);
                // try to get next dialog
                dialog = dialogService.next();
            }
            logger.info("No more dialogs to process...");
        } catch (Throwable ex) {
            logger.error("Failed on execute ServerProcessor", ex);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // no op
        }
    }

    private void processDialog(Dialog dialog) throws InterruptedException {
        Action action = dialog.next(); // pick up it
        DialogTx dialogTx = new DialogTx();
        while (action != null) {
            ActionResult actionResult = new ActionResult(dialogId, action.getSeqId());
            try {
                if (action instanceof MessageAction) {
                    // Log send message
                    Message msg = action.getMsg().toMessage();
                    actionResult.setMetadata(getMessageCode(msg), action.getMcMsgMetadata(msg));
                    // Send message an await by response, if the response is async it returns <code>null</code>
                    currentMessage = dispatch(castAs(MessageAction.class, action));
                    actionResult.ok();
                } else if (action instanceof AssertAction) {
                    // Assert action
                    assertAction(castAs(AssertAction.class, action), actionResult);
                    // set message to null to prevent false assert
                    currentMessage = null;
                } else {
                    // No op
                    actionResult.failed();
                }
            } catch (Throwable ex) {
                logger.error("Failed to execute an action. dialogId:{} actionSeq:{}",
                             dialog.getDialogId(), action == null ? null : action.getSeqId(), ex);
                // Set result to failed
                actionResult.setMetadata(-1, ex.getClass().getName() + ":" + ex.getMessage());
                actionResult.failed();
            }
            // pick up next action
            action = dialog.next();
            // Append action result
            dialogTx.append(actionResult);
        }
        // write result
        outputFileAppender.offerAll(dialogTx.getResults());
    }

    private void assertAction(AssertAction assertAction, ActionResult actionResult) throws InterruptedException {
        if (Action.ConcurrencyType.sync.equals(assertAction.getConcurrencyType())) {
            // assert with previous message
            actionResult.setMetadata(getMessageCode(currentMessage), assertAction.getMcMsgMetadata(currentMessage));
            actionResult.setResult(assertAction.doAssert(currentMessage) ? ActionResult.Result.ok : ActionResult.Result.failed);
            return;
        }

        if (currentMessage == null) {
            // await till get message response
            sync.doWait(TIMEOUT_AWAIT_ASYNC_RESPONSE);
        }

        if (currentMessage == null) {
            logger.warn("Aync response is null. dialogId={}", dialogId);
        }
        // Async assert action
        actionResult.

                setMetadata(getMessageCode(currentMessage), assertAction.getMcMsgMetadata(currentMessage));
        actionResult.setResult(assertAction.doAssert(currentMessage) ? ActionResult.Result.ok : ActionResult.Result.failed);
    }

    /**
     *
     * @param msg
     * @return Code to corresponds with Message. If is null return null.
     */
    private int getMessageCode(Message msg) {
        if (msg == null) {
            return -1;
        }
        return msg.getCode();
    }

    private Message dispatch(MessageAction msgAction) {
        try {
            return appServer.dispatch(msgAction.getMsg().toMessage());
        } catch (Exception ex) {
            logger.error("Failed to dispatch message.", ex);
            return null;
        }
    }

    public void notifyResponse(Message mcMessage) {
        currentMessage = mcMessage;
        // wake up thread
        sync.doNotifyAll();
    }

    private void takeDialogId(Long tmpDialogId) {
        if (dialogId != tmpDialogId) {
            dialogId = tmpDialogId;
        }
    }

    public synchronized void shutdown() {
        // no op
    }

    public Long getDialogId() {
        return dialogId;
    }

    @Override
    public Integer call() throws Exception {
        try {
            start();
            return 0;
        } catch (Throwable ex) {
            logger.error("Failed to initialize ServerProcessor...", ex);
            return -1;
        }
    }
}
