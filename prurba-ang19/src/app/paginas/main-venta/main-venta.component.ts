import {Component, OnInit, ViewChild} from '@angular/core';
import {MaterialModule} from "../../material/material.module";
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {ProductoRepor} from "../../modelo/ProductoRepor";
import {debounceTime, distinctUntilChanged, map, Observable, switchMap} from "rxjs";
import {ProductoService} from "../../servicio/producto.service";
import {AsyncPipe, NgIf} from "@angular/common";
import {ClienteService} from '../../servicio/cliente.service';
import {Cliente} from '../../modelo/Cliente';
import {startWith} from 'rxjs/operators';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
import {VentcarritoService} from '../../servicio/ventcarrito.service';
import {VentCarrito, VentCarritosCA} from '../../modelo/VentCarrito';
import {MatSnackBar} from '@angular/material/snack-bar';
import {environment} from '../../../environments/environment.development';
import { format } from "date-fns";
import {VentaCA} from '../../modelo/Venta';
import {VentaService} from '../../servicio/venta.service';


@Component({
  selector: 'app-main-venta',
  standalone: true,
  imports: [MaterialModule, ReactiveFormsModule, AsyncPipe, NgIf/*,  RouterLink */],
  templateUrl: './main-venta.component.html',
  styleUrl: './main-venta.component.css'
})
export class MainVentaComponent implements OnInit{

  salesFormGroup!: FormGroup;

  producControl= new FormControl<string| ProductoRepor>('');
  clientControl = new FormControl<string | Cliente>('');


  products!: ProductoRepor[];
  clientes: Cliente[] = [];

  productFiltered$!: Observable<ProductoRepor[]>;

  clientFiltered$!: Observable<Cliente[]>;
  //Ini Parte 2
  dataSource!: MatTableDataSource<VentCarrito>;
  columnsDefinitions = [
    { def: 'idCarrito', label: 'idCarrito', hide: true},
    { def: 'dniruc', label: 'dniruc', hide: true},
    { def: 'producto', label: 'producto', hide: true},
    { def: 'nombreProducto', label: 'nombreProducto', hide: false},
    { def: 'cantidad', label: 'cantidad', hide: false},
    { def: 'punitario', label: 'punitario', hide: false},
    { def: 'ptotal', label: 'ptotal', hide: false},
    { def: 'usuario', label: 'usuario', hide: true},
    { def: 'actions', label: 'actions', hide: false}
  ];
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  isButtonDisabled: boolean = true;
  isButtonDisabledReg: boolean = true;
  //Fin Parte 2

  constructor(
    private formBuilder: FormBuilder,
    private productService: ProductoService,
    private clienteService: ClienteService,
    private ventcarritoService: VentcarritoService,
    private ventaService: VentaService,
    private _snackBar: MatSnackBar
  ) {}


  showName(val: any) { return val ? val.nombre : val;}
  showClientName(val: Cliente | string): string {
    return typeof val === 'string' ? val : val?.nombres ?? '';
  }


  ngOnInit(): void {
    this.salesFormGroup = this.formBuilder.group({

      idProducto: [null],
      stock:[null],
      cantidad:[1],
      pu: [null],
      ptotal:[0],

      dniruc: [null, Validators.required],
      direccion: [null, Validators.required],

      bimponble: [0, Validators.required],
      igv: [0, Validators.required],
      descuento: [0, Validators.required],
      importet: [0, Validators.required],
    });

    //Producto
    this.productFiltered$ = this.producControl.valueChanges.pipe(
      map((val) => this.filterProduct(val))
    );
    this.loadInitialData();

    //Cliente
    this.clientFiltered$ = this.clientControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      startWith(''),
      map((val) => this.filterCliente(val!))
    );
    this.loadInitialDataCliente();


