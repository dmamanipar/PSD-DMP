import {Usuario} from './Usuario';
import {ProductoRepor} from './ProductoRepor';

export interface VentCarrito {
  idCarrito: number
  dniruc: string
  producto: ProductoRepor
  nombreProducto: string
  cantidad: number
  punitario: number
  ptotal: number
  estado: number
  usuario: Usuario
}

export class VentCarritosCA{
  constructor(
   public idCarrito: number,
   public dniruc: string,
   public producto: number,
   public nombreProducto: string,
   public cantidad: number,
   public punitario: number,
   public ptotal: number,
   public estado: number,
   public usuario: number
 ){}
}
