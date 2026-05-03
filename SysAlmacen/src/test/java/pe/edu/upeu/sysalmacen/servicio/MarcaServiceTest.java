package pe.edu.upeu.sysalmacen.servicio;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.sysalmacen.excepciones.CustomResponse;
import pe.edu.upeu.sysalmacen.excepciones.ModelNotFoundException;
import pe.edu.upeu.sysalmacen.modelo.Marca;
import pe.edu.upeu.sysalmacen.repositorio.IMarcaRepository;
import pe.edu.upeu.sysalmacen.servicio.impl.MarcaServiceImp;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MarcaServiceTest {
    @Mock
    private IMarcaRepository repo;
    @InjectMocks
    private MarcaServiceImp marcaService;
    Marca marca;

    @BeforeEach
    void setUp() {
        marca = Marca.builder()
                .idMarca(1L)
                .nombre("Puma")
                .build();
    }

    @Order(1)
    @DisplayName("GuardarMarca")
    @Test
    void testSaveMarca() {
        //given
        given(repo.save(marca)).willReturn(marca);
        //when
        Marca ppx=marcaService.save(marca);
        //then
        Assertions.assertThat(ppx.getNombre()).isNotNull();
        Assertions.assertThat(ppx.getNombre()).isEqualTo(marca.getNombre());
    }

    @Order(2)
    @DisplayName("Listar Marca")
    @Test
    void testListMarca() {
        //given
        Marca p=Marca.builder()
                .idMarca(2L)
                .nombre("Adidas")
                .build();
        given(repo.findAll()).willReturn(List.of(marca,p));
        //when
        List<Marca> pl=marcaService.findAll();
        for (Marca pr:pl){
            System.out.println(pr.getNombre());
        }
        //then
        Assertions.assertThat(pl).hasSize(2);
        Assertions.assertThat(pl.get(0)).isEqualTo(marca);
        Assertions.assertThat(pl).hasSize(2);
    }

    @Order(3)
    @DisplayName("Actualizar Marca")
    @Test
    void testUpdateMarca() {
        //given
        given(repo.save(marca)).willReturn(marca);
        given(repo.findById(1L)).willReturn(Optional.of(marca));
        //when
        marca.setNombre("Nike");
        Marca pa=marcaService.update(marca.getIdMarca(),marca);
        //then
        System.out.println(pa.getNombre());
        Assertions.assertThat(pa.getNombre()).isEqualTo("Nike");
    }

    @Order(4)
    @DisplayName("Eliminar Marca")
    @Test
    void testDeletePeriodo() {
        //given
        given(repo.findById(1L)).willReturn(Optional.of(marca));
        //when
        CustomResponse pd=marcaService.delete(1L);
        //then
        System.out.println(pd.getMessage());
        Assertions.assertThat(pd.getMessage()).isEqualTo("true");
    }

    @Order(5)
    @DisplayName("Eliminar Marca Id no Existe")
    @Test
    void testDeleteByIdNonExistent() {
        //given
        Long idInexistente = 99L;
        given(repo.findById(idInexistente)).willReturn(Optional.empty());
        //when and then
        Assertions.assertThatThrownBy(() ->
                        marcaService.delete(idInexistente))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: "+idInexistente);
    }

}

