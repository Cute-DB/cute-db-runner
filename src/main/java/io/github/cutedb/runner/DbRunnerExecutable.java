package io.github.cutedb.runner;


import io.github.cutedb.runner.dto.BuildStatus;
import io.github.cutedb.runner.dto.Run;
import io.github.cutedb.runner.ws.CuteDbWsConsumer;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.executable.BaseStagedExecutable;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.Files.*;
import static sf.util.Utility.isBlank;

public class DbRunnerExecutable extends BaseStagedExecutable
{

  private static final Logger LOGGER = Logger.getLogger(DbRunnerExecutable.class.getName());

  static final String COMMAND = "cutedbrunner";

  CuteDbWsConsumer cuteDbWsConsumer;

  protected DbRunnerExecutable()
  {
    super(COMMAND);
      cuteDbWsConsumer = new CuteDbWsConsumer();
  }

  @Override
  public void executeOn(final Catalog catalog, final Connection connection) throws Exception
  {

    final LintedCatalog lintedCatalog = createLintedCatalog(catalog, connection);
    sendDataToServer(lintedCatalog);

  }

  private LintedCatalog createLintedCatalog(final Catalog catalog, final Connection connection) throws SchemaCrawlerException
  {
    final LintOptions lintOptions = new LintOptionsBuilder().fromConfig(additionalConfiguration).toOptions();

    final LinterConfigs linterConfigs = readLinterConfigs(lintOptions, getAdditionalConfiguration());
    final Linters linters = new Linters(linterConfigs);

    final LintedCatalog lintedCatalog = new LintedCatalog(catalog, connection, linters);
    return lintedCatalog;
  }




  private void sendDataToServer(final LintedCatalog lintedCatalog) throws IOException
  {
      initiateANewRun(lintedCatalog);
      LOGGER.info(getAdditionalConfiguration().toString());



  }

    private Run initiateANewRun(final LintedCatalog lintedCatalog){
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
        return newRun;
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

//  LintResult generateLintsResult(LintedCatalog lintedCatalog){
//    LOGGER.log(Level.INFO, "Start generateLintsResult");
//    if (lintedCatalog == null) {
//      LOGGER.log(Level.INFO, "lintedCatalog = null");
//      return null;
//    }
//
//    DbLintResult result = new DbLintResult();
//
//    Stream<Lint<?>> lints = StreamSupport.stream(lintedCatalog.getCollector().spliterator(),  false);
//
//    // Count critical hits
//    Stream<Lint<?>> hits = lints.filter(l -> l.getSeverity() == critical);
//    result.setNbCriticalHit(((Long)hits.count()).intValue());
//
//    // Count high hits
//    lints = StreamSupport.stream(lintedCatalog.getCollector().spliterator(),  false);
//    hits = lints.filter(l -> l.getSeverity() == high);
//    result.setNbHighHit(((Long)hits.count()).intValue());
//
//    // Count medium hits
//    lints = StreamSupport.stream(lintedCatalog.getCollector().spliterator(),  false);
//    hits = lints.filter(l -> l.getSeverity() == medium);
//    result.setNbMediumHit(((Long)hits.count()).intValue());
//
//    // Count low hits
//    lints = StreamSupport.stream(lintedCatalog.getCollector().spliterator(),  false);
//    hits = lints.filter(l -> l.getSeverity() == low);
//    result.setNbLowHit(((Long)hits.count()).intValue());
//
//    result.setJsonStringHits("[{'Severity': 'Critical', Hit: "+result.getNbCriticalHit()+"}, " +
//            "{'Severity': 'High', Hit: "+result.getNbHighHit()+"}," +
//            "{'Severity': 'Medium', Hit: "+result.getNbMediumHit()+"}" +
//            "{'Severity': 'Low', Hit: "+result.getNbLowHit()+"}]");
//
//    if(result.getNbCriticalHit() > 0){
//      result.setGlobalScore(0);
//    }
//    else if(result.getNbHighHit() > 0){
//      result.setGlobalScore(1);
//    }
//    else if(result.getNbMediumHit() > 0){
//      result.setGlobalScore(2);
//    }
//    else if(result.getNbLowHit() > 0) {
//      result.setGlobalScore(3);
//    }
//    else{
//      result.setGlobalScore(4);
//    }
//    generateTreeMapData(lintedCatalog);
//    LOGGER.log(Level.INFO, "End generateLintsResult");
//    return result;
//
//  }


//  String generateTreeMapData(LintedCatalog lintedCatalog){
//    Stream<Lint<?>> lints = StreamSupport.stream(lintedCatalog.getCollector().spliterator(),  false);
//
//    List<String> tableList = new ArrayList<>();
//    lints.forEach(l -> {
//      if(!tableList.contains(l.getObjectName())) {
//        tableList.add(l.getObjectName());
//      }
//    });
//
//
//
//    List<TableLintResult> tableLintResults = new ArrayList<>();
//    tableList.forEach(t -> {
//
//      TableLintResult tableLintResult = new TableLintResult();
//      tableLintResult.setTableName(t);
//
//      Stream<Lint<?>> lintList = StreamSupport.stream(lintedCatalog.getCollector().spliterator(),  false);
//      Stream<Lint<?>> tableLints = lintList.filter(l -> l.getObjectName().equals(t));
//      tableLintResult.setNbCriticalHit(((Long)tableLints.filter(h -> h.getSeverity() == LintSeverity.critical).count()).intValue());
//
//      lintList = StreamSupport.stream(lintedCatalog.getCollector().spliterator(),  false);
//      tableLints = lintList.filter(l -> l.getObjectName().equals(t));
//      tableLintResult.setNbHighHit(((Long)tableLints.filter(h -> h.getSeverity() == LintSeverity.high).count()).intValue());
//
//      lintList = StreamSupport.stream(lintedCatalog.getCollector().spliterator(),  false);
//      tableLints = lintList.filter(l -> l.getObjectName().equals(t));
//      tableLintResult.setNbMediumHit(((Long)tableLints.filter(h -> h.getSeverity() == LintSeverity.medium).count()).intValue());
//
//      lintList = StreamSupport.stream(lintedCatalog.getCollector().spliterator(),  false);
//      tableLints = lintList.filter(l -> l.getObjectName().equals(t));
//      tableLintResult.setNbLowHit((((Long)tableLints.filter(h -> h.getSeverity() == LintSeverity.low).count()).intValue());
//
//      tableLintResults.add(tableLintResult);
//
//    });
//
//    return "";
//  }
}
