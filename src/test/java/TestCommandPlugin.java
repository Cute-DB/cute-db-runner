
import io.github.cutedb.runner.DbRunnerExecutable;
import org.junit.Test;
import schemacrawler.tools.executable.CommandRegistry;

import static org.junit.Assert.assertTrue;

public class TestCommandPlugin
{

  @Test
  public void testCommandPlugin()
    throws Exception
  {
    final CommandRegistry registry = new CommandRegistry();
    assertTrue(registry.hasCommand(DbRunnerExecutable.COMMAND));
  }

}
