package pe.edu.upeu.sysalmacen.config;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import java.util.List;

/**
 * Contenedor MySQL compartido como Singleton para TODOS los tests.
 *
 * ¿Por qué Singleton?
 * Si cada clase de test arrancara su propio contenedor, el tiempo total
 * de ejecución se multiplicaría (cada contenedor tarda ~8-15s en iniciar).
 * Con el patrón Singleton el contenedor arranca UNA sola vez y todos los
 * tests lo comparten, haciendo la suite igual de rápida que con H2.
 *
 * ¿Cómo funciona withReuse(true)?
 * Testcontainers escribe un hash del contenedor en ~/.testcontainers.properties.
 * Si el contenedor ya está corriendo con ese hash, lo reutiliza en lugar
 * de crear uno nuevo. Esto acelera las ejecuciones repetidas en desarrollo.
 * Para que .withReuse(true) funcione hay que habilitar reutilización
 * agregando en ~/.testcontainers.properties:
 *   testcontainers.reuse.enable=true
 *
 * Para Oracle: cambiar MySQLContainer por OracleContainer y la imagen
 * a "gvenzl/oracle-xe:21-slim". Ver comentario al final del archivo.
 */
public final class MySQLTestContainer {

    public static final MySQLContainer<?> INSTANCE;

    static {
        INSTANCE = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("sysalmacen_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        INSTANCE.setPortBindings(List.of("3307:3306"));
        INSTANCE.start();
    }

    private MySQLTestContainer() {
    }

    /*
     * ── Para Oracle XE ─────────────────────────────────────────────────────
     * 1. Cambiar dependencias en pom.xml (ver comentario en pom.xml)
     * 2. Reemplazar el bloque static de arriba por:
     *
     *    import org.testcontainers.oracle.OracleContainer;
     *    public static final OracleContainer INSTANCE;
     *    static {
     *        INSTANCE = new OracleContainer(
     *                DockerImageName.parse("gvenzl/oracle-xe:21-slim"))
     *                .withDatabaseName("sysalmacen_test")
     *                .withUsername("test")
     *                .withPassword("test")
     *                .withReuse(true);
     *        INSTANCE.start();
     *    }
     *
     * 3. En los tests cambiar MySQL8Dialect por OracleDialect
     * ──────────────────────────────────────────────────────────────────────
     */
}
