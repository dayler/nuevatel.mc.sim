package com.nuevatel.mcsim.domain;

import java.util.Properties;

/**
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/14/15.
 */
public class Config {

    private int concurrencyLevel;

    private String output;

    private String input;

    private Long period;

    private int nTimes;

    private boolean cyclicExecution = false;

    private Properties properties;

    private int executionCount = 0;

    public int getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public void setConcurrencyLevel(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    public String getOutputFilePath() {
        return output;
    }

    public void setOutputFilePath(String output) {
        this.output = output;
    }

    public String getInputFilePath() {
        return input;
    }

    public void setInputFilePath(String input) {
        this.input = input;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Long getPeriod() {
        return period;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }

    public int getnTimes() {
        return nTimes;
    }

    public void setnTimes(int nTimes) {
        this.nTimes = nTimes;
    }

    public boolean isCyclicExecution() {
        return cyclicExecution;
    }

    public void setCyclicExecution(boolean cyclicExecution) {
        this.cyclicExecution = cyclicExecution;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public synchronized void incExecutionCount() {
        // inc execution count
        executionCount++;
    }
}
