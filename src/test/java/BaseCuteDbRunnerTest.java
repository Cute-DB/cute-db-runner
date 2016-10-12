import io.github.cutedb.runner.DbRunnerExecutable;
import org.junit.Test;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.tools.executable.Executable;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.lint.executable.LintOptionsBuilder;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.TextOutputFormat;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by barmi83 on 29/07/16.
 */
public class BaseCuteDbRunnerTest {

    @Test
    public void executeNppb() throws Exception {


        execute("nppb", "nppb_adm", "changeme");

    }

    @Test
    public void executeDpmmc() throws Exception {
        execute("dpmmc", "dpmmc_adm", "changeme");
    }

    private void execute(String databaseName, String user, String pwd) throws Exception {
        final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
        options.setSchemaInfoLevel(SchemaInfoLevelBuilder.standard());

        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+databaseName, user, pwd);

        final Executable executable = new SchemaCrawlerExecutable("cutedbrunner");

        final Path linterConfigsFile = FileSystems.getDefault().getPath("", BaseCuteDbRunnerTest.class.getResource("schemacrawler-linter-configs-test.xml").getPath());
        final LintOptionsBuilder optionsBuilder = new LintOptionsBuilder();
        optionsBuilder.withLinterConfigs(linterConfigsFile.toString());
        executable.setAdditionalConfiguration(optionsBuilder.toConfig());
        executable.getAdditionalConfiguration().put(DbRunnerExecutable.CUTEDB_SERVER_PARAMETER, "http://localhost:9000/");

        Path out = Paths.get("target/test_"+this.getClass().getSimpleName()+".json");
        OutputOptions outputOptions = new OutputOptions(TextOutputFormat.json, out);
        outputOptions.setOutputFile(Paths.get("target/test_"+this.getClass().getSimpleName()+".json"));

        executable.setOutputOptions(outputOptions);
        executable.setSchemaCrawlerOptions(options);
        //TODO catch execption to send error to server
        executable.execute(connection);
    }

}
