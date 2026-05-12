import {Categoria} from "./Categoria";
import {Marca} from "./Marca";
import {UnidadMedida} from "./UnidadMedida";

export class ProductoRepor {
  constructor(
    public idProducto: number,
    public nombre: string,
    public pu: number,
    public puOld: number,
    public utilidad: number,
    public stock: number,
    public stockOld: number,
    public categoria: Categoria,
    public marca: Marca,
    public unidadMedida:UnidadMedida,
  ) {}
}
