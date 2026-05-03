package pe.edu.upeu.sysalmacen.servicio;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.sysalmacen.excepciones.CustomResponse;
import pe.edu.upeu.sysalmacen.excepciones.ModelNotFoundException;
import pe.edu.upeu.sysalmacen.modelo.UnidadMedida;
import pe.edu.upeu.sysalmacen.repositorio.IUnidadMedidaRepository;
import pe.edu.upeu.sysalmacen.servicio.impl.UnidadMedidaServiceImp;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas unitarias - UnidadMedidaService")
class UnidadMedidaServiceTest {

    @Mock
    private IUnidadMedidaRepository repo;

    @InjectMocks
    private UnidadMedidaServiceImp unidadMedidaService;

    private UnidadMedida unidadMedida;

    @BeforeEach
    void setUp() {
        unidadMedida = UnidadMedida.builder()
                .idUnidad(1L)
                .nombreMedida("Kilogramo")
                .build();
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("Guardar unidad medida - debe retornar la unidad guardada")
    @Test
    void testSaveUnidadMedida_RetornaUnidad() {
        given(repo.save(unidadMedida)).willReturn(unidadMedida);

        UnidadMedida resultado = unidadMedidaService.save(unidadMedida);

        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.getNombreMedida()).isEqualTo("Kilogramo");
        then(repo).should(times(1)).save(unidadMedida);
    }

    @Order(2)
    @DisplayName("Guardar unidad medida - verifica diferentes nombres de medida")
    @ParameterizedTest(name = "id={0}, nombre=''{1}''")
    @CsvSource({
        "1, Kilogramo",
        "2, Litro",
        "3, Metro",
        "4, Unidad"
    })
    void testSaveUnidadMedida_ConDiferentesNombres(Long id, String nombre) {
        UnidadMedida unidad = UnidadMedida.builder().idUnidad(id).nombreMedida(nombre).build();
        given(repo.save(unidad)).willReturn(unidad);

        UnidadMedida resultado = unidadMedidaService.save(unidad);

        Assertions.assertThat(resultado.getNombreMedida()).isEqualTo(nombre);
        Assertions.assertThat(resultado.getIdUnidad()).isEqualTo(id);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Order(3)
    @DisplayName("Listar unidades de medida - debe retornar lista completa")
    @Test
    void testFindAllUnidades_RetornaLista() {
        UnidadMedida u2 = UnidadMedida.builder().idUnidad(2L).nombreMedida("Litro").build();
        UnidadMedida u3 = UnidadMedida.builder().idUnidad(3L).nombreMedida("Metro").build();
        given(repo.findAll()).willReturn(List.of(unidadMedida, u2, u3));

        List<UnidadMedida> resultado = unidadMedidaService.findAll();

        Assertions.assertThat(resultado).hasSize(3);
        Assertions.assertThat(resultado).extracting(UnidadMedida::getNombreMedida)
                .containsExactly("Kilogramo", "Litro", "Metro");
    }

    @Order(4)
    @DisplayName("Listar unidades de medida - lista vacía")
    @Test
    void testFindAllUnidades_ListaVacia() {
        given(repo.findAll()).willReturn(List.of());

        List<UnidadMedida> resultado = unidadMedidaService.findAll();

        Assertions.assertThat(resultado).isEmpty();
        then(repo).should(times(1)).findAll();
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Order(5)
    @DisplayName("Buscar unidad de medida por id - retorna unidad existente")
    @ParameterizedTest(name = "id={0}, nombre=''{1}''")
    @CsvSource({"1, Kilogramo", "2, Litro", "3, Metro"})
    void testFindById_RetornaUnidadExistente(Long id, String nombre) {
        UnidadMedida unidad = UnidadMedida.builder().idUnidad(id).nombreMedida(nombre).build();
        given(repo.findById(id)).willReturn(Optional.of(unidad));

        UnidadMedida resultado = unidadMedidaService.findById(id);

        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.getIdUnidad()).isEqualTo(id);
        Assertions.assertThat(resultado.getNombreMedida()).isEqualTo(nombre);
    }

    @Order(6)
    @DisplayName("Buscar unidad de medida por id - lanza excepción cuando no existe")
    @ParameterizedTest(name = "id inexistente={0}")
    @ValueSource(longs = {50L, 100L, 999L})
    void testFindById_LanzaExcepcionCuandoNoExiste(Long idInexistente) {
        given(repo.findById(idInexistente)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> unidadMedidaService.findById(idInexistente))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: " + idInexistente);
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Order(7)
    @DisplayName("Actualizar unidad medida - retorna unidad actualizada")
    @Test
    void testUpdateUnidadMedida_RetornaUnidadActualizada() {
        UnidadMedida unidadActualizada = UnidadMedida.builder()
                .idUnidad(1L).nombreMedida("Gramo").build();
        given(repo.findById(1L)).willReturn(Optional.of(unidadMedida));
        given(repo.save(unidadActualizada)).willReturn(unidadActualizada);

        UnidadMedida resultado = unidadMedidaService.update(1L, unidadActualizada);

        Assertions.assertThat(resultado.getNombreMedida()).isEqualTo("Gramo");
        then(repo).should(times(1)).findById(1L);
        then(repo).should(times(1)).save(unidadActualizada);
    }

    @Order(8)
    @DisplayName("Actualizar unidad medida - lanza excepción cuando id no existe")
    @Test
    void testUpdateUnidadMedida_LanzaExcepcionCuandoIdNoExiste() {
        Long idInexistente = 99L;
        given(repo.findById(idInexistente)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> unidadMedidaService.update(idInexistente, unidadMedida))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: " + idInexistente);
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Order(9)
    @DisplayName("Eliminar unidad medida - debe retornar respuesta exitosa")
    @Test
    void testDeleteUnidadMedida_RetornaCustomResponseExitoso() {
        given(repo.findById(1L)).willReturn(Optional.of(unidadMedida));

        CustomResponse respuesta = unidadMedidaService.delete(1L);

        Assertions.assertThat(respuesta).isNotNull();
        Assertions.assertThat(respuesta.getMessage()).isEqualTo("true");
        Assertions.assertThat(respuesta.getStatusCode()).isEqualTo(200);
        Assertions.assertThat(respuesta.getDetails()).isEqualTo("Todo Ok");
        then(repo).should(times(1)).deleteById(1L);
    }

    @Order(10)
    @DisplayName("Eliminar unidad medida - lanza excepción cuando id no existe")
    @ParameterizedTest(name = "id inexistente={0}")
    @ValueSource(longs = {77L, 88L, 99L})
    void testDeleteUnidadMedida_LanzaExcepcionCuandoIdNoExiste(Long idInexistente) {
        given(repo.findById(idInexistente)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> unidadMedidaService.delete(idInexistente))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: " + idInexistente);

        then(repo).should(never()).deleteById(any());
    }
}
