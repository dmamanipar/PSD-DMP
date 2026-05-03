package pe.edu.upeu.sysalmacen.servicio;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.sysalmacen.dtos.ProductoDTO;
import pe.edu.upeu.sysalmacen.excepciones.ModelNotFoundException;
import pe.edu.upeu.sysalmacen.mappers.ProductoMapper;
import pe.edu.upeu.sysalmacen.modelo.Categoria;
import pe.edu.upeu.sysalmacen.modelo.Marca;
import pe.edu.upeu.sysalmacen.modelo.Producto;
import pe.edu.upeu.sysalmacen.modelo.UnidadMedida;
import pe.edu.upeu.sysalmacen.repositorio.ICategoriaRepository;
import pe.edu.upeu.sysalmacen.repositorio.IMarcaRepository;
import pe.edu.upeu.sysalmacen.repositorio.IProductoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IUnidadMedidaRepository;
import pe.edu.upeu.sysalmacen.servicio.impl.ProductoServiceImp;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas unitarias - ProductoService")
class ProductoServiceTest {

    @Mock
    private IProductoRepository repo;
    @Mock
    private ProductoMapper productoMapper;
    @Mock
    private ICategoriaRepository categoriaRepository;
    @Mock
    private IMarcaRepository marcaRepository;
    @Mock
    private IUnidadMedidaRepository unidadMedidaRepository;

    @InjectMocks
    private ProductoServiceImp productoService;

    private Producto producto;
    private Categoria categoria;
    private Marca marca;
    private UnidadMedida unidadMedida;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().idCategoria(1L).nombre("Electrónica").build();
        marca = Marca.builder().idMarca(1L).nombre("Sony").build();
        unidadMedida = UnidadMedida.builder().idUnidad(1L).nombreMedida("Unidad").build();

        producto = Producto.builder()
                .idProducto(1L)
                .nombre("Audífonos Bluetooth")
                .pu(150.0)
                .stock(50.0)
                .categoria(categoria)
                .marca(marca)
                .unidadMedida(unidadMedida)
                .build();
    }

    @Order(1)
    @DisplayName("Listar productos - debe retornar lista completa")
    @Test
    void testFindAll_RetornaLista() {
        given(repo.findAll()).willReturn(List.of(producto));

        List<Producto> resultado = productoService.findAll();

        Assertions.assertThat(resultado).hasSize(1);
        Assertions.assertThat(resultado.get(0).getNombre()).isEqualTo("Audífonos Bluetooth");
    }

    @Order(2)
    @DisplayName("Buscar producto por id - debe retornar producto existente")
    @Test
    void testFindById_RetornaProducto() {
        given(repo.findById(1L)).willReturn(Optional.of(producto));

        Producto resultado = productoService.findById(1L);

        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.getIdProducto()).isEqualTo(1L);
    }

    @Order(3)
    @DisplayName("Buscar producto por id - lanza excepción cuando no existe")
    @Test
    void testFindById_LanzaExcepcionCuandoNoExiste() {
        given(repo.findById(99L)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> productoService.findById(99L))
                .isInstanceOf(ModelNotFoundException.class);
    }

    @Order(4)
    @DisplayName("Guardar producto DTO - debe retornar el DTO guardado")
    @Test
    void testSaveD_RetornaProductoDTO() {
        ProductoDTO.ProductoCADto caDto = new ProductoDTO.ProductoCADto(
                null, "Nuevo Producto", 100.0, 90.0, 10.0, 20.0, 15.0, 1L, 1L, 1L);

        Producto entitiySinId = Producto.builder().nombre("Nuevo Producto").build();
        Producto entityConId = Producto.builder().idProducto(2L).nombre("Nuevo Producto").build();
        ProductoDTO resultDto = new ProductoDTO();
        resultDto.setIdProducto(2L);
        resultDto.setNombre("Nuevo Producto");

        given(productoMapper.toEntityFromCADTO(caDto)).willReturn(entitiySinId);
        given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoria));
        given(marcaRepository.findById(1L)).willReturn(Optional.of(marca));
        given(unidadMedidaRepository.findById(1L)).willReturn(Optional.of(unidadMedida));
        given(repo.save(entitiySinId)).willReturn(entityConId);
        given(productoMapper.toDTO(entityConId)).willReturn(resultDto);

        ProductoDTO resultado = productoService.saveD(caDto);

        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.getIdProducto()).isEqualTo(2L);
        verify(repo).save(any(Producto.class));
    }

    @Order(5)
    @DisplayName("Eliminar producto - debe llamar al repositorio")
    @Test
    void testDelete_LlamaARepo() {
        given(repo.findById(1L)).willReturn(Optional.of(producto));

        productoService.delete(1L);

        verify(repo).deleteById(1L);
    }
}
