package org.openjavacard.tool.command.base;

import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicCommand implements Runnable {

    @Parameter(
            names = {"--help", "-h"}, order = 50,
            description = "Show help"
    )
    protected boolean aHelp;

    protected final Logger LOG;

    public BasicCommand() {
        LOG = LoggerFactory.getLogger(getClass());
    }

    @Override
    public void run() {
        LOG.trace("run()");
        try {
            prepare();
            beforeExecute();
            execute();
            afterExecute();
        } catch (HelpException e) {
            throw new Error("Help not implemented");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void prepare() throws Exception {
        LOG.trace("prepare()");
        // abort and show help if requested
        if(aHelp) {
            throw new HelpException(this);
        }
    }

    protected void beforeExecute() throws Exception {
        LOG.trace("beforeExecute()");
    }

    protected void execute() throws Exception {
        LOG.trace("execute()");
    }

    protected void afterExecute() throws Exception {
        LOG.trace("afterExecute()");
    }

}
