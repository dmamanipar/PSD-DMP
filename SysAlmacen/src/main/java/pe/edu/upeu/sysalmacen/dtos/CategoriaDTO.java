package pe.edu.upeu.sysalmacen.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoriaDTO {
    private Long idCategoria;

    @NotEmpty
    @NotNull
    private String nombre;
}
