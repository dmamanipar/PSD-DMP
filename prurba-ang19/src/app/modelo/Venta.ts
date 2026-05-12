import {Cliente} from './Cliente';
import {Usuario} from './Usuario';

export interface Venta {
  idVenta: number
  precioBase: number
  igv: number
  precioTotal: number
  cliente: Cliente
  usuario: Usuario
  numDoc: string
  fechaGener: string
  serie: string
  tipoDoc: string
}

export class VentaCA {
  constructor(
  public idVenta: number,
  public precioBase: number,
  public igv: number,
  public precioTotal: number,
  public cliente: string,
  public usuario: number,
  public numDoc: string,
  public fechaGener: string,
  public serie: string,
  public tipoDoc: string
  ) {}
}
