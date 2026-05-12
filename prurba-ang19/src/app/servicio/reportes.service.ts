import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment.development";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ReportesService {
  private url: string = `${environment.HOST}/reporte`;
  constructor(private http: HttpClient) {
  }
  //pdf
  generateReport() {
    return this.http.get(`${this.url}/generateReport`, {responseType: 'blob'});
  }


//Files, Image
  saveFile(data: File | null){
    const formdata: FormData = new FormData();
// @ts-ignore
    formdata.append('file', data);
    return this.http.post(`${this.url}/saveFile`, formdata);
  }
  readFile(id: number){
    return this.http.get(`${this.url}/readFile/${id}`, { responseType: 'blob'});
  }


  saveFileCloud(data: File | null){
    const formdata: FormData = new FormData();
    // @ts-ignore
    formdata.append('file', data);
    return this.http.post(`${this.url}/saveFileCloud`, formdata);
  }
}
