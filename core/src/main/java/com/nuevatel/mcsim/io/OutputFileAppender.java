package com.nuevatel.mcsim.io;

import com.nuevatel.common.util.Parameters;
import com.nuevatel.common.util.date.DateFormatter;
import com.nuevatel.mcsim.domain.ActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Write output in the specified path. Lines must to write in the specific order.
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/17/15.
 */
public class OutputFileAppender {

    public static final long TIMEOUT_QUEUE = 500L;
    private static Logger logger = LogManager.getLogger(OutputFileAppender.class);

    private String outputPath;

    private BlockingQueue<ActionResult> results = new LinkedBlockingQueue<>();

    private boolean running = false;

    public OutputFileAppender(String outputPath) {
        Parameters.checkNull(outputPath, "outputPath");

        this.outputPath = outputPath;
    }

    /**
     * Append all rs:ActionResults to be write in the output
     *
     * @param rs
     * @throws InterruptedException
     */
    public synchronized void offerAll(List<ActionResult>rs) throws InterruptedException {
        for (ActionResult r : rs) {
            results.offer(r, TIMEOUT_QUEUE, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Initialize output file appender, to be ready to write the output.
     *
     * @throws IOException
     */
    public void start() {
        try {
            // Create new output
            File file = new File(outputPath);
            if (file.exists()) {
                // move
                Path source = Paths.get(outputPath);
                Path fileName = source.getFileName();
                Files.move(source, source.resolveSibling(fileName.normalize().toString() + "_" + DateFormatter.CUSTOM.format(new Date(), "yyyyMMdd_HHmmss")));
            } else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            running = true;
            ByteBuffer buff = ByteBuffer.allocate(1024);
            try (RandomAccessFile raFile = new RandomAccessFile(file, "rw");
                 FileChannel fileChannel = raFile.getChannel()) {
                // write header
                writeHeader(fileChannel);
                // Consume results
                while (running) {
                    // Try get result
                    ActionResult result = pollActionResult();
                    if (result == null) {
                        continue;
                    }
                    // make buff
                    buff.clear();
                    buff.put(result.toString().getBytes());
                    buff.put(System.lineSeparator().getBytes());
                    buff.flip();
                    // write
                    while (buff.hasRemaining()) {
                        fileChannel.write(buff);
                    }
                }
            }
        } catch (IOException ex) {
            logger.error("An Exception occurred", ex);
        }
    }

    /**
     * Write first row of the file.
     *
     * @param fileChannel
     * @throws IOException
     */
    private void writeHeader(FileChannel fileChannel) throws IOException {
        ByteBuffer tmpBuff = ByteBuffer.allocate(40);
        tmpBuff.clear();
        tmpBuff.put("id,dialogId,seqId,assert,code,metadata".getBytes());
        tmpBuff.put(System.lineSeparator().getBytes());
        tmpBuff.flip();
        fileChannel.write(tmpBuff);
    }

    /**
     *
     * @return ActionResult to process. <b>TIMEOUT 500ms<b/> after that returns <code>null</code>
     */
    private ActionResult pollActionResult() {
        try {
             return results.poll(TIMEOUT_QUEUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            return null;
        }
    }

    /**
     * Stop write actions in the output file
     */
    public void shutdown() {
        if (!results.isEmpty()) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                // no op
            }
        }
        running = false;
    }
}
