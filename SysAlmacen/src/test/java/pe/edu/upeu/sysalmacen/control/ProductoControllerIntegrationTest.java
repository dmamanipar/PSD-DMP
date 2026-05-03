package pe.edu.upeu.sysalmacen.control;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import pe.edu.upeu.sysalmacen.dtos.ProductoDTO;
import pe.edu.upeu.sysalmacen.modelo.Categoria;
import pe.edu.upeu.sysalmacen.modelo.Marca;
import pe.edu.upeu.sysalmacen.modelo.UnidadMedida;
import pe.edu.upeu.sysalmacen.repositorio.ICategoriaRepository;
import pe.edu.upeu.sysalmacen.repositorio.IMarcaRepository;
import pe.edu.upeu.sysalmacen.repositorio.IUnidadMedidaRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - ProductoController")
class ProductoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ICategoriaRepository categoriaRepository;
    @Autowired
    private IMarcaRepository marcaRepository;
    @Autowired
    private IUnidadMedidaRepository unidadMedidaRepository;

    private String token;
    private Long categoriaId;
    private Long marcaId;
    private Long unidadMedidaId;
    private Long productoId;

    @BeforeAll
    void setup() throws Exception {
        token = obtenerTokenJwt();
        
        // Limpiar para asegurar IDs limpios si es necesario (BaseIntegrationTest ya usa tc)
        // categoriaRepository.deleteAll(); // Cuidado si otros tests dependen de esto
        
        Categoria cat = categoriaRepository.save(Categoria.builder().nombre("Electrónica").build());
        Marca mar = marcaRepository.save(Marca.builder().nombre("Sony").build());
        UnidadMedida uni = unidadMedidaRepository.save(UnidadMedida.builder().nombreMedida("Unidad").build());
        
        categoriaId = cat.getIdCategoria();
        marcaId = mar.getIdMarca();
        unidadMedidaId = uni.getIdUnidad();
    }

    @Order(1)
    @DisplayName("POST /productos - crea producto y retorna 201")
    @Test
    void testCrearProducto_RetornaCreated() throws Exception {
        ProductoDTO.ProductoCADto dto = new ProductoDTO.ProductoCADto(
                null, "Audífonos Bluetooth", 150.0, 140.0, 10.0, 50.0, 45.0, 
                categoriaId, marcaId, unidadMedidaId);

        String location = mockMvc.perform(post("/productos")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        String[] partes = location.split("/");
        productoId = Long.parseLong(partes[partes.length - 1]);
        Assertions.assertNotNull(productoId);
    }

    @Order(2)
    @DisplayName("GET /productos - retorna lista")
    @Test
    void testListarProductos_RetornaOk() throws Exception {
        mockMvc.perform(get("/productos")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Order(3)
    @DisplayName("GET /productos/{id} - retorna producto")
    @Test
    void testBuscarProductoPorId_RetornaOk() throws Exception {
        mockMvc.perform(get("/productos/{id}", productoId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Audífonos Bluetooth")));
    }

    @Order(4)
    @DisplayName("PUT /productos/{id} - actualiza producto")
    @Test
    void testActualizarProducto_RetornaOk() throws Exception {
        ProductoDTO.ProductoCADto dtoActualizado = new ProductoDTO.ProductoCADto(
                productoId, "Audífonos Sony Actualizados", 160.0, 150.0, 10.0, 40.0, 50.0, 
                categoriaId, marcaId, unidadMedidaId);

        mockMvc.perform(put("/productos/{id}", productoId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Audífonos Sony Actualizados")));
    }

    @Order(5)
    @DisplayName("DELETE /productos/{id} - elimina producto")
    @Test
    void testEliminarProducto_RetornaNoContent() throws Exception {
        mockMvc.perform(delete("/productos/{id}", productoId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        
        mockMvc.perform(get("/productos/{id}", productoId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }
}
