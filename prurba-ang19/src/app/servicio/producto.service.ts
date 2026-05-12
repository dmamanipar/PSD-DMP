import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {environment} from "../../environments/environment.development";
import {Producto} from "../modelo/Producto";
import {BehaviorSubject, Observable, Subject, tap} from "rxjs";
import {ProductoRepor} from "../modelo/ProductoRepor";
@Injectable({
  providedIn: 'root'
})

export class ProductoService {
  private url:string = `${environment.HOST}/productos`;

  //private productosSubject = new BehaviorSubject<ProductoRepor[]>([]); // Comportamiento inicial
  //productos$ = this.productosSubject.asObservable(); // Observable para suscribirse
  private productosSubject: Subject<ProductoRepor[]> = new Subject<ProductoRepor[]>;

  private productoSeleccionadoSubject = new BehaviorSubject<ProductoRepor | null>(null);
  productoSeleccionado$ = this.productoSeleccionadoSubject.asObservable();

  constructor(private http: HttpClient) { }


  findAll():void{
    //return this.http.get<Producto[]>(this.url);
    this.http.get<ProductoRepor[]>(this.url).subscribe(data=>{
      this.productosSubject.next(data);
    });
  }

  findById(id:number){
    return this.http.get<ProductoRepor>(this.url+`/${id}`);
  }

  save(producto:Producto):Observable<Producto>{
    //return this.http.post(this.url, producto);
    return this.http.post<Producto>(this.url, producto).pipe(
      tap(() => this.findAll())
    );
  }

  update(id: number, producto: Producto):Observable<Producto>{
    return this.http.put<Producto>(`${this.url}/${id}`, producto).pipe(
      tap(() => this.findAll())
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`).pipe(
      tap(() => this.findAll())
    );
  }

  seleccionarProducto(producto: ProductoRepor) {
    console.log("SERVICE");
    console.log(producto);
    this.productoSeleccionadoSubject.next(producto);
  }

  setProductosSubject(data: ProductoRepor[]){this.productosSubject.next(data);}
  getProductosSubject(){return this.productosSubject.asObservable();}

  listPageable(p: number, s: number){
    return this.http.get<any>(`${this.url}/pageable?page=${p}&size=${s}`);
  }

}
