package org.motechproject.voiceserver;

import org.apache.log4j.Logger;
import org.jvoicexml.ConnectionInformation;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.Session;
import org.jvoicexml.event.ErrorEvent;

import java.io.File;

public class VoiceServer {
    private ServerLogAppender appender;
    private final JVoiceXmlMain jVoiceXml;

    public VoiceServer() {
        final Logger rootLogger = Logger.getRootLogger();
        appender = new ServerLogAppender();
        rootLogger.addAppender(appender);

        String pathToConfigDir = new File(getClass().getResource("/jvoicexml.xml").getFile()).getParent();
        System.setProperty("jvoicexml.config", pathToConfigDir);
        jVoiceXml = new JVoiceXmlMain();
    }

    public Session createSession(ConnectionInformation connectionInformation) throws ErrorEvent {
        return jVoiceXml.createSession(connectionInformation);
    }

    public VoiceServer start() {
        jVoiceXml.start();
        appender.waitTillServerHasStarted();
        return this;
    }

    public void shutdown() {
        jVoiceXml.shutdown();
        jVoiceXml.waitShutdownComplete();

        killCrazyTerminationThreadWhichKillsTheJVM();
    }

    private void killCrazyTerminationThreadWhichKillsTheJVM() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        Thread[] activeThreads = new Thread[group.activeCount() * 2];

        int numberOfThreads = group.enumerate(activeThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            Thread thread = activeThreads[i];
            if (thread.getName().equals("TerminationThread")) {
                thread.interrupt();
            }
        }
    }
}
