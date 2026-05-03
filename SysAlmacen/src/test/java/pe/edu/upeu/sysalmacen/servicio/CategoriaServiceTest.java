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
import pe.edu.upeu.sysalmacen.modelo.Categoria;
import pe.edu.upeu.sysalmacen.repositorio.ICategoriaRepository;
import pe.edu.upeu.sysalmacen.servicio.impl.CategoriaServiceImp;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas unitarias - CategoriaService")
class CategoriaServiceTest {

    @Mock
    private ICategoriaRepository repo;

    @InjectMocks
    private CategoriaServiceImp categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder()
                .idCategoria(1L)
                .nombre("Electrónica")
                .build();
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("Guardar categoria - debe retornar la categoria guardada")
    @Test
    void testSaveCategoria_RetornaCategoria() {
        given(repo.save(categoria)).willReturn(categoria);

        Categoria resultado = categoriaService.save(categoria);

        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.getNombre()).isEqualTo("Electrónica");
        then(repo).should(times(1)).save(categoria);
    }

    @Order(2)
    @DisplayName("Guardar categoria - verifica nombre con múltiples valores")
    @ParameterizedTest(name = "nombre=''{0}''")
    @ValueSource(strings = {"Ropa", "Calzado", "Alimentos", "Tecnología"})
    void testSaveCategoria_ConDiferentesNombres(String nombre) {
        Categoria cat = Categoria.builder().idCategoria(1L).nombre(nombre).build();
        given(repo.save(cat)).willReturn(cat);

        Categoria resultado = categoriaService.save(cat);

        Assertions.assertThat(resultado.getNombre()).isEqualTo(nombre);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Order(3)
    @DisplayName("Listar categorias - debe retornar lista completa")
    @Test
    void testFindAllCategorias_RetornaLista() {
        Categoria cat2 = Categoria.builder().idCategoria(2L).nombre("Ropa").build();
        given(repo.findAll()).willReturn(List.of(categoria, cat2));

        List<Categoria> resultado = categoriaService.findAll();

        Assertions.assertThat(resultado)
                .hasSize(2)
                .containsExactly(categoria, cat2);
        then(repo).should(times(1)).findAll();
    }

    @Order(4)
    @DisplayName("Listar categorias - lista vacía")
    @Test
    void testFindAllCategorias_ListaVacia() {
        given(repo.findAll()).willReturn(List.of());

        List<Categoria> resultado = categoriaService.findAll();

        Assertions.assertThat(resultado).isEmpty();
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Order(5)
    @DisplayName("Buscar categoria por id - debe retornar categoria existente")
    @ParameterizedTest(name = "id={0}, nombre=''{1}''")
    @CsvSource({"1, Electrónica", "2, Ropa", "3, Calzado"})
    void testFindById_RetornaCategoria(Long id, String nombre) {
        Categoria cat = Categoria.builder().idCategoria(id).nombre(nombre).build();
        given(repo.findById(id)).willReturn(Optional.of(cat));

        Categoria resultado = categoriaService.findById(id);

        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.getIdCategoria()).isEqualTo(id);
        Assertions.assertThat(resultado.getNombre()).isEqualTo(nombre);
    }

    @Order(6)
    @DisplayName("Buscar categoria por id - lanza excepción cuando no existe")
    @ParameterizedTest(name = "id inexistente={0}")
    @ValueSource(longs = {99L, 100L, 999L})
    void testFindById_LanzaExcepcionCuandoNoExiste(Long idInexistente) {
        given(repo.findById(idInexistente)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> categoriaService.findById(idInexistente))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: " + idInexistente);
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Order(7)
    @DisplayName("Actualizar categoria - debe retornar categoria actualizada")
    @Test
    void testUpdateCategoria_RetornaCategoriaActualizada() {
        Categoria categoriaActualizada = Categoria.builder()
                .idCategoria(1L).nombre("Tecnología").build();
        given(repo.findById(1L)).willReturn(Optional.of(categoria));
        given(repo.save(categoriaActualizada)).willReturn(categoriaActualizada);

        Categoria resultado = categoriaService.update(1L, categoriaActualizada);

        Assertions.assertThat(resultado.getNombre()).isEqualTo("Tecnología");
        then(repo).should(times(1)).findById(1L);
        then(repo).should(times(1)).save(categoriaActualizada);
    }

    @Order(8)
    @DisplayName("Actualizar categoria - lanza excepción cuando id no existe")
    @Test
    void testUpdateCategoria_LanzaExcepcionCuandoIdNoExiste() {
        Long idInexistente = 50L;
        given(repo.findById(idInexistente)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> categoriaService.update(idInexistente, categoria))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: " + idInexistente);
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Order(9)
    @DisplayName("Eliminar categoria - debe retornar respuesta exitosa")
    @Test
    void testDeleteCategoria_RetornaCustomResponseExitoso() {
        given(repo.findById(1L)).willReturn(Optional.of(categoria));

        CustomResponse respuesta = categoriaService.delete(1L);

        Assertions.assertThat(respuesta).isNotNull();
        Assertions.assertThat(respuesta.getMessage()).isEqualTo("true");
        Assertions.assertThat(respuesta.getStatusCode()).isEqualTo(200);
        Assertions.assertThat(respuesta.getDetails()).isEqualTo("Todo Ok");
        then(repo).should(times(1)).deleteById(1L);
    }

    @Order(10)
    @DisplayName("Eliminar categoria - lanza excepción cuando id no existe")
    @ParameterizedTest(name = "id inexistente={0}")
    @ValueSource(longs = {88L, 99L})
    void testDeleteCategoria_LanzaExcepcionCuandoIdNoExiste(Long idInexistente) {
        given(repo.findById(idInexistente)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> categoriaService.delete(idInexistente))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: " + idInexistente);

        then(repo).should(never()).deleteById(any());
    }
}
