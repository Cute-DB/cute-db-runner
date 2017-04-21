package io.github.cutedb.runner.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.github.cutedb.runner.ws.CuteDbWsConsumer;

/**
 * Created by barmi83 on 2/3/17.
 */
public class CuteDbServerAppender extends AppenderBase<ILoggingEvent>
{

    String server;
    CuteDbWsConsumer cuteDbWsConsumer;

    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public synchronized void start() {

        if(!isStarted()) {
//            if (Objects.equals(this.server, "cuteDbServer_IS_UNDEFINED")) {
//                addError("No server set for the appender named [" + name + "].");
//                return;
//            }

            if (cuteDbWsConsumer == null) {
                cuteDbWsConsumer = new CuteDbWsConsumer(server);
            }

            super.start();

        }
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
            sendMessageToServer(iLoggingEvent);
    }

    private void sendMessageToServer(final ILoggingEvent evt){
        CuteDbLog log = new CuteDbLog();
        log.setTimestamp(evt.getTimeStamp());
        log.setLevel(evt.getLevel().levelStr);
        log.setMessage(evt.getFormattedMessage());
        cuteDbWsConsumer.sendLog(log);
    }
}
