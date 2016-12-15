import io.github.cutedb.runner.DbRunnerExecutable;
import io.github.cutedb.runner.utils.H2SqlDatabase;
import org.junit.Before;
import org.junit.Test;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.tools.executable.Executable;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.lint.executable.LintOptionsBuilder;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.TextOutputFormat;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by barmi83 on 29/07/16.
 */
public class BaseCuteDbRunnerTest {

    protected static H2SqlDatabase database;

    @Before
    public void init() throws Exception {
        database = new H2SqlDatabase();
        database.setUp("test");
    }

    @Test
    public void executeSampleData() throws Exception {
        execute("runnerTest", "sa", "");
    }


    private void execute(String databaseName, String user, String pwd)  throws Exception{
        final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
        options.setSchemaInfoLevel(SchemaInfoLevelBuilder.standard());

        Connection connection = DriverManager.getConnection(H2SqlDatabase.CONNECTION_STRING, H2SqlDatabase.DBA_USER_NAME, H2SqlDatabase.DBA_PASSWORD);

        final Executable executable = new SchemaCrawlerExecutable(DbRunnerExecutable.COMMAND);

        //final Path linterConfigsFile = FileSystems.getDefault().getPath("", BaseCuteDbRunnerTest.class.getResource("schemacrawler-linter-configs-test.xml").getPath());
        final LintOptionsBuilder optionsBuilder = new LintOptionsBuilder();
        //optionsBuilder.withLinterConfigs(linterConfigsFile.toString());
        executable.setAdditionalConfiguration(optionsBuilder.toConfig());
        executable.getAdditionalConfiguration().put("server", "h2");
        executable.getAdditionalConfiguration().put("host", "localhost");
        executable.getAdditionalConfiguration().put("database", "runnerTest");
        executable.getAdditionalConfiguration().put("user", "sa");
        executable.getAdditionalConfiguration().put("password", "sa");
        executable.getAdditionalConfiguration().put("schema", "public");
        executable.getAdditionalConfiguration().put("cutedbserver","localhost");
        executable.getAdditionalConfiguration().put("outputfile","test_lints.html");
        executable.getAdditionalConfiguration().put("outputformat","html");

        Path here = Paths.get("").toAbsolutePath();
        executable.getAdditionalConfiguration().put("linterconfigs", "src/test/resources/schemacrawler-linter-configs-test.xml");

        Path out = Paths.get("target/test_"+this.getClass().getSimpleName()+".json");
        OutputOptions outputOptions = new OutputOptions(TextOutputFormat.json, out);
        outputOptions.setOutputFile(Paths.get("target/test_"+this.getClass().getSimpleName()+".json"));

        executable.setOutputOptions(outputOptions);
        executable.setSchemaCrawlerOptions(options);
        executable.execute(connection);
    }

}
