package io.github.cutedb.runner;


import io.github.cutedb.runner.dto.BuildStatus;
import io.github.cutedb.runner.dto.LintSeverity;
import io.github.cutedb.runner.dto.Run;
import io.github.cutedb.runner.exceptions.CuteDbRunnerException;
import io.github.cutedb.runner.logger.LoggerUtils;
import io.github.cutedb.runner.ws.CuteDbWsConsumer;
import org.slf4j.Logger;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.executable.BaseStagedExecutable;
import schemacrawler.tools.lint.*;
import schemacrawler.tools.lint.executable.LintOptions;
import schemacrawler.tools.lint.executable.LintOptionsBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DbRunnerExecutable extends BaseStagedExecutable
{

    private Logger LOGGER;

    public static final String COMMAND = "cutedb";
    public static final String CUTEDB_SERVER_PARAMETER = "cutedbserver";
    public static final String CUTEDB_SERVER_PORT_PARAMETER = "cutedbserverport";
    public static final String CUTEDB_SERVER_PORT = "9000";


    CuteDbWsConsumer cuteDbWsConsumer;
    Run currentRun;

    protected DbRunnerExecutable()
    {
        super(COMMAND);
    }

    @Override
    /**
     * Execute the command
     * Initiate a new run by asking an unique id, check the database, process the results, and send data to remote cute-db-server
     */
    public void executeOn(final Catalog catalog, final Connection connection) throws CuteDbRunnerException {

        if(additionalConfiguration != null &&  additionalConfiguration.containsKey(CUTEDB_SERVER_PARAMETER)){

            String server = additionalConfiguration.get(CUTEDB_SERVER_PARAMETER);
            String port = additionalConfiguration.getStringValue(CUTEDB_SERVER_PORT_PARAMETER, CUTEDB_SERVER_PORT);

            LOGGER = LoggerUtils.createLoggerFor(DbRunnerExecutable.class.getName(), "http://"+server+":"+port);

//            ((CuteDbServerAppender)LOGGER).setServer("http://"+server+":"+port);
//            System.setProperty("server", "http://"+server+":"+port);

            cuteDbWsConsumer = new CuteDbWsConsumer(server, port);

        }
        else
            throw new CuteDbRunnerException("cutedbserver url is missing.");

        currentRun = initiateANewRun(catalog);

        try {
            final LintedCatalog lintedCatalog = createLintedCatalog(catalog, connection);
            processLints(lintedCatalog);
        }catch (SchemaCrawlerException e){
            LOGGER.error("The run failed : "+e.getMessage(), e);
            if(currentRun != null){
                currentRun.setStatus(BuildStatus.FAILURE);
                currentRun.setEnded(new Date());
                currentRun.setReason("The run failed : "+e.getMessage());
            }
        }finally {
            if(currentRun != null)
                sendDataToServer();
        }



    }

    /**
     * Check database with SchemaCrawler lint plugin
     * @param catalog
     * @param connection
     * @return
     * @throws SchemaCrawlerException
     */
    private LintedCatalog createLintedCatalog(final Catalog catalog, final Connection connection) throws SchemaCrawlerException
    {
        final LintOptions lintOptions = new LintOptionsBuilder().fromConfig(additionalConfiguration).toOptions();

        final LinterConfigs linterConfigs = LintUtility.readLinterConfigs(lintOptions, getAdditionalConfiguration());
        final Linters linters = new Linters(linterConfigs);

        return new LintedCatalog(catalog, connection, linters);
    }

    /**
     * Send Run data to remote server
     */
    private void sendDataToServer(){
        cuteDbWsConsumer.updateRun(currentRun);
    }


    /**
     * Process Schemacrawler lints
     * @param lintedCatalog
     */
    private void processLints(final LintedCatalog lintedCatalog)
    {
        LOGGER.info("Start sending data");
        Stream<Lint<?>> scLints = StreamSupport.stream(lintedCatalog.getCollector().spliterator(),  false);

        // Count critical hits
        scLints.forEach(scLint->{
           io.github.cutedb.runner.dto.Lint newLint = new io.github.cutedb.runner.dto.Lint();
            newLint.setUuid(UUID.randomUUID().toString());
            newLint.setLinter(scLint.getLinterId());
            newLint.setMessage(scLint.getMessage());
            newLint.setObjectName(scLint.getObjectName());
            newLint.setSeverity(LintSeverity.getSeverity(scLint.getSeverity().name()));
            newLint.setValue(scLint.getValueAsString());
            newLint.setRunUuid(currentRun.getUuid());
            cuteDbWsConsumer.createNewLint(newLint);
        });

       currentRun.setStatus(BuildStatus.SUCCESS);
       currentRun.setEnded(new Date());
    }

    /**
     * Initiate a new Run :
     * - ask for an unique ID
     * - set basic run information
     * - send the new run to remote server
     * @param lintedCatalog
     * @return
     * @throws CuteDbRunnerException
     */
    private Run initiateANewRun(final Catalog lintedCatalog) throws CuteDbRunnerException {
        String uuid = cuteDbWsConsumer.generateRunUuid();

        Run newRun = new Run();
        newRun.setUuid(uuid);
        newRun.setJdbcUrl(lintedCatalog.getJdbcDriverInfo().getConnectionUrl());
        newRun.setDatabaseProductName(lintedCatalog.getDatabaseInfo().getProductName());
        newRun.setServer(additionalConfiguration.get("server"));
        newRun.setStatus(BuildStatus.RUNNING);
        newRun.setDbName(additionalConfiguration.get("database"));
        newRun.setDbHost(additionalConfiguration.get("host"));
        try {
            newRun.setHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage(), e);
            throw new CuteDbRunnerException("Unable to get host name", e.getCause());
        }
        Run remoteRun = cuteDbWsConsumer.createNewRun(newRun);
        return remoteRun;
    }


//    /**
//     * Obtain linter configuration from a system property
//     *
//     * @return LinterConfigs
//     * @throws SchemaCrawlerException
//     */
//    private static LinterConfigs readLinterConfigs(final LintOptions lintOptions, final Config config)
//    {
//        final LinterConfigs linterConfigs = new LinterConfigs(config);
//        String linterConfigsFile = null;
//        try
//        {
//            linterConfigsFile = lintOptions.getLinterConfigs();
//            if (!isBlank(linterConfigsFile))
//            {
//                final Path linterConfigsFilePath = Paths.get(linterConfigsFile).toAbsolutePath();
//                if (isRegularFile(linterConfigsFilePath) && isReadable(linterConfigsFilePath))
//                {
//                    linterConfigs.parse(newBufferedReader(linterConfigsFilePath));
//                }
//                else
//                {
//                    LOGGER.log(Level.WARNING, "Could not find linter configs file, " + linterConfigsFile);
//                }
//            }
//            else
//            {
//                LOGGER.log(Level.CONFIG, "Using default linter configs");
//            }
//
//            return linterConfigs;
//        }
//        catch (final Exception e)
//        {
//            LOGGER.log(Level.WARNING, "Could not load linter configs from file, " + linterConfigsFile, e);
//            return linterConfigs;
//        }
//    }

}
