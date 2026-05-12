import {Component, OnInit, ViewChild} from '@angular/core';
import {ProductoService} from "../../servicio/producto.service";
import {ProductoRepor} from "../../modelo/ProductoRepor";
import {
  MatCell, MatCellDef,
  MatColumnDef,
  MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef, MatNoDataRow, MatRow, MatRowDef,
  MatTable,
  MatTableDataSource
} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {FormProductoComponent} from "../form-producto/form-producto.component";
import {MaterialModule} from "../../material/material.module";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-lista-producto',
  standalone: true,
  imports: [
    MaterialModule,
    MatFormField,
    MatTable,
    MatInput,
    MatHeaderCell,
    MatCell,
    MatSort,
    MatColumnDef,
    MatHeaderRow,
    MatRow,
    MatPaginator,
    MatNoDataRow,
    MatHeaderRowDef,
    MatRowDef,
    MatCellDef,
    MatHeaderCellDef,
    MatLabel,
    FormProductoComponent,
    MatSortModule,
    NgIf
  ],
  templateUrl: './lista-producto.component.html',
  styleUrl: './lista-producto.component.css'
})
export class ListaProductoComponent implements OnInit {
  productos:ProductoRepor[]=[];

  //displayedColumns: string[] = ['idProducto', 'nombre', 'pu', 'puOld', 'acciones'];
  columnsDefinitions = [
    { def: 'idProducto', label: 'idProducto', hide: true},
    { def: 'nombre', label: 'nombre', hide: false},
    { def: 'pu', label: 'pu', hide: false},
    { def: 'puOld', label: 'puOld', hide: false},
    { def: 'categoria', label: 'categoria', hide: false},
    { def: 'acciones', label: 'acciones', hide: false}
  ];

  dataSource!: MatTableDataSource<ProductoRepor>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  showComponent: boolean = false;
  toggleComponent(da:boolean) {
    this.showComponent = da;
  }

  constructor(private productoService: ProductoService) {
  }

  ngOnInit():void {
    //this.productoService.findAll().subscribe(productos => this.productos = productos);

    this.productoService.getProductosSubject().subscribe(data=>{
      //this.productos=data;
      this.dataSource = new MatTableDataSource(data);
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
    });
    this.productoService.findAll();

  }

  eliminar(id:number){
    if(confirm("Desea eliminar?")){
      this.productoService.delete(id).subscribe();
    }
  }

  cargarProducto(producto:ProductoRepor){
    this.productoService.seleccionarProducto(producto);
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

}
