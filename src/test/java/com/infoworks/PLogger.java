package com.infoworks;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PLogger {

    private final Logger LOG;
    private final Stopwatch watch;

    public PLogger() {
        this(Logger.getLogger(PLogger.class.getSimpleName()));
    }

    public PLogger(Logger LOG) {
        this.LOG = LOG;
        this.watch = Stopwatch.createStarted();
    }

    public void printMillis(String tag) {
        if (tag == null) tag = "";
        LOG.info("Time of execution in " + tag + ": " + watch.elapsed(TimeUnit.MILLISECONDS) + "ms");
    }

    public void printSeconds(String tag) {
        if (tag == null) tag = "";
        LOG.info("Time of execution in " + tag + ": " + watch.elapsed(TimeUnit.SECONDS) + "s");
    }
}