    this.salesFormGroup.get('cantidad')?.valueChanges.subscribe(value => {
      this.calcularPrecioTotal();
    });
    this.salesFormGroup.get('pu')?.valueChanges.subscribe(value => {
      this.calcularPrecioTotal();
    });

  }

  resetFormPart(){
    this.salesFormGroup.get('idProducto')?.setValue(null, { emitEvent: false });
    this.salesFormGroup.get('stock')?.setValue(null, { emitEvent: false });
    this.salesFormGroup.get('cantidad')?.setValue(1, { emitEvent: false });
    this.salesFormGroup.get('pu')?.setValue(null, { emitEvent: false });
    this.salesFormGroup.get('ptotal')?.setValue(0, { emitEvent: false });
  }

  calcularPrecioTotal() {
    const cantidad = this.salesFormGroup.get('cantidad')?.value || 0;
    const precioUnitario = this.salesFormGroup.get('pu')?.value || 0;
    const precioTotal = cantidad * precioUnitario;
    this.salesFormGroup.get('ptotal')?.setValue(precioTotal, { emitEvent: false });
    this.validateForm();
  }


  loadInitialData() {
    this.productService.findAll();
    this.productService.getProductosSubject().subscribe((data) => (this.products=data));
  }

  private loadInitialDataCliente(): void {
    this.clienteService.findAll().subscribe(data => {
      this.clienteService.setClientesChange(data);
    });
    this.clienteService.getClientesChange().subscribe(data => {
      this.clientes = data;
    });
  }

  filterProduct(val: any) {
    this.salesFormGroup.get('pu')?.setValue(0);
    this.salesFormGroup.get('idProducto')?.setValue(0);
    if (val?.idProducto> 0) {
      return this.products.filter(
        (el) =>
          el.nombre.toLowerCase().includes(val.nombre.toLowerCase())
      );
    } else {
      return this.products.filter(
        (el) =>
          el.nombre.toLowerCase().includes(val?.toLowerCase())
      );
    }
  }

  private filterCliente(val: string | Cliente): Cliente[] {
    const searchTerm = typeof val === 'string' ? val.toLowerCase() : val.nombres.toLowerCase();

    return this.clientes.filter(cliente =>
      cliente.nombres.toLowerCase().includes(searchTerm)
    );
  }

  onProductSelected(event: any) {
    const selectedProduct = event.option.value;
    this.salesFormGroup.get('pu')?.setValue(selectedProduct.pu);
    this.salesFormGroup.get('idProducto')?.setValue(selectedProduct.idProducto);
    this.salesFormGroup.get('stock')?.setValue(selectedProduct.stock);
    this.validateForm();
  }

  onClientSelected(event: any): void {
    const selectedClient = event.option.value as Cliente;
    this.salesFormGroup.patchValue({ dniruc: selectedClient.dniruc});
    this.salesFormGroup.patchValue({ direccion: selectedClient.direccion});

    this.ventcarritoService.listarCarritoCliente(selectedClient.dniruc).subscribe(data => {
      this.createTable(data);
      this.validateFormReg();
    });

    this.ventcarritoService.getEntidadChange().subscribe(data => this.createTable(data));
    this.ventcarritoService.getMessageChange().subscribe(data => this._snackBar.open(data, 'INFO', {duration: 1000}));

    if(this.dataSource.data.length<1){
      this.resetFormPart();
      this.salesFormGroup.get('cantidad')?.setValue(0, { emitEvent: false });
    }
    this.validateForm();
    if(this.dataSource.data.length>=1){
      console.log("Entro Aqui");
      this.validateFormReg();
    }

  }

  createTable(data: VentCarrito[]){
    this.dataSource = new MatTableDataSource(data);
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    this.calcularBIPT(data);

  }


  calcularBIPT(data: VentCarrito[]){
    const total = data.reduce((sum, item) => sum + item.ptotal, 0);
    const baseImponible = total / 1.18; // Excluir IGV
    const igv = total - baseImponible;

    // Actualizar valores en el formulario
    this.salesFormGroup.get('bimponble')?.setValue(baseImponible.toFixed(2), { emitEvent: false });
    this.salesFormGroup.get('igv')?.setValue(igv.toFixed(2), { emitEvent: false });
    this.salesFormGroup.get('importet')?.setValue(total.toFixed(2), { emitEvent: false });
  }

  onNewClient(event: Event) {
    event.preventDefault();
    console.log("Implementar el agregar un nuevo Cliente");
    // Aquí puedes agregar la lógica para abrir el formulario de nuevo cliente
  }

  onAddCarrito(event: Event) {
    event.preventDefault();
    if (this.salesFormGroup.valid) {
     let id: number= Number(sessionStorage.getItem(environment.DATA_USERLOGIN));

      const dtoForm: VentCarritosCA = new VentCarritosCA(
        0,
        this.salesFormGroup.get('dniruc')?.value || '',
        (this.producControl.value as ProductoRepor).idProducto,
        (this.producControl.value as ProductoRepor).nombre,
        this.salesFormGroup.get('cantidad')?.value || 0,
        this.salesFormGroup.get('pu')?.value || 0,
        this.salesFormGroup.get('ptotal')?.value || 0,
        1,
        id
      );
      this.ventcarritoService.saveD(dtoForm)
        .pipe(switchMap( ()=> this.ventcarritoService.listarCarritoCliente(this.salesFormGroup.get('dniruc')?.value || '') ))
        .subscribe(data => {
          this.ventcarritoService.setEntidadChange(data);
          this.ventcarritoService.setMessageChange('CREATED!');
          this.validateFormReg();
          //this.toastMsg("Se ha agregado satisfactoriamente", 'custom-snackbar');
      });

    }else{
      this.toastMsg("Quiza no esta seleccionado un determinado cliente", 'custom-snackbar');
    }
  }

  operate() {
    const fechaFormateada = format(new Date(), "yyyy-MM-dd HH:mm:ss");
    console.log(fechaFormateada); // Salida: "2025-03-10 14:30:45"
    if (this.salesFormGroup.valid) {
      let id: number= Number(sessionStorage.getItem(environment.DATA_USERLOGIN));

      const dtoForm: VentaCA = new VentaCA(
        0,
        this.salesFormGroup.get('bimponble')?.value || 0,
        this.salesFormGroup.get('igv')?.value || 0,
        this.salesFormGroup.get('importet')?.value || 0,
        (this.clientControl.value as Cliente).dniruc,
        id,
        "001",
        fechaFormateada,
        "F-01",
        "Factura"
      );
      this.ventaService.saveD(dtoForm)
        .pipe(switchMap( ()=> this.ventcarritoService.listarCarritoCliente(this.salesFormGroup.get('dniruc')?.value || '') ))
        .subscribe(data => {
          this.ventcarritoService.setEntidadChange(data);
          this.ventcarritoService.setMessageChange('Registrado venta de forma exitosa!');
          this.validateFormReg();
          this.resetFormPart();
          this.validateForm();
        });
    }else{
      this.toastMsg("El formulario de ventas no cumple los requisitos", 'custom-snackbar');
    }
  }
 //parte 2
  getDisplayedColumns(){
    return this.columnsDefinitions.filter(cd => !cd.hide).map(cd => cd.def);
  }

  delete(id: number){
    this.ventcarritoService.delete(id)
      .pipe(switchMap( ()=>this.ventcarritoService.listarCarritoCliente(this.salesFormGroup.get('dniruc')?.value || '')))
      .subscribe(data => {
        this.ventcarritoService.setEntidadChange(data);
        this.ventcarritoService.setMessageChange('DELETED!');
        this.validateFormReg();
      });
  }

  /** Método para validar si el botón Agregar debe habilitarse */
  validateForm() {
    const ptotal = this.salesFormGroup.get('ptotal')?.value;
    this.isButtonDisabled = !ptotal || ptotal <= 0;
  }

  validateFormReg() {
    const importet = this.salesFormGroup.get('importet')?.value;
    this.isButtonDisabledReg = !importet || importet <= 0;
  }

  toastMsg(msg: string, _classStyle:string): void {
    this._snackBar.open(msg, 'INFO', { duration: 2000, verticalPosition: 'top', horizontalPosition: 'right', panelClass: ['custom-snackbar']});
  }
}
