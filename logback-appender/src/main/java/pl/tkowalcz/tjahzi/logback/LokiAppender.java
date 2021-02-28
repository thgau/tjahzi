package pl.tkowalcz.tjahzi.logback;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import pl.tkowalcz.tjahzi.LoggingSystem;
import pl.tkowalcz.tjahzi.TjahziLogger;

import java.nio.ByteBuffer;
import java.util.Collections;

public class LokiAppender extends LokiAppenderConfigurator {

    private PatternLayoutEncoder encoder;
    private LoggingSystem loggingSystem;

    private TjahziLogger logger;
    private String logLevelLabel;

    public PatternLayoutEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    protected void append(ILoggingEvent event) {
        String logLevel = event.getLevel().toString();
        byte[] logLine = encoder.encode(event);

        logger.log(
                event.getTimeStamp(),
                Collections.emptyMap(),
                logLevelLabel,
                logLevel,
                ByteBuffer.wrap(logLine)
        );
    }

    @Override
    public void start() {
        if (encoder == null) {
            addError("No encoder set for the appender named [" + name + "].");
            return;
        }

        encoder.start();

        LokiAppenderFactory lokiAppenderFactory = new LokiAppenderFactory(this);
        loggingSystem = lokiAppenderFactory.createAppender();
        logLevelLabel = lokiAppenderFactory.getLogLevelLabel();

        logger = loggingSystem.createLogger();
        loggingSystem.start();
        super.start();
    }

    @Override
    public void stop() {
        loggingSystem.close(
                1000,
                thread -> addError("Loki appender was unable to stop thread on shutdown: " + thread)
        );

        super.stop();
    }
}
