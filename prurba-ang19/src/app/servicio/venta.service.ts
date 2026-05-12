import { Injectable } from '@angular/core';
import {GenericService} from './generic.service';
import {Venta, VentaCA} from '../modelo/Venta';
import {BehaviorSubject, Subject} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class VentaService extends GenericService<Venta>{

  private entidadSubject = new BehaviorSubject<Venta[]>([]);
  private messageChange: Subject<string> = new Subject<string>;

  constructor(protected override http: HttpClient) {
    super(http,`${environment.HOST}/ventas`);
  }

  saveD(dto: VentaCA){ return this.http.post(this.url, dto);}


  setEntidadChange(data: Venta[]){ this.entidadSubject.next(data);}
  getEntidadChange(){return this.entidadSubject.asObservable(); }

  setMessageChange(data: string) {this.messageChange.next(data);}
  getMessageChange(){return this.messageChange.asObservable();}
}
