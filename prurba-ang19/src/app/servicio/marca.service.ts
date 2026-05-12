import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment.development";
import {BehaviorSubject, Observable, Subject, tap} from "rxjs";
import {ProductoRepor} from "../modelo/ProductoRepor";
import {HttpClient} from "@angular/common/http";
import {Marca} from "../modelo/Marca";

@Injectable({
  providedIn: 'root'
})
export class MarcaService {
  private url:string = `${environment.HOST}/marcas`;
  private marcaSubject = new BehaviorSubject<Marca[]>([]);
  marcas$ = this.marcaSubject.asObservable();

  private messageChange: Subject<string> = new Subject<string>;
  constructor(private http: HttpClient) { }
  findAll():void{
    this.http.get<Marca[]>(this.url).subscribe(data=>{
      this.marcaSubject.next(data);
    });
  }
  findById(id:number){
    return this.http.get<Marca>(this.url+`/${id}`);
  }
  save(marca:Marca):Observable<Marca>{
    return this.http.post<Marca>(this.url, marca).pipe(
      tap(() => this.findAll())
    );
  }
  update(id: number, marca: Marca):Observable<Marca>{
    return this.http.put<Marca>(`${this.url}/${id}`, marca).pipe(
      tap(() => this.findAll())
    );
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`).pipe(
      tap(() => this.findAll())
    );
  }
  setMessageChange(data: string){
    this.messageChange.next(data);
  }

}
