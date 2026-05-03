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
import pe.edu.upeu.sysalmacen.modelo.Cliente;
import pe.edu.upeu.sysalmacen.repositorio.IClienteRepository;
import pe.edu.upeu.sysalmacen.servicio.impl.ClienteServiceImp;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas unitarias - ClienteService")
class ClienteServiceTest {

    @Mock
    private IClienteRepository repo;

    @InjectMocks
    private ClienteServiceImp clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .dniruc("12345678")
                .nombres("Juan Pérez")
                .tipoDocumento("DNI")
                .direccion("Av. Lima 123")
                .repLegal(null)
                .build();
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("Guardar cliente - debe retornar cliente guardado")
    @Test
    void testSaveCliente_RetornaCliente() {
        given(repo.save(cliente)).willReturn(cliente);

        Cliente resultado = clienteService.save(cliente);

        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.getDniruc()).isEqualTo("12345678");
        Assertions.assertThat(resultado.getNombres()).isEqualTo("Juan Pérez");
        then(repo).should(times(1)).save(cliente);
    }

    @Order(2)
    @DisplayName("Guardar cliente - verifica con diferentes DNI/RUC")
    @ParameterizedTest(name = "dniruc=''{0}'', nombres=''{1}''")
    @CsvSource({
        "12345678, Juan Perez, DNI",
        "20123456789, Empresa SAC, RUC",
        "87654321, Maria Lopez, DNI"
    })
    void testSaveCliente_ConDiferentesDocumentos(String dniruc, String nombres, String tipoDoc) {
        Cliente cli = Cliente.builder()
                .dniruc(dniruc).nombres(nombres).tipoDocumento(tipoDoc).build();
        given(repo.save(cli)).willReturn(cli);

        Cliente resultado = clienteService.save(cli);

        Assertions.assertThat(resultado.getDniruc()).isEqualTo(dniruc);
        Assertions.assertThat(resultado.getTipoDocumento()).isEqualTo(tipoDoc);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Order(3)
    @DisplayName("Listar clientes - debe retornar lista completa")
    @Test
    void testFindAllClientes_RetornaLista() {
        Cliente cliente2 = Cliente.builder()
                .dniruc("20123456789").nombres("Empresa SAC")
                .tipoDocumento("RUC").build();
        given(repo.findAll()).willReturn(List.of(cliente, cliente2));

        List<Cliente> resultado = clienteService.findAll();

        Assertions.assertThat(resultado).hasSize(2);
        Assertions.assertThat(resultado).extracting(Cliente::getDniruc)
                .containsExactly("12345678", "20123456789");
    }

    @Order(4)
    @DisplayName("Listar clientes - lista vacía cuando no hay registros")
    @Test
    void testFindAllClientes_ListaVacia() {
        given(repo.findAll()).willReturn(List.of());

        List<Cliente> resultado = clienteService.findAll();

        Assertions.assertThat(resultado).isEmpty();
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Order(5)
    @DisplayName("Buscar cliente por dniruc - debe retornar cliente existente")
    @ParameterizedTest(name = "dniruc=''{0}''")
    @ValueSource(strings = {"12345678", "20123456789"})
    void testFindById_RetornaClienteExistente(String dniruc) {
        Cliente cli = Cliente.builder().dniruc(dniruc).nombres("Test").tipoDocumento("DNI").build();
        given(repo.findById(dniruc)).willReturn(Optional.of(cli));

        Cliente resultado = clienteService.findById(dniruc);

        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.getDniruc()).isEqualTo(dniruc);
    }

    @Order(6)
    @DisplayName("Buscar cliente por dniruc - lanza excepción cuando no existe")
    @ParameterizedTest(name = "dniruc inexistente=''{0}''")
    @ValueSource(strings = {"00000000", "99999999"})
    void testFindById_LanzaExcepcionCuandoNoExiste(String dnirucInexistente) {
        given(repo.findById(dnirucInexistente)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> clienteService.findById(dnirucInexistente))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: " + dnirucInexistente);
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Order(7)
    @DisplayName("Actualizar cliente - debe retornar cliente actualizado")
    @Test
    void testUpdateCliente_RetornaClienteActualizado() {
        Cliente clienteActualizado = Cliente.builder()
                .dniruc("12345678").nombres("Juan Actualizado")
                .tipoDocumento("DNI").build();
        given(repo.findById("12345678")).willReturn(Optional.of(cliente));
        given(repo.save(clienteActualizado)).willReturn(clienteActualizado);

        Cliente resultado = clienteService.update("12345678", clienteActualizado);

        Assertions.assertThat(resultado.getNombres()).isEqualTo("Juan Actualizado");
        then(repo).should(times(1)).findById("12345678");
        then(repo).should(times(1)).save(clienteActualizado);
    }

    @Order(8)
    @DisplayName("Actualizar cliente - lanza excepción cuando dniruc no existe")
    @Test
    void testUpdateCliente_LanzaExcepcionCuandoIdNoExiste() {
        String dnirucInexistente = "00000000";
        given(repo.findById(dnirucInexistente)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> clienteService.update(dnirucInexistente, cliente))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: " + dnirucInexistente);
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Order(9)
    @DisplayName("Eliminar cliente - debe retornar respuesta exitosa")
    @Test
    void testDeleteCliente_RetornaCustomResponseExitoso() {
        given(repo.findById("12345678")).willReturn(Optional.of(cliente));

        CustomResponse respuesta = clienteService.delete("12345678");

        Assertions.assertThat(respuesta).isNotNull();
        Assertions.assertThat(respuesta.getMessage()).isEqualTo("true");
        Assertions.assertThat(respuesta.getStatusCode()).isEqualTo(200);
        then(repo).should(times(1)).deleteById("12345678");
    }

    @Order(10)
    @DisplayName("Eliminar cliente - lanza excepción cuando dniruc no existe")
    @ParameterizedTest(name = "dniruc inexistente=''{0}''")
    @ValueSource(strings = {"00000000", "99999999"})
    void testDeleteCliente_LanzaExcepcionCuandoIdNoExiste(String dnirucInexistente) {
        given(repo.findById(dnirucInexistente)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> clienteService.delete(dnirucInexistente))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: " + dnirucInexistente);

        then(repo).should(never()).deleteById(any());
    }
}
