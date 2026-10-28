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
        String url = System.getenv("JOOQ_JDBC_URL");
        if (url == null || url.isBlank()) {
            generateWithContainer(projectDir, outputDir);
            return;
        }
        apply(projectDir, outputDir, url, System.getenv("JOOQ_JDBC_USER"), System.getenv("JOOQ_JDBC_PASSWORD"));
    }

    private static void generateWithContainer(File projectDir, File outputDir) throws Exception {
        PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");
        postgres.start();
        try {
            apply(projectDir, outputDir, postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        } finally {
            postgres.stop();
        }
    }

    private static void apply(File projectDir, File outputDir, String url, String user, String password) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            var database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.yaml",
                    new DirectoryResourceAccessor(new File(projectDir, "src/main/resources")),
                    database)) {
                liquibase.update();
            }
        }
        GenerationTool.generate(configuration(outputDir, url, user, password));
    }

    private static Configuration configuration(File outputDir, String url, String user, String password) {
        return new Configuration()
                .withLogging(Logging.WARN)
                .withJdbc(new Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(url)
                        .withUser(user)
                        .withPassword(password))
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
