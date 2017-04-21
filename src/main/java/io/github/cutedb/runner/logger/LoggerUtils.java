package io.github.cutedb.runner.logger;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import org.slf4j.LoggerFactory;

/**
 * Created by barmi83 on 4/14/17.
 */
public class LoggerUtils {

    public static Logger createLoggerFor(String string, String serverUrl) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();

        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setContext(lc);
        ple.start();
        CuteDbServerAppender cuteDbServerAppender = new CuteDbServerAppender();
        cuteDbServerAppender.setContext(lc);
        cuteDbServerAppender.setServer(serverUrl);
        cuteDbServerAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(string);
        logger.addAppender(cuteDbServerAppender);
        return logger;
    }
}
