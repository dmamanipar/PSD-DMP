package pe.edu.upeu.sysalmacen.control;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import pe.edu.upeu.sysalmacen.dtos.MarcaDTO;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para MarcaController.
 *
 * Estrategia:
 *  - @SpringBootTest levanta el contexto completo con H2 en memoria.
 *  - MockMvc envía peticiones HTTP reales al DispatcherServlet.
 *  - JWT se obtiene una sola vez en @BeforeAll para toda la clase.
 *  - @TestInstance(PER_CLASS) permite @BeforeAll en método no estático.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - MarcaController")
class MarcaControllerIntegrationTest extends BaseIntegrationTest {

    private String token;
    private Long marcaId;

    @BeforeAll
    void autenticar() throws Exception {
        token = obtenerTokenJwt();
    }

    // ─── POST /marcas ────────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("POST /marcas - crea marca y retorna HTTP 200 con message=true")
    @Test
    void testCrearMarca_RetornaOkConMessageTrue() throws Exception {
        MarcaDTO dto = new MarcaDTO(null, "MarcaIntegracion");

        mockMvc.perform(post("/marcas")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("true")));
    }

    @Order(2)
    @DisplayName("POST /marcas - verifica diferentes nombres de marca")
    @ParameterizedTest(name = "nombre=''{0}''")
    @ValueSource(strings = {"Nike", "Adidas", "Puma"})
    void testCrearMarca_ConDiferentesNombres(String nombre) throws Exception {
        MarcaDTO dto = new MarcaDTO(null, nombre);

        mockMvc.perform(post("/marcas")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("true")))
                .andExpect(jsonPath("$.statusCode", is(200)));
    }

    @Order(3)
    @DisplayName("POST /marcas - retorna HTTP 403 sin token de autenticacion")
    @Test
    void testCrearMarca_SinToken_RetornaForbiddenOUnauthorized() throws Exception {
        MarcaDTO dto = new MarcaDTO(null, "MarcaSinAuth");

        mockMvc.perform(post("/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ─── GET /marcas ─────────────────────────────────────────────────────────

    @Order(4)
    @DisplayName("GET /marcas - retorna lista de marcas con HTTP 200")
    @Test
    void testListarMarcas_RetornaListaConHttpOk() throws Exception {
        mockMvc.perform(get("/marcas")
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    // ─── GET /marcas/buscarmaxid ─────────────────────────────────────────────

    @Order(5)
    @DisplayName("GET /marcas/buscarmaxid - retorna Long con el max id existente")
    @Test
    void testObtenerMaxId_RetornaLongPositivo() throws Exception {
        String respuesta = mockMvc.perform(get("/marcas/buscarmaxid")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        marcaId = Long.parseLong(respuesta);
        Assertions.assertTrue(marcaId > 0, "El id máximo debe ser mayor a 0");
    }

    // ─── GET /marcas/{id} ────────────────────────────────────────────────────

    @Order(6)
    @DisplayName("GET /marcas/{id} - retorna la marca con el id solicitado")
    @Test
    void testBuscarMarcaPorId_RetornaMarcaDTO() throws Exception {
        // Reutiliza el id obtenido en el test anterior
        if (marcaId == null) {
            String resp = mockMvc.perform(get("/marcas/buscarmaxid")
                            .header("Authorization", bearer(token)))
                    .andReturn().getResponse().getContentAsString();
            marcaId = Long.parseLong(resp);
        }

        mockMvc.perform(get("/marcas/{id}", marcaId)
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMarca", is(marcaId.intValue())));
    }

    @Order(7)
    @DisplayName("GET /marcas/{id} - retorna HTTP 404 cuando id no existe")
    @Test
    void testBuscarMarcaPorId_IdInexistente_RetornaNotFound() throws Exception {
        mockMvc.perform(get("/marcas/{id}", 999999L)
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /marcas/{id} ────────────────────────────────────────────────────

    @Order(8)
    @DisplayName("PUT /marcas/{id} - actualiza la marca y retorna HTTP 200")
    @Test
    void testActualizarMarca_RetornaMarcaActualizada() throws Exception {
        if (marcaId == null) {
            String resp = mockMvc.perform(get("/marcas/buscarmaxid")
                            .header("Authorization", bearer(token)))
                    .andReturn().getResponse().getContentAsString();
            marcaId = Long.parseLong(resp);
        }
        MarcaDTO dtoActualizado = new MarcaDTO(marcaId, "MarcaActualizadaIT");

        mockMvc.perform(put("/marcas/{id}", marcaId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMarca", is(marcaId.intValue())))
                .andExpect(jsonPath("$.nombre", is("MarcaActualizadaIT")));
    }

    // ─── DELETE /marcas/{id} ─────────────────────────────────────────────────

    @Order(9)
    @DisplayName("DELETE /marcas/{id} - elimina la marca y retorna HTTP 200")
    @Test
    void testEliminarMarca_RetornaOkConMessageTrue() throws Exception {


        // Obtener el id recién creado
        String idStr = mockMvc.perform(get("/marcas/buscarmaxid")
                        .header("Authorization", bearer(token)))
                .andReturn().getResponse().getContentAsString();
        Long idEliminar = Long.parseLong(idStr);

        mockMvc.perform(delete("/marcas/{id}", idEliminar)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("true")));
    }

    @Order(10)
    @DisplayName("DELETE /marcas/{id} - id inexistente retorna HTTP 404")
    @Test
    void testEliminarMarca_IdInexistente_RetornaNotFound() throws Exception {
        mockMvc.perform(delete("/marcas/{id}", 999999L)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }
}
