import { Injectable } from '@angular/core';
import {GenericService} from './generic.service';
import {Cliente} from '../modelo/Cliente';
import {BehaviorSubject, Subject} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class ClienteService  extends GenericService<Cliente> {

  private clientesSubject = new BehaviorSubject<Cliente[]>([]);
  private messageChange: Subject<string> = new Subject<string>;

  constructor(protected override http: HttpClient) {
    super(http,`${environment.HOST}/clientes`);
  }
  setClientesChange(data: Cliente[]){ this.clientesSubject.next(data);}
  getClientesChange(){return this.clientesSubject.asObservable(); }

  setMessageChange(data: string) {
    this.messageChange.next(data);
  }

}
