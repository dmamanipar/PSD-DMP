package pe.edu.upeu.sysalmacen.control;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import pe.edu.upeu.sysalmacen.dtos.ClienteDTO;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para ClienteController.
 * El Cliente usa dniruc (String) como PK, no secuencia autogenerada.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - ClienteController")
class ClienteControllerIntegrationTest extends BaseIntegrationTest {

    private String token;
    private static final String DNIRUC_TEST = "12345678";

    @BeforeAll
    void autenticar() throws Exception {
        token = obtenerTokenJwt();
    }

    // ─── POST /clientes ──────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("POST /clientes - crea cliente y retorna HTTP 201 con Location")
    @Test
    void testCrearCliente_RetornaCreatedConLocation() throws Exception {
        ClienteDTO dto = new ClienteDTO(DNIRUC_TEST, "Juan Pérez", null, "DNI", "Av. Lima 123");

        mockMvc.perform(post("/clientes")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(DNIRUC_TEST)));
    }

    @Order(2)
    @DisplayName("POST /clientes - crea clientes con diferentes tipos de documento")
    @ParameterizedTest(name = "dniruc=''{0}'', tipoDoc=''{1}''")
    @CsvSource({
        "20111222333, Empresa SAC, RUC",
        "87654321, María López, DNI"
    })
    void testCrearCliente_ConDiferentesDocumentos(String dniruc, String nombres, String tipoDoc) throws Exception {
        ClienteDTO dto = new ClienteDTO(dniruc, nombres, null, tipoDoc, null);

        mockMvc.perform(post("/clientes")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(dniruc)));
    }

    @Order(3)
    @DisplayName("POST /clientes - sin autenticacion retorna 401 o 403")
    @Test
    void testCrearCliente_SinToken_RetornaUnauthorized() throws Exception {
        ClienteDTO dto = new ClienteDTO("99999999", "Sin Auth", null, "DNI", null);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ─── GET /clientes ───────────────────────────────────────────────────────

    @Order(4)
    @DisplayName("GET /clientes - retorna lista de clientes con HTTP 200")
    @Test
    void testListarClientes_RetornaListaConHttpOk() throws Exception {
        mockMvc.perform(get("/clientes")
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ─── GET /clientes/{id} ──────────────────────────────────────────────────

    @Order(5)
    @DisplayName("GET /clientes/{id} - retorna cliente por dniruc")
    @Test
    void testBuscarClientePorDniruc_RetornaClienteDTO() throws Exception {
        mockMvc.perform(get("/clientes/{id}", DNIRUC_TEST)
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dniruc", is(DNIRUC_TEST)))
                .andExpect(jsonPath("$.nombres", is("Juan Pérez")));
    }

    @Order(6)
    @DisplayName("GET /clientes/{id} - dniruc inexistente retorna HTTP 404")
    @Test
    void testBuscarClientePorDniruc_Inexistente_RetornaNotFound() throws Exception {
        mockMvc.perform(get("/clientes/{id}", "00000000")
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /clientes/{id} ──────────────────────────────────────────────────

    @Order(7)
    @DisplayName("PUT /clientes/{id} - actualiza cliente y retorna HTTP 200")
    @Test
    void testActualizarCliente_RetornaClienteActualizado() throws Exception {
        ClienteDTO dtoActualizado = new ClienteDTO(
                DNIRUC_TEST, "Juan Pérez Actualizado", null, "DNI", "Jr. Nuevo 999");

        mockMvc.perform(put("/clientes/{id}", DNIRUC_TEST)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dniruc", is(DNIRUC_TEST)))
                .andExpect(jsonPath("$.nombres", is("Juan Pérez Actualizado")));
    }

    // ─── DELETE /clientes/{id} ───────────────────────────────────────────────

    @Order(8)
    @DisplayName("DELETE /clientes/{id} - elimina cliente y retorna HTTP 204")
    @Test
    void testEliminarCliente_RetornaNoContent() throws Exception {
        // Crear cliente específico para eliminar
        ClienteDTO dtoEliminar = new ClienteDTO("11223344", "AEliminar", null, "DNI", null);
        mockMvc.perform(post("/clientes")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoEliminar)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/clientes/{id}", "11223344")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Order(9)
    @DisplayName("DELETE /clientes/{id} - dniruc inexistente retorna HTTP 404")
    @Test
    void testEliminarCliente_Inexistente_RetornaNotFound() throws Exception {
        mockMvc.perform(delete("/clientes/{id}", "00000000")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }
}
