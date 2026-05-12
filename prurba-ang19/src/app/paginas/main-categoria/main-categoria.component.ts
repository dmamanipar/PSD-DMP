import {Component, OnInit, ViewChild} from '@angular/core';
import {MaterialModule} from "../../material/material.module";
import {Categoria} from "../../modelo/Categoria";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {CategoriaService} from "../../servicio/categoria.service";
import {MatDialog} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {switchMap} from "rxjs";
import {CategoriaDialogComponent} from "./categoria-dialog/categoria-dialog.component";

@Component({
  selector: 'app-main-categoria',
  standalone: true,
  imports: [MaterialModule],
  templateUrl: './main-categoria.component.html',
  styleUrl: './main-categoria.component.css'
})
export class MainCategoriaComponent implements OnInit{

  dataSource!: MatTableDataSource<Categoria>;

  columnsDefinitions = [
    { def: 'idCategoria', label: 'idCategoria', hide: true},
    { def: 'nombre', label: 'nombre', hide: false},
    { def: 'acciones', label: 'acciones', hide: false}
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private categoriaService: CategoriaService,
    private _dialog: MatDialog,
    private _snackBar: MatSnackBar
  ){}

  ngOnInit(): void {
    this.categoriaService.findAll().subscribe(data => this.createTable(data));

    this.categoriaService.getCategoriaChange().subscribe(data => this.createTable(data));
    this.categoriaService.getMessageChange().subscribe(data => this._snackBar.open(data, 'INFO', {duration: 2000}))
  }

  createTable(data: Categoria[]){
    this.dataSource = new MatTableDataSource(data);
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  getDisplayedColumns(){
    return this.columnsDefinitions.filter(cd => !cd.hide).map(cd => cd.def);
  }

  openDialog(categoria?: Categoria){
    this._dialog.open(CategoriaDialogComponent, {
      width: '750px',
      data: categoria,
      disableClose: true
    });
  }


  delete(idMedic: number){
    this.categoriaService.delete(idMedic)
      .pipe(switchMap( ()=> this.categoriaService.findAll()))
      .subscribe(data => {
        this.categoriaService.setCategoriaChange(data);
        this.categoriaService.setMessageChange('DELETED!');
      });
  }


}
