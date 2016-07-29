package io.github.cutedb.runner;


import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.executable.CommandProvider;
import schemacrawler.tools.executable.Executable;
import schemacrawler.tools.options.OutputOptions;

public class DbRunnerCommandProvider implements CommandProvider
{

  @Override
  public Executable configureNewExecutable(final SchemaCrawlerOptions schemaCrawlerOptions, final OutputOptions outputOptions)
  {
    final DbRunnerExecutable executable = new DbRunnerExecutable();
    if (schemaCrawlerOptions != null)
    {
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
    }
    if (outputOptions != null)
    {
      executable.setOutputOptions(outputOptions);
    }
    return executable;
  }

  @Override
  public String getCommand()
  {
    return DbRunnerExecutable.COMMAND;
  }

  @Override
  public String getHelpResource()
  {
    return "/help/DbRunnerCommandProvider.txt";
  }

}
