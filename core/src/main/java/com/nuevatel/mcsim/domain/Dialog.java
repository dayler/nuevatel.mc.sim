package com.nuevatel.mcsim.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Define a model fro a set of appconn message
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/13/15.
 */
public class Dialog {

    private Long dialogId;

    private int index = 0;

    private List<Action>actions = new ArrayList<>();

    public Dialog(Long dialogId) {
        this.dialogId = dialogId;
    }

    public void addAction(Action action) {
        if (action == null) {
            return;
        }
        actions.add(action);
    }

    public Long getDialogId() {
        return dialogId;
    }

    /**
     *
     * @return Next action. <code>null</code> no more actions.
     */
    public Action next() {
        if (actions.size() > index) {
            return actions.get(index++);
        }
        return null;
    }

    public void reset() {
        index = 0;
    }
}
