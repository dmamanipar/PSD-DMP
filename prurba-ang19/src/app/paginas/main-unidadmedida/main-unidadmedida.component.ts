import {Component, OnInit, ViewChild} from '@angular/core';
import {MaterialModule} from "../../material/material.module";
import {ActivatedRoute, Router, RouterLink, RouterOutlet} from "@angular/router";
import {MatTableDataSource} from "@angular/material/table";
import {MatSort} from "@angular/material/sort";
import {MatPaginator} from "@angular/material/paginator";
import {UnidadMedida} from "../../modelo/UnidadMedida";
import {MatSnackBar} from "@angular/material/snack-bar";

import {switchMap} from "rxjs";
import {UnidadmedidaService} from "../../servicio/unidadmedida.service";

@Component({
  selector: 'app-main-unidadmedida',
  standalone: true,
  imports: [MaterialModule, RouterOutlet, RouterLink],
  templateUrl: './main-unidadmedida.component.html',
  styleUrl: './main-unidadmedida.component.css'
})
export class MainUnidadmedidaComponent implements OnInit {
  dataSource!: MatTableDataSource<UnidadMedida>;

  columnsDefinitions = [
    { def: 'idUnidad', label: 'idUnidad', hide: true},
    { def: 'nombreMedida', label: 'nombreMedida', hide: false},
    { def: 'acciones', label: 'acciones', hide: false}
  ];

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private unidadMedidaService: UnidadmedidaService,
    private router: ActivatedRoute,
    private _snackBar: MatSnackBar
  ){}

  ngOnInit(): void {
    this.unidadMedidaService.findAll().subscribe(data => {
      this.unidadMedidaService.setUnidadMedidaChange(data);
    });
    this.unidadMedidaService.getUnidadMedidaChange().subscribe(data=>{
      this.dataSource = new MatTableDataSource(data);
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
      this.createTable(data);
    });
  }

  createTable(data: UnidadMedida[]){
    this.dataSource = new MatTableDataSource(data);
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  getDisplayedColumns(){
    return this.columnsDefinitions.filter(cd => !cd.hide).map(cd => cd.def);
  }

  applyFilter(e: any){
    this.dataSource.filter = e.target.value.trim();
  }

  delete(idUnidad: number){
    this.unidadMedidaService.delete(idUnidad)
      .pipe(switchMap( ()=> this.unidadMedidaService.findAll()))
      .subscribe(data => {
        this.unidadMedidaService.setUnidadMedidaChange(data);
        this.unidadMedidaService.setMessageChange('DELETED!');
        this.toastMsg("Se ha eliminado correctamente");
      });

  }

  toastMsg(msg: string): void {
    this._snackBar.open(msg, 'INFO', { duration: 2000, verticalPosition: 'top', horizontalPosition: 'right'});
  }

  checkChildren(){
  return this.router.children.length>0;
  }
}
