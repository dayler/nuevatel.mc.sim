package com.nuevatel.mcsim;

import com.nuevatel.mcsim.domain.Dialog;

import java.util.Iterator;
import java.util.Map;

/**
 * Consume dialogMap, iterate over it till it is finished.
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/15/15.
 */
public class DialogService {

    private Iterator<Map.Entry<Long, Dialog>> iterator = null;

    public DialogService(Map<Long, Dialog>dialogMap) {
        iterator = dialogMap.entrySet().iterator();
    }

    public synchronized Dialog next() {
        if (iterator.hasNext()) {
            Map.Entry<Long, Dialog>entry = iterator.next();
            return entry.getValue();
        }
        return null;
    }
}
