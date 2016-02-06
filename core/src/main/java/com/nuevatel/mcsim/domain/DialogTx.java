package com.nuevatel.mcsim.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Assert action for a dialog. This is composed by a set of actions responses.
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/17/15.
 */
public class DialogTx {

    private List<ActionResult>results = new ArrayList<>();

    public void append(ActionResult result) {
        results.add(result);
    }

    public List<ActionResult> getResults() {
        return Collections.unmodifiableList(results);
    }
}
