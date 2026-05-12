import {Component, OnInit, ViewChild} from '@angular/core';
import {MaterialModule} from "../../material/material.module";
import {RouterLink, RouterOutlet} from "@angular/router";
import {MatTableDataSource} from "@angular/material/table";
import {Marca} from "../../modelo/Marca";
import {MatSort} from "@angular/material/sort";
import {MatPaginator} from "@angular/material/paginator";
import {MarcaService} from "../../servicio/marca.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {switchMap} from "rxjs";

@Component({
  selector: 'app-main-marca',
  standalone: true,
  imports: [MaterialModule, RouterOutlet, RouterLink],
  templateUrl: './main-marca.component.html',
  styleUrl: './main-marca.component.css'
})
export class MainMarcaComponent implements OnInit {
  dataSource!: MatTableDataSource<Marca>;
  columnsDefinitions = [
    { def: 'idMarca', label: 'idMarca', hide: true},
    { def: 'nombre', label: 'nombre', hide: false},
    { def: 'lastName', label: 'idPatilastNameent', hide: false},
    { def: 'dni', label: 'dni', hide: false},
    { def: 'actions', label: 'actions', hide: false}
  ];

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private marcaService: MarcaService,
    private _snackBar: MatSnackBar
  ){}

  ngOnInit(): void {
    this.marcaService.findAll();
    this.marcaService.marcas$.subscribe(data=>{
      //this.productos=data;
      this.dataSource = new MatTableDataSource(data);
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
      this.createTable(data);
      //this._snackBar.open(data, 'INFO', { duration: 2000, verticalPosition: 'top', horizontalPosition: 'right'});
    });
  }


  createTable(data: Marca[]){
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

  delete(idPatient: number){
    this.marcaService.delete(idPatient)
      .pipe(switchMap( ()=> this.marcaService.marcas$ ))
      .subscribe(data => {
        //this.marcaService.setPatientChange(data);
        this.marcaService.setMessageChange('DELETED!');
      });
  }
}
