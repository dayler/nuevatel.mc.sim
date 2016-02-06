package com.nuevatel.mcsim;

/**
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/14/15.
 */
public enum PropName {
    period("mc.sim.period"),
    nTimes("mc.sim.ntimes"),

    concurrencyLevel("mc.sim-server.concurrencyLevel"),
    appId("appId"),

    cyclicExecution("mc.sim.cyclicExecution"),
    ;

    private String name;

    private PropName(String name) {
        this.name = name;
    }

    public String property() {
        return name;
    }
}
