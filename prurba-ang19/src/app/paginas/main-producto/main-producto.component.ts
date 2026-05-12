import {Component, OnInit, ViewChild} from '@angular/core';
import {MaterialModule} from "../../material/material.module";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {ProductoRepor} from "../../modelo/ProductoRepor";
import {MatTableDataSource} from "@angular/material/table";
import {ProductoService} from "../../servicio/producto.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RouterLink, RouterOutlet} from "@angular/router";

@Component({
  selector: 'app-main-producto',
  standalone: true,
  imports: [
    MaterialModule,
    RouterOutlet,
    RouterLink
  ],
  templateUrl: './main-producto.component.html',
  styleUrl: './main-producto.component.css'
})
export class MainProductoComponent implements OnInit{
  columnsDefinitions = [
    { def: 'idProducto', label: 'idProducto', hide: true},
    { def: 'nombre', label: 'nombre', hide: false},
    { def: 'pu', label: 'pu', hide: false},
    { def: 'puOld', label: 'puOld', hide: false},
    { def: 'categoria', label: 'categoria', hide: false},
    { def: 'acciones', label: 'acciones', hide: false}
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  totalElements: number = 0;
  dataSource!: MatTableDataSource<ProductoRepor>;
  constructor(private productoService: ProductoService, private _snackBar: MatSnackBar) {}
  ngOnInit(): void {
    this.productoService.listPageable(0, 2).subscribe(data=>this.createTable(data));
  }
  createTable(data: any){
    this.totalElements = data.totalElements;
    this.dataSource = new MatTableDataSource(data.content);
    this.dataSource.sort = this.sort;
  }
  showMore(e : any){
    this.productoService.listPageable(e.pageIndex, e.pageSize).subscribe(data => this.createTable(data));
  }
  eliminar(id:number){
    if(confirm("Desea eliminar?")){
      this.productoService.delete(id).subscribe(data=>{
        this.toastMsg("Se ha elimidado correctamente!");
      });
    }
  }
  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }
  getDisplayedColumns(){
    return this.columnsDefinitions.filter(cd => !cd.hide).map(cd => cd.def);
  }
  toastMsg(msg: string): void {
    this._snackBar.open(msg, 'INFO', { duration: 2000, verticalPosition: 'top', horizontalPosition: 'right'});
  }

}
