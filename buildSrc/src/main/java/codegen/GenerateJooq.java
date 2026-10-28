package codegen;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Logging;
import org.jooq.meta.jaxb.Target;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Comparator;
import java.util.stream.Stream;

public final class GenerateJooq {

    private GenerateJooq() {
    }

    public static void run(File projectDir, File outputDir) throws Exception {
        delete(outputDir.toPath());
        Files.createDirectories(outputDir.toPath());
        PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");
        postgres.start();
        try {
            migrate(projectDir, postgres);
            generate(postgres, outputDir);
        } finally {
            postgres.stop();
        }
    }

    private static void migrate(File projectDir, PostgreSQLContainer postgres) throws Exception {
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            var database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            File resources = new File(projectDir, "src/main/resources");
            try (Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.sql",
                    new DirectoryResourceAccessor(resources),
                    database)) {
                liquibase.update();
            }
        }
    }

    private static void generate(PostgreSQLContainer postgres, File outputDir) throws Exception {
        Configuration configuration = new Configuration()
                .withLogging(Logging.WARN)
                .withJdbc(new Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(postgres.getJdbcUrl())
                        .withUser(postgres.getUsername())
                        .withPassword(postgres.getPassword()))
                .withGenerator(new Generator()
                        .withDatabase(new org.jooq.meta.jaxb.Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withInputSchema("public")
                                .withExcludes("databasechangelog|databasechangeloglock")
                                .withOutputSchemaToDefault(true))
                        .withGenerate(new Generate()
                                .withDeprecated(false)
                                .withRecords(true)
                                .withPojos(false)
                                .withDaos(false)
                                .withFluentSetters(true))
                        .withTarget(new Target()
                                .withPackageName("com.aichat.aichatdemo2.jooq")
                                .withDirectory(outputDir.getAbsolutePath())));
        GenerationTool.generate(configuration);
    }

    private static void delete(Path root) throws Exception {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
