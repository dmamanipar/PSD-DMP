package pe.edu.upeu.sysalmacen.repositorio;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.upeu.sysalmacen.config.MySQLTestContainer;
import pe.edu.upeu.sysalmacen.modelo.Marca;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de repositorio - IMarcaRepository con MySQL real (Testcontainers).
 *
 * Cambios respecto a la versión H2:
 *  - @AutoConfigureTestDatabase(Replace.NONE) → Spring NO reemplaza el
 *    datasource, usa el del contenedor MySQL inyectado via @DynamicPropertySource.
 *  - @Testcontainers → activa el ciclo de vida de los contenedores JUnit 5.
 *  - @ActiveProfiles("tc") → carga application-tc.properties (dialecto MySQL).
 *  - MySQLTestContainer.INSTANCE → Singleton compartido; el contenedor
 *    arranca una sola vez para todos los tests del proyecto.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("tc")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de repositorio - IMarcaRepository (MySQL real)")
class IMarcaRepositoryTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      MySQLTestContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", MySQLTestContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", MySQLTestContainer.INSTANCE::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private IMarcaRepository marcaRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IVentaDetalleRepository ventaDetalleRepository;

    private Marca marcaGuardada;

    @BeforeEach
    void setUp() {
        ventaDetalleRepository.deleteAll();
        productoRepository.deleteAll();
        marcaRepository.deleteAll();

        marcaGuardada = marcaRepository.save(
                Marca.builder().nombre("Samsung").build());
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("Guardar marca - debe persistir y asignar id generado")
    @Test
    void testGuardarMarca_PersisteMarcaConIdGenerado() {
        Marca nueva = Marca.builder().nombre("LG").build();

        Marca guardada = marcaRepository.save(nueva);

        assertNotNull(guardada.getIdMarca());
        assertEquals("LG", guardada.getNombre());
    }

    @Order(2)
    @DisplayName("Guardar marca - verifica diferentes nombres")
    @ParameterizedTest(name = "nombre=''{0}''")
    @ValueSource(strings = {"Sony", "Panasonic", "Philips"})
    void testGuardarMarca_ConDiferentesNombres(String nombre) {
        Marca m = marcaRepository.save(Marca.builder().nombre(nombre).build());

        Assertions.assertThat(m.getIdMarca()).isNotNull().isPositive();
        Assertions.assertThat(m.getNombre()).isEqualTo(nombre);
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Order(3)
    @DisplayName("Buscar marca por id - retorna marca existente")
    @Test
    void testFindById_RetornaMarcaExistente() {
        Optional<Marca> resultado = marcaRepository.findById(marcaGuardada.getIdMarca());

        assertTrue(resultado.isPresent());
        assertEquals("Samsung", resultado.get().getNombre());
    }

    @Order(4)
    @DisplayName("Buscar marca por id - retorna vacío cuando no existe")
    @Test
    void testFindById_RetornaVacioCuandoNoExiste() {
        assertFalse(marcaRepository.findById(9999L).isPresent());
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Order(5)
    @DisplayName("Listar marcas - retorna todas las marcas guardadas")
    @Test
    void testFindAll_RetornaTodasLasMarcas() {
        marcaRepository.save(Marca.builder().nombre("LG").build());
        marcaRepository.save(Marca.builder().nombre("Sony").build());

        List<Marca> marcas = marcaRepository.findAll();

        Assertions.assertThat(marcas).hasSize(3);
        Assertions.assertThat(marcas).extracting(Marca::getNombre)
                .contains("Samsung", "LG", "Sony");
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Order(6)
    @DisplayName("Actualizar marca - debe persistir el nuevo nombre")
    @Test
    void testActualizarMarca_PersisteCambios() {
        marcaGuardada.setNombre("Samsung Electronics");
        Marca actualizada = marcaRepository.save(marcaGuardada);

        assertEquals("Samsung Electronics", actualizada.getNombre());
        assertEquals(marcaGuardada.getIdMarca(), actualizada.getIdMarca());
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Order(7)
    @DisplayName("Eliminar marca por id - debe dejar de existir en BD")
    @Test
    void testEliminarMarca_NoPuedeEncontrarsePosteriormente() {
        Long id = marcaGuardada.getIdMarca();
        marcaRepository.deleteById(id);

        assertFalse(marcaRepository.findById(id).isPresent());
    }

    // ─── maxID (query nativa — motivo principal de usar MySQL real) ──────────

    @Order(8)
    @DisplayName("maxID (query nativa MySQL) - retorna el mayor id guardado")
    @Test
    void testMaxId_RetornaIdMaximoExistente() {
        marcaRepository.save(Marca.builder().nombre("LG").build());
        marcaRepository.save(Marca.builder().nombre("Sony").build());

        Optional<Long> maxId = marcaRepository.maxID();

        assertTrue(maxId.isPresent());
        Assertions.assertThat(maxId.get()).isGreaterThan(0L);
    }

    @Order(9)
    @DisplayName("maxID - el resultado coincide con el ultimo id insertado")
    @Test
    void testMaxId_CoinciadeConUltimoInsertado() {
        Marca ultima = marcaRepository.save(Marca.builder().nombre("Ultima").build());

        Optional<Long> maxId = marcaRepository.maxID();

        assertTrue(maxId.isPresent());
        assertEquals(ultima.getIdMarca(), maxId.get());
    }

}
