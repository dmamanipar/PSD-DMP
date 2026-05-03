package pe.edu.upeu.sysalmacen.servicio;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.sysalmacen.dtos.VentaDTO;
import pe.edu.upeu.sysalmacen.mappers.VentaMapper;
import pe.edu.upeu.sysalmacen.modelo.*;
import pe.edu.upeu.sysalmacen.repositorio.*;
import pe.edu.upeu.sysalmacen.servicio.impl.VentaServiceImp;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas unitarias - VentaService")
class VentaServiceTest {

    @Mock
    private IVentaRepository repo;
    @Mock
    private IClienteRepository clienteRepository;
    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IVentCarritoRepository ventCarritoRepository;
    @Mock
    private IProductoRepository productoRepository;
    @Mock
    private IVentaDetalleRepository ventaDetalleRepository;
    @Mock
    private VentaMapper ventaMapper;

    @InjectMocks
    private VentaServiceImp ventaService;

    private Venta venta;
    private Cliente cliente;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder().dniruc("77777777").nombres("Juan Perez").build();
        usuario = Usuario.builder().idUsuario(1L).user("admin").build();
        venta = Venta.builder()
                .idVenta(1L)
                .cliente(cliente)
                .usuario(usuario)
                .numDoc("000001")
                .build();
    }

    @Order(1)
    @DisplayName("Listar ventas - debe retornar lista completa")
    @Test
    void testFindAll_RetornaLista() {
        given(repo.findAll()).willReturn(List.of(venta));

        List<Venta> resultado = ventaService.findAll();

        Assertions.assertThat(resultado).hasSize(1);
    }

    @Order(2)
    @DisplayName("Guardar venta DTO - debe procesar venta y detalles")
    @Test
    void testSaveD_ProcesaVentaYDetalles() {
        VentaDTO.VentaCADTO caDto = new VentaDTO.VentaCADTO(
                null, 100.0, 18.0, 118.0, "77777777", 1L, "000001", null, "V001", "BOLETA");
        
        Producto producto = Producto.builder().idProducto(1L).nombre("Producto 1").build();
        VentCarrito carItem = VentCarrito.builder()
                .producto(producto)
                .cantidad(2.0)
                .punitario(50.0)
                .ptotal(100.0)
                .build();

        given(ventaMapper.toEntityFromCADTO(caDto)).willReturn(venta);
        given(clienteRepository.findById("77777777")).willReturn(Optional.of(cliente));
        given(usuarioRepository.findById(1L)).willReturn(Optional.of(usuario));
        given(repo.save(venta)).willReturn(venta);
        given(ventCarritoRepository.listaCarritoCliente("77777777")).willReturn(List.of(carItem));
        given(productoRepository.findById(1L)).willReturn(Optional.of(producto));
        given(ventaMapper.toDTO(venta)).willReturn(new VentaDTO());

        VentaDTO result = ventaService.saveD(caDto);

        Assertions.assertThat(result).isNotNull();
        verify(repo).save(venta);
        verify(ventaDetalleRepository).save(any(VentaDetalle.class));
        verify(ventCarritoRepository).deleteByDniruc("77777777");
    }
}
