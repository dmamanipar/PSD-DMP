import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment.development";
import {Categoria} from "../modelo/Categoria";
import {GenericService} from "./generic.service";
import {BehaviorSubject, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CategoriaService extends GenericService<Categoria>{
  protected categoriaSubject = new
  BehaviorSubject<Categoria[]>([]);
  private messageChange: Subject<string> = new
  Subject<string>;
  constructor(protected override http: HttpClient) {
    super(http, `${environment.HOST}/categorias`);
  }
  setCategoriaChange(data: Categoria[]){
    this.categoriaSubject.next(data);
  }
  getCategoriaChange(){
    return this.categoriaSubject.asObservable();
  }

  setMessageChange(data: string){
    this.messageChange.next(data);
  }
  getMessageChange(){
    return this.messageChange.asObservable();
  }

}
