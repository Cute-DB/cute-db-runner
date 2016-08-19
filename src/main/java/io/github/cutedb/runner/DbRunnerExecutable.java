package io.github.cutedb.runner;


import io.github.cutedb.runner.dto.BuildStatus;
import io.github.cutedb.runner.dto.LintSeverity;
import io.github.cutedb.runner.dto.Run;
import io.github.cutedb.runner.ws.CuteDbWsConsumer;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.executable.BaseStagedExecutable;
import schemacrawler.tools.lint.Lint;
import schemacrawler.tools.lint.LintedCatalog;
import schemacrawler.tools.lint.LinterConfigs;
import schemacrawler.tools.lint.Linters;
import schemacrawler.tools.lint.executable.LintOptions;
import schemacrawler.tools.lint.executable.LintOptionsBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.Files.*;
import static sf.util.Utility.isBlank;

public class DbRunnerExecutable extends BaseStagedExecutable
{

    private static final Logger LOGGER = Logger.getLogger(DbRunnerExecutable.class.getName());

    static final String COMMAND = "cutedbrunner";

    CuteDbWsConsumer cuteDbWsConsumer;
    Run currentRun;

    protected DbRunnerExecutable()
    {
        super(COMMAND);
        cuteDbWsConsumer = new CuteDbWsConsumer();
    }

    @Override
    public void executeOn(final Catalog catalog, final Connection connection) throws Exception
    {

        currentRun = initiateANewRun(catalog);
        try {
            final LintedCatalog lintedCatalog = createLintedCatalog(catalog, connection);
            sendDataToServer(lintedCatalog);
        }catch (Exception e){
            //TODO
            System.out.println("ERROR");
            e.printStackTrace();
        }



    }

    private LintedCatalog createLintedCatalog(final Catalog catalog, final Connection connection) throws SchemaCrawlerException
    {
        final LintOptions lintOptions = new LintOptionsBuilder().fromConfig(additionalConfiguration).toOptions();

        final LinterConfigs linterConfigs = readLinterConfigs(lintOptions, getAdditionalConfiguration());
        final Linters linters = new Linters(linterConfigs);

        return new LintedCatalog(catalog, connection, linters);
    }




    private void sendDataToServer(final LintedCatalog lintedCatalog) throws IOException
    {
        LOGGER.log(Level.INFO, "Start sending data");
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
       cuteDbWsConsumer.updateRun(currentRun);
    }

    private Run initiateANewRun(final Catalog lintedCatalog){
        String uuid = cuteDbWsConsumer.generateRunUuid();

        Run newRun = new Run();
        newRun.setUuid(uuid);
        newRun.setJdbcUrl(lintedCatalog.getJdbcDriverInfo().getConnectionUrl());
        newRun.setDatabaseProductName(lintedCatalog.getDatabaseInfo().getProductName());
        newRun.setStatus(BuildStatus.RUNNING);
        try {
            newRun.setHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        LOGGER.info(newRun.toString());
        Run remoteRun = cuteDbWsConsumer.createNewRun(newRun);
        LOGGER.info(remoteRun.toString());
        return remoteRun;
    }



    /**
     * Obtain linter configuration from a system property
     *
     * @return LinterConfigs
     * @throws SchemaCrawlerException
     */
    private static LinterConfigs readLinterConfigs(final LintOptions lintOptions, final Config config)
    {
        final LinterConfigs linterConfigs = new LinterConfigs(config);
        String linterConfigsFile = null;
        try
        {
            linterConfigsFile = lintOptions.getLinterConfigs();
            if (!isBlank(linterConfigsFile))
            {
                final Path linterConfigsFilePath = Paths.get(linterConfigsFile).toAbsolutePath();
                if (isRegularFile(linterConfigsFilePath) && isReadable(linterConfigsFilePath))
                {
                    linterConfigs.parse(newBufferedReader(linterConfigsFilePath));
                }
                else
                {
                    LOGGER.log(Level.WARNING, "Could not find linter configs file, " + linterConfigsFile);
                }
            }
            else
            {
                LOGGER.log(Level.CONFIG, "Using default linter configs");
            }

            return linterConfigs;
        }
        catch (final Exception e)
        {
            LOGGER.log(Level.WARNING, "Could not load linter configs from file, " + linterConfigsFile, e);
            return linterConfigs;
        }
    }

}
