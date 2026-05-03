package pe.edu.upeu.sysalmacen.repositorio;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.upeu.sysalmacen.config.MySQLTestContainer;
import pe.edu.upeu.sysalmacen.modelo.Categoria;
import pe.edu.upeu.sysalmacen.modelo.Marca;
import pe.edu.upeu.sysalmacen.modelo.Producto;
import pe.edu.upeu.sysalmacen.modelo.UnidadMedida;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("tc")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de repositorio - IProductoRepository (MySQL real)")
class IProductoRepositoryTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      MySQLTestContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", MySQLTestContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", MySQLTestContainer.INSTANCE::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IVentaDetalleRepository ventaDetalleRepository;

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private IMarcaRepository marcaRepository;

    @Autowired
    private IUnidadMedidaRepository unidadMedidaRepository;

    private Categoria categoria;
    private Marca marca;
    private UnidadMedida unidadMedida;
    private Producto productoGuardado;

    @BeforeEach
    void setUp() {
        ventaDetalleRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
        marcaRepository.deleteAll();
        unidadMedidaRepository.deleteAll();

        categoria = categoriaRepository.save(Categoria.builder().nombre("Electrónica").build());
        marca = marcaRepository.save(Marca.builder().nombre("Sony").build());
        unidadMedida = unidadMedidaRepository.save(UnidadMedida.builder().nombreMedida("Unidad").build());

        productoGuardado = productoRepository.save(Producto.builder()
                .nombre("Audífonos Bluetooth")
                .pu(150.0)
                .puOld(140.0)
                .utilidad(10.0)
                .stock(50.0)
                .stockOld(45.0)
                .categoria(categoria)
                .marca(marca)
                .unidadMedida(unidadMedida)
                .build());
    }

    @Order(1)
    @DisplayName("Guardar producto - debe persistir y asignar id generado")
    @Test
    void testGuardarProducto_PersisteProductoConIdGenerado() {
        Producto nuevo = Producto.builder()
                .nombre("Cámara DSLR")
                .pu(1200.0)
                .puOld(1150.0)
                .utilidad(50.0)
                .stock(10.0)
                .stockOld(5.0)
                .categoria(categoria)
                .marca(marca)
                .unidadMedida(unidadMedida)
                .build();

        Producto guardado = productoRepository.save(nuevo);

        assertNotNull(guardado.getIdProducto());
        assertEquals("Cámara DSLR", guardado.getNombre());
    }

    @Order(2)
    @DisplayName("Buscar producto por id - retorna producto existente")
    @Test
    void testFindById_RetornaProductoExistente() {
        Optional<Producto> resultado = productoRepository.findById(productoGuardado.getIdProducto());

        assertTrue(resultado.isPresent());
        assertEquals("Audífonos Bluetooth", resultado.get().getNombre());
    }

    @Order(3)
    @DisplayName("Listar productos - retorna todos los productos guardados")
    @Test
    void testFindAll_RetornaTodosLosProductos() {
        productoRepository.save(Producto.builder()
                .nombre("Mouse Gamer")
                .pu(50.0)
                .puOld(45.0)
                .utilidad(5.0)
                .stock(100.0)
                .stockOld(80.0)
                .categoria(categoria)
                .marca(marca)
                .unidadMedida(unidadMedida)
                .build());

        List<Producto> productos = productoRepository.findAll();

        Assertions.assertThat(productos).hasSize(2);
        Assertions.assertThat(productos).extracting(Producto::getNombre)
                .contains("Audífonos Bluetooth", "Mouse Gamer");
    }

    @Order(4)
    @DisplayName("Actualizar producto - debe persistir los cambios")
    @Test
    void testActualizarProducto_PersisteCambios() {
        productoGuardado.setNombre("Audífonos Sony WH-1000XM4");
        productoGuardado.setPu(160.0);
        Producto actualizado = productoRepository.save(productoGuardado);

        assertEquals("Audífonos Sony WH-1000XM4", actualizado.getNombre());
        assertEquals(160.0, actualizado.getPu());
    }

    @Order(5)
    @DisplayName("Eliminar producto - no debe existir después de eliminar")
    @Test
    void testEliminarProducto_NoPuedeEncontrarsePosteriormente() {
        Long id = productoGuardado.getIdProducto();
        productoRepository.deleteById(id);

        assertFalse(productoRepository.findById(id).isPresent());
    }


}
