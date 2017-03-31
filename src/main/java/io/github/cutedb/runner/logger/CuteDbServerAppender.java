package io.github.cutedb.runner.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.LayoutBase;

/**
 * Created by barmi83 on 2/3/17.
 */
public class CuteDbServerAppender extends AppenderBase<ILoggingEvent>
{
    String server;

    private static Layout<ILoggingEvent> defaultLayout = new LayoutBase<ILoggingEvent>() {
        public String doLayout(ILoggingEvent event) {
            return "-- [" + event.getLevel() + "]" +
                    event.getLoggerName() + " - " +
                    event.getFormattedMessage().replaceAll("\n", "\n\t");
        }
    };

    @Override
    public void start() {
        if (this.server == null) {
            addError("No server set for the appender named ["+ name +"].");
            return;
        }

        super.start();
    }


    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {

    }

    private void sendMessageToServer(final ILoggingEvent evt){
        CuteDbLog log = new CuteDbLog();
        log.setTimestamp(evt.getTimeStamp());
        log.setLevel(evt.getLevel().levelStr);
        log.setMessage(evt.getFormattedMessage());


    }
}
