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
    public void execute() throws Exception {


        final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
        options.setSchemaInfoLevel(SchemaInfoLevelBuilder.standard());

        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/nppb", "nppb_adm", "changeme");

        final Executable executable = new SchemaCrawlerExecutable("cutedbrunner");

        final Path linterConfigsFile = FileSystems.getDefault().getPath("", BaseCuteDbRunnerTest.class.getResource("schemacrawler-linter-configs-test.xml").getPath());
        final LintOptionsBuilder optionsBuilder = new LintOptionsBuilder();
        optionsBuilder.withLinterConfigs(linterConfigsFile.toString());
        executable.setAdditionalConfiguration(optionsBuilder.toConfig());

        Path out = Paths.get("target/test_"+this.getClass().getSimpleName()+".json");
        OutputOptions outputOptions = new OutputOptions(TextOutputFormat.json, out);
        outputOptions.setOutputFile(Paths.get("target/test_"+this.getClass().getSimpleName()+".json"));

        executable.setOutputOptions(outputOptions);
        executable.setSchemaCrawlerOptions(options);
        //TODO catch execption to send error to server
        executable.execute(connection);

    }
}
