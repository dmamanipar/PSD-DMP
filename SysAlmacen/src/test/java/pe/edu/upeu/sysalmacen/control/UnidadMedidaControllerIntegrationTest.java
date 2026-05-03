package pe.edu.upeu.sysalmacen.control;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import pe.edu.upeu.sysalmacen.dtos.UnidadMedidaDTO;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para UnidadMedidaController.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - UnidadMedidaController")
class UnidadMedidaControllerIntegrationTest extends BaseIntegrationTest {

    private String token;
    private Long unidadId;

    @BeforeAll
    void autenticar() throws Exception {
        token = obtenerTokenJwt();
    }

    // ─── POST /unidadmedidas ─────────────────────────────────────────────────

    @Order(1)
    @DisplayName("POST /unidadmedidas - crea unidad de medida y retorna HTTP 201")
    @Test
    void testCrearUnidadMedida_RetornaCreatedConLocation() throws Exception {
        UnidadMedidaDTO dto = new UnidadMedidaDTO(null, "Kilogramo");

        String location = mockMvc.perform(post("/unidadmedidas")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn()
                .getResponse().getHeader("Location");

        String[] partes = location.split("/");
        unidadId = Long.parseLong(partes[partes.length - 1]);
        Assertions.assertNotNull(unidadId);
    }

    @Order(2)
    @DisplayName("POST /unidadmedidas - verifica diferentes nombres de medida")
    @ParameterizedTest(name = "nombre=''{0}''")
    @ValueSource(strings = {"Litro", "Metro", "Gramo", "Unidad"})
    void testCrearUnidadMedida_ConDiferentesNombres(String nombre) throws Exception {
        UnidadMedidaDTO dto = new UnidadMedidaDTO(null, nombre);

        mockMvc.perform(post("/unidadmedidas")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Order(3)
    @DisplayName("POST /unidadmedidas - sin autenticacion retorna 401 o 403")
    @Test
    void testCrearUnidadMedida_SinToken_RetornaUnauthorized() throws Exception {
        UnidadMedidaDTO dto = new UnidadMedidaDTO(null, "SinAuth");

        mockMvc.perform(post("/unidadmedidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ─── GET /unidadmedidas ──────────────────────────────────────────────────

    @Order(4)
    @DisplayName("GET /unidadmedidas - retorna lista con HTTP 200")
    @Test
    void testListarUnidadesMedida_RetornaListaConHttpOk() throws Exception {
        mockMvc.perform(get("/unidadmedidas")
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ─── GET /unidadmedidas/{id} ─────────────────────────────────────────────

    @Order(5)
    @DisplayName("GET /unidadmedidas/{id} - retorna unidad de medida por id")
    @Test
    void testBuscarUnidadPorId_RetornaUnidadMedidaDTO() throws Exception {
        Assertions.assertNotNull(unidadId);

        mockMvc.perform(get("/unidadmedidas/{id}", unidadId)
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUnidad", is(unidadId.intValue())))
                .andExpect(jsonPath("$.nombreMedida", is("Kilogramo")));
    }

    @Order(6)
    @DisplayName("GET /unidadmedidas/{id} - id inexistente retorna HTTP 404")
    @Test
    void testBuscarUnidadPorId_IdInexistente_RetornaNotFound() throws Exception {
        mockMvc.perform(get("/unidadmedidas/{id}", 999999L)
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /unidadmedidas/{id} ─────────────────────────────────────────────

    @Order(7)
    @DisplayName("PUT /unidadmedidas/{id} - actualiza unidad y retorna HTTP 200")
    @Test
    void testActualizarUnidadMedida_RetornaUnidadActualizada() throws Exception {
        Assertions.assertNotNull(unidadId);
        UnidadMedidaDTO dtoActualizado = new UnidadMedidaDTO(unidadId, "Gramo Actualizado");

        mockMvc.perform(put("/unidadmedidas/{id}", unidadId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUnidad", is(unidadId.intValue())))
                .andExpect(jsonPath("$.nombreMedida", is("Gramo Actualizado")));
    }

    // ─── DELETE /unidadmedidas/{id} ──────────────────────────────────────────

    @Order(8)
    @DisplayName("DELETE /unidadmedidas/{id} - elimina unidad y retorna HTTP 204")
    @Test
    void testEliminarUnidadMedida_RetornaNoContent() throws Exception {
        // Crear unidad específica para eliminar
        UnidadMedidaDTO dtoEliminar = new UnidadMedidaDTO(null, "AEliminar");
        String location = mockMvc.perform(post("/unidadmedidas")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoEliminar)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        String[] partes = location.split("/");
        Long idEliminar = Long.parseLong(partes[partes.length - 1]);

        mockMvc.perform(delete("/unidadmedidas/{id}", idEliminar)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Order(9)
    @DisplayName("DELETE /unidadmedidas/{id} - id inexistente retorna HTTP 404")
    @Test
    void testEliminarUnidadMedida_IdInexistente_RetornaNotFound() throws Exception {
        mockMvc.perform(delete("/unidadmedidas/{id}", 999999L)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }
}
