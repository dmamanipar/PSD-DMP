package pe.edu.upeu.sysalmacen.repositorio;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.upeu.sysalmacen.config.MySQLTestContainer;
import pe.edu.upeu.sysalmacen.modelo.Cliente;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("tc")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de repositorio - IClienteRepository (MySQL real)")
class IClienteRepositoryTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      MySQLTestContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", MySQLTestContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", MySQLTestContainer.INSTANCE::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IVentaRepository ventaRepository;

    @Autowired
    private IVentaDetalleRepository ventaDetalleRepository;

    private Cliente clienteGuardado;

    @BeforeEach
    void setUp() {
        ventaDetalleRepository.deleteAll();
        ventaRepository.deleteAll();
        clienteRepository.deleteAll();

        clienteGuardado = clienteRepository.save(Cliente.builder()
                .dniruc("12345678")
                .nombres("Juan Pérez")
                .tipoDocumento("DNI")
                .direccion("Av. Lima 123")
                .build());
    }

    @Order(1)
    @DisplayName("Guardar cliente - debe persistir con el dniruc como PK")
    @Test
    void testGuardarCliente_PersisteCon_DnirucComoPK() {
        Cliente nuevo = Cliente.builder()
                .dniruc("87654321").nombres("María López")
                .tipoDocumento("DNI").build();

        Cliente guardado = clienteRepository.save(nuevo);

        assertEquals("87654321", guardado.getDniruc());
        assertEquals("María López", guardado.getNombres());
    }

    @Order(2)
    @DisplayName("Guardar cliente - verifica diferentes tipos de documento")
    @ParameterizedTest(name = "dniruc=''{0}'', tipoDoc=''{1}''")
    @CsvSource({"20123456789, RUC", "11223344, DNI", "55667788, DNI"})
    void testGuardarCliente_ConDiferentesDocumentos(String dniruc, String tipoDoc) {
        Cliente cli = clienteRepository.save(Cliente.builder()
                .dniruc(dniruc).nombres("Test Cliente")
                .tipoDocumento(tipoDoc).build());

        assertEquals(dniruc, cli.getDniruc());
        assertEquals(tipoDoc, cli.getTipoDocumento());
    }

    @Order(3)
    @DisplayName("Buscar cliente por dniruc - retorna cliente existente")
    @Test
    void testFindById_RetornaClienteExistente() {
        Optional<Cliente> resultado = clienteRepository.findById("12345678");

        assertTrue(resultado.isPresent());
        assertEquals("Juan Pérez", resultado.get().getNombres());
        assertEquals("DNI", resultado.get().getTipoDocumento());
    }

    @Order(4)
    @DisplayName("Buscar cliente por dniruc - retorna vacío cuando no existe")
    @ParameterizedTest(name = "dniruc inexistente=''{0}''")
    @ValueSource(strings = {"00000000", "99999999"})
    void testFindById_RetornaVacioCuandoNoExiste(String dnirucInexistente) {
        assertFalse(clienteRepository.findById(dnirucInexistente).isPresent());
    }

    @Order(5)
    @DisplayName("Listar clientes - retorna todos los clientes guardados")
    @Test
    void testFindAll_RetornasTodosLosClientes() {
        clienteRepository.save(Cliente.builder()
                .dniruc("20123456789").nombres("Empresa SAC")
                .tipoDocumento("RUC").build());

        List<Cliente> clientes = clienteRepository.findAll();

        Assertions.assertThat(clientes).hasSize(2);
        Assertions.assertThat(clientes).extracting(Cliente::getDniruc)
                .contains("12345678", "20123456789");
    }

    @Order(6)
    @DisplayName("Listar clientes - retorna lista vacía cuando no hay registros")
    @Test
    void testFindAll_ListaVaciaSinRegistros() {
        clienteRepository.deleteAll();
        Assertions.assertThat(clienteRepository.findAll()).isEmpty();
    }

    @Order(7)
    @DisplayName("Actualizar cliente - persiste los cambios")
    @Test
    void testActualizarCliente_PersisteCambios() {
        clienteGuardado.setNombres("Juan Actualizado");
        clienteGuardado.setDireccion("Jr. Nuevo 456");

        Cliente actualizado = clienteRepository.save(clienteGuardado);

        assertEquals("Juan Actualizado", actualizado.getNombres());
        assertEquals("Jr. Nuevo 456", actualizado.getDireccion());
        assertEquals("12345678", actualizado.getDniruc());
    }

    @Order(8)
    @DisplayName("Eliminar cliente - no debe existir después de eliminar")
    @Test
    void testEliminarCliente_NoPuedeEncontrarsePosteriormente() {
        clienteRepository.deleteById("12345678");
        assertFalse(clienteRepository.findById("12345678").isPresent());
    }

    @Order(9)
    @DisplayName("Eliminar cliente - el conteo debe decrementar en 1")
    @Test
    void testEliminarCliente_ReduceConteoTotal() {
        long totalAntes = clienteRepository.count();
        clienteRepository.deleteById("12345678");
        Assertions.assertThat(clienteRepository.count()).isEqualTo(totalAntes - 1);
    }

}
