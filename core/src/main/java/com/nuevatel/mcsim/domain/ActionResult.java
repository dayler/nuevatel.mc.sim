package com.nuevatel.mcsim.domain;

/**
 * The assert action response.
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/17/15.
 */
public class ActionResult {

    private static int indicator = 0;

    private int id;

    private Long dialogId;

    private Integer seqId;

    private Result result = Result.failed;

    private Integer code;

    private String metadata = null;

    public ActionResult(Long dialogId, Integer seqId) {
        this.dialogId = dialogId;
        this.seqId = seqId;
        id = nextInd();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Long getDialogId() {
        return dialogId;
    }

    public Integer getSeqId() {
        return seqId;
    }

    public Result getResult() {
        return result;
    }

    public int getId() {
        return id;
    }

    public void ok() {
        result = Result.ok;
    }

    public void failed() {
        result = Result.failed;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void setMetadata(Integer code, String metadata) {
        this.code = code;
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s", id, dialogId, seqId, result.name(),
                             code == null ? "null" : code.intValue(), metadata);
    }

    public synchronized static int nextInd() {
        return indicator++;
    }

    /**
     * Possible results.
     */
    public enum Result {
        ok, failed,
        ;
    }
}
