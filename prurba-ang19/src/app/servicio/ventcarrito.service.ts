import { Injectable } from '@angular/core';
import {GenericService} from './generic.service';
import {BehaviorSubject, Subject} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment.development';
import {VentCarrito, VentCarritosCA} from '../modelo/VentCarrito';


@Injectable({
  providedIn: 'root'
})
export class VentcarritoService extends GenericService<VentCarrito>{
  private entidadSubject = new BehaviorSubject<VentCarrito[]>([]);
  private messageChange: Subject<string> = new Subject<string>;

  constructor(protected override http: HttpClient) {
    super(http,`${environment.HOST}/ventcarritos`);
  }

  listarCarritoCliente(dniruc: string){
    return this.http.get<VentCarrito[]>(`${this.url}/list/${dniruc}`);
  }
  saveD(dto: VentCarritosCA){
    return this.http.post(this.url, dto);
  }

  setEntidadChange(data: VentCarrito[]){ this.entidadSubject.next(data);}
  getEntidadChange(){return this.entidadSubject.asObservable(); }

  setMessageChange(data: string) {
    this.messageChange.next(data);
  }
  getMessageChange(){
    return this.messageChange.asObservable();
  }
}
