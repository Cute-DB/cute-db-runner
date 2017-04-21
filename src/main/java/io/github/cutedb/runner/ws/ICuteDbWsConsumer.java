package io.github.cutedb.runner.ws;

import io.github.cutedb.runner.dto.Lint;
import io.github.cutedb.runner.dto.Run;
import io.github.cutedb.runner.logger.CuteDbLog;

/**
 * Created by barmi83 on 29/07/16.
 */
public interface ICuteDbWsConsumer {

    public static final String BEAN_ID = "cuteDbWsConsumer";

    public String generateRunUuid();

    public Run createNewRun(Run newRun);

    public Lint createNewLint(Lint newLint);

    public Run updateRun(Run run);

    public void sendLog(CuteDbLog log);

    }
