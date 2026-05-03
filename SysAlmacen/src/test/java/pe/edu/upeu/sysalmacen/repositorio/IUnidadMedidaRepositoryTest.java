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
import pe.edu.upeu.sysalmacen.modelo.UnidadMedida;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("tc")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de repositorio - IUnidadMedidaRepository (MySQL real)")
class IUnidadMedidaRepositoryTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      MySQLTestContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", MySQLTestContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", MySQLTestContainer.INSTANCE::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private IUnidadMedidaRepository unidadMedidaRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IVentaDetalleRepository ventaDetalleRepository;

    private UnidadMedida unidadGuardada;

    @BeforeEach
    void setUp() {
        ventaDetalleRepository.deleteAll();
        productoRepository.deleteAll();
        unidadMedidaRepository.deleteAll();
        unidadGuardada = unidadMedidaRepository.save(
                UnidadMedida.builder().nombreMedida("Kilogramo").build());
    }

    @Order(1)
    @DisplayName("Guardar unidad de medida - debe persistir con id generado")
    @Test
    void testGuardarUnidadMedida_PersisteCon_IdGenerado() {
        UnidadMedida nueva = UnidadMedida.builder().nombreMedida("Litro").build();

        UnidadMedida guardada = unidadMedidaRepository.save(nueva);

        assertNotNull(guardada.getIdUnidad());
        assertEquals("Litro", guardada.getNombreMedida());
    }

    @Order(2)
    @DisplayName("Guardar unidad de medida - verifica diferentes nombres")
    @ParameterizedTest(name = "nombre=''{0}''")
    @ValueSource(strings = {"Metro", "Litro", "Gramo", "Unidad", "Docena"})
    void testGuardarUnidadMedida_ConDiferentesNombres(String nombre) {
        UnidadMedida u = unidadMedidaRepository.save(
                UnidadMedida.builder().nombreMedida(nombre).build());

        Assertions.assertThat(u.getIdUnidad()).isNotNull().isPositive();
        Assertions.assertThat(u.getNombreMedida()).isEqualTo(nombre);
    }

    @Order(3)
    @DisplayName("Buscar unidad de medida por id - retorna unidad existente")
    @Test
    void testFindById_RetornaUnidadExistente() {
        Optional<UnidadMedida> resultado = unidadMedidaRepository.findById(unidadGuardada.getIdUnidad());

        assertTrue(resultado.isPresent());
        assertEquals("Kilogramo", resultado.get().getNombreMedida());
    }

    @Order(4)
    @DisplayName("Buscar unidad de medida por id - retorna vacío cuando no existe")
    @Test
    void testFindById_RetornaVacioCuandoNoExiste() {
        assertFalse(unidadMedidaRepository.findById(9999L).isPresent());
    }

    @Order(5)
    @DisplayName("Listar unidades de medida - retorna todas las guardadas")
    @Test
    void testFindAll_RetornaTodasLasUnidades() {
        unidadMedidaRepository.save(UnidadMedida.builder().nombreMedida("Litro").build());
        unidadMedidaRepository.save(UnidadMedida.builder().nombreMedida("Metro").build());

        List<UnidadMedida> unidades = unidadMedidaRepository.findAll();

        Assertions.assertThat(unidades).hasSize(3);
        Assertions.assertThat(unidades).extracting(UnidadMedida::getNombreMedida)
                .contains("Kilogramo", "Litro", "Metro");
    }

    @Order(6)
    @DisplayName("Listar unidades de medida - retorna lista vacía sin registros")
    @Test
    void testFindAll_ListaVaciaSinRegistros() {
        unidadMedidaRepository.deleteAll();
        Assertions.assertThat(unidadMedidaRepository.findAll()).isEmpty();
    }

    @Order(7)
    @DisplayName("Actualizar unidad de medida - persiste el nuevo nombre")
    @Test
    void testActualizarUnidadMedida_PersisteCambios() {
        unidadGuardada.setNombreMedida("Kilogramo Actualizado");
        UnidadMedida actualizada = unidadMedidaRepository.save(unidadGuardada);

        assertEquals("Kilogramo Actualizado", actualizada.getNombreMedida());
        assertEquals(unidadGuardada.getIdUnidad(), actualizada.getIdUnidad());
    }

    @Order(8)
    @DisplayName("Eliminar unidad de medida - no debe existir después de eliminar")
    @Test
    void testEliminarUnidadMedida_NoPuedeEncontrarsePosteriormente() {
        Long id = unidadGuardada.getIdUnidad();
        unidadMedidaRepository.deleteById(id);
        assertFalse(unidadMedidaRepository.findById(id).isPresent());
    }

    @Order(9)
    @DisplayName("Eliminar unidad de medida - el conteo debe decrementar en 1")
    @Test
    void testEliminarUnidadMedida_ReduceConteoTotal() {
        unidadMedidaRepository.save(UnidadMedida.builder().nombreMedida("Extra").build());
        long totalAntes = unidadMedidaRepository.count();

        unidadMedidaRepository.deleteById(unidadGuardada.getIdUnidad());

        Assertions.assertThat(unidadMedidaRepository.count()).isEqualTo(totalAntes - 1);
    }


}
