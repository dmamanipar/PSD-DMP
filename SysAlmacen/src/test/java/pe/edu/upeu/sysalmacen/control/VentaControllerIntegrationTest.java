package pe.edu.upeu.sysalmacen.control;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import pe.edu.upeu.sysalmacen.dtos.VentaDTO;
import pe.edu.upeu.sysalmacen.modelo.*;
import pe.edu.upeu.sysalmacen.repositorio.*;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - VentaController")
class VentaControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ICategoriaRepository categoriaRepository;
    @Autowired
    private IMarcaRepository marcaRepository;
    @Autowired
    private IUnidadMedidaRepository unidadMedidaRepository;
    @Autowired
    private IProductoRepository productoRepository;
    @Autowired
    private IClienteRepository clienteRepository;
    @Autowired
    private IVentCarritoRepository ventCarritoRepository;
    @Autowired
    private IUsuarioRepository usuarioRepository;
    @Autowired
    private IVentaRepository ventaRepository;

    private String token;
    private Usuario usuario;
    private Cliente cliente;
    private Producto producto;

    @BeforeAll
    void setup() throws Exception {
        token = obtenerTokenJwt();
        
        // Obtener el usuario autenticado
        usuario = usuarioRepository.findOneByUser(USER_TEST)
                .orElseThrow(() -> new RuntimeException("Usuario de prueba no encontrado"));

        cliente = clienteRepository.save(Cliente.builder()
                .dniruc("77777777")
                .nombres("Cliente de Prueba")
                .tipoDocumento("DNI")
                .build());

        Categoria cat = categoriaRepository.save(Categoria.builder().nombre("General").build());
        Marca mar = marcaRepository.save(Marca.builder().nombre("Genérica").build());
        UnidadMedida uni = unidadMedidaRepository.save(UnidadMedida.builder().nombreMedida("Unidad").build());

        producto = productoRepository.save(Producto.builder()
                .nombre("Producto de Prueba")
                .pu(10.0)
                .puOld(10.0)
                .utilidad(2.0)
                .stock(100.0)
                .stockOld(100.0)
                .categoria(cat)
                .marca(mar)
                .unidadMedida(uni)
                .build());
    }

    @Order(1)
    @DisplayName("POST /ventas - procesa una venta desde el carrito")
    @Test
    void testProcesarVenta_RetornaCreated() throws Exception {
        // 1. Agregar item al carrito manualmente
        ventCarritoRepository.save(VentCarrito.builder()
                .dniruc(cliente.getDniruc())
                .producto(producto)
                .nombreProducto(producto.getNombre())
                .cantidad(2.0)
                .punitario(producto.getPu())
                .ptotal(20.0)
                .estado(1)
                .usuario(usuario)
                .build());

        // 2. Preparar DTO de Venta
        VentaDTO.VentaCADTO dto = new VentaDTO.VentaCADTO(
                null, 20.0, 3.6, 23.6, cliente.getDniruc(), usuario.getIdUsuario(),
                "000001", LocalDateTime.now(), "V001", "BOLETA");

        // 3. Ejecutar POST
        mockMvc.perform(post("/ventas")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // 4. Verificar que el carrito esté vacío
        Assertions.assertTrue(ventCarritoRepository.listaCarritoCliente(cliente.getDniruc()).isEmpty());
    }

    @Order(2)
    @DisplayName("GET /ventas - retorna lista de ventas")
    @Test
    void testListarVentas_RetornaOk() throws Exception {
        mockMvc.perform(get("/ventas")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}
