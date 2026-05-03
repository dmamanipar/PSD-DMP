package pe.edu.upeu.sysalmacen.repositorio;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.upeu.sysalmacen.config.MySQLTestContainer;
import pe.edu.upeu.sysalmacen.modelo.Cliente;
import pe.edu.upeu.sysalmacen.modelo.Usuario;
import pe.edu.upeu.sysalmacen.modelo.Venta;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("tc")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de repositorio - IVentaRepository (MySQL real)")
class IVentaRepositoryTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      MySQLTestContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", MySQLTestContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", MySQLTestContainer.INSTANCE::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private IVentaRepository ventaRepository;

    @Autowired
    private IVentaDetalleRepository ventaDetalleRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IUsuarioRolRepository usuarioRolRepository;

    private Cliente cliente;
    private Usuario usuario;
    private Venta ventaGuardada;

    @BeforeEach
    void setUp() {
        ventaDetalleRepository.deleteAll();
        ventaRepository.deleteAll();
        usuarioRolRepository.deleteAll();
        usuarioRepository.deleteAll();
        clienteRepository.deleteAll();

        cliente = clienteRepository.save(Cliente.builder()
                .dniruc("77777777")
                .nombres("Juan Perez")
                .tipoDocumento("DNI")
                .build());

        usuario = usuarioRepository.save(Usuario.builder()
                .user("vendedor1")
                .clave("123456")
                .estado("Activo")
                .build());

        ventaGuardada = ventaRepository.save(Venta.builder()
                .precioBase(100.0)
                .igv(18.0)
                .precioTotal(118.0)
                .cliente(cliente)
                .usuario(usuario)
                .numDoc("000001")
                .serie("V001")
                .tipoDoc("BOLETA")
                .fechaGener(LocalDateTime.now())
                .build());
    }

    @Order(1)
    @DisplayName("Guardar venta - debe persistir y asignar id generado")
    @Test
    void testGuardarVenta_PersisteVentaConIdGenerado() {
        Venta nueva = Venta.builder()
                .precioBase(200.0)
                .igv(36.0)
                .precioTotal(236.0)
                .cliente(cliente)
                .usuario(usuario)
                .numDoc("000002")
                .serie("V001")
                .tipoDoc("BOLETA")
                .fechaGener(LocalDateTime.now())
                .build();

        Venta guardada = ventaRepository.save(nueva);

        assertNotNull(guardada.getIdVenta());
        assertEquals("000002", guardada.getNumDoc());
    }

    @Order(2)
    @DisplayName("Buscar venta por id - retorna venta existente")
    @Test
    void testFindById_RetornaVentaExistente() {
        Optional<Venta> resultado = ventaRepository.findById(ventaGuardada.getIdVenta());

        assertTrue(resultado.isPresent());
        assertEquals("000001", resultado.get().getNumDoc());
    }

    @Order(3)
    @DisplayName("Actualizar venta - debe persistir los cambios")
    @Test
    void testActualizarVenta_PersisteCambios() {
        ventaGuardada.setNumDoc("000001-ACT");
        Venta actualizada = ventaRepository.save(ventaGuardada);

        assertEquals("000001-ACT", actualizada.getNumDoc());
    }

    @Order(4)
    @DisplayName("Eliminar venta - no debe existir después de eliminar")
    @Test
    void testEliminarVenta_NoPuedeEncontrarsePosteriormente() {
        Long id = ventaGuardada.getIdVenta();
        ventaRepository.deleteById(id);

        assertFalse(ventaRepository.findById(id).isPresent());
    }

}
