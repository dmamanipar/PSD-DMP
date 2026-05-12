import {Injectable} from '@angular/core';
import {GenericService} from "./generic.service";
import {UnidadMedida} from "../modelo/UnidadMedida";
import {BehaviorSubject, Subject} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment.development";

@Injectable({
  providedIn: 'root'
})
export class UnidadmedidaService extends GenericService<UnidadMedida> {
  private unidMediadSubject = new BehaviorSubject<UnidadMedida[]>([]);
  private messageChange: Subject<string> = new Subject<string>;

  constructor(protected override http: HttpClient) {
    super(http, `${environment.HOST}/unidadmedidas`);
  }


  setUnidadMedidaChange(data: UnidadMedida[]) { this.unidMediadSubject.next(data);}
  getUnidadMedidaChange() { return this.unidMediadSubject.asObservable();}
  setMessageChange(data: string) { this.messageChange.next(data);}


}
