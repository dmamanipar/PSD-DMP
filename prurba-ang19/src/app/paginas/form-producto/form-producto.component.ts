import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {Categoria} from "../../modelo/Categoria";
import {Marca} from "../../modelo/Marca";
import {ProductoService} from "../../servicio/producto.service";
import {Producto} from "../../modelo/Producto";
import {AsyncPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {ProductoRepor} from "../../modelo/ProductoRepor";
import {CategoriaService} from "../../servicio/categoria.service";
import {MarcaService} from "../../servicio/marca.service";
import {MatTableDataSource} from "@angular/material/table";
import {MaterialModule} from "../../material/material.module";
import {MatOption, MatSelect} from "@angular/material/select";
import {MatCard, MatCardHeader, MatCardTitleGroup} from "@angular/material/card";
import {RouterLink} from "@angular/router";
import {Observable} from "rxjs";
import {UnidadMedida} from "../../modelo/UnidadMedida";
import {UnidadmedidaService} from "../../servicio/unidadmedida.service";

@Component({
  selector: 'app-form-producto',
  standalone: true,
  imports: [
    ReactiveFormsModule, NgForOf, NgIf, MaterialModule, MatSelect, MatOption, MatCard, MatCardHeader, AsyncPipe
  ],
  templateUrl: './form-producto.component.html',
  styleUrl: './form-producto.component.css'
})
export class FormProductoComponent implements OnInit {
  productForm!: FormGroup;
  productSaved = false;

  categorias: Categoria[] = [];
  marcas: Marca[] = [];


  unidadMedidas$!:Observable<UnidadMedida[]>;


  productoSeleccionado:ProductoRepor|null = null;

  fbr: FormBuilder=new FormBuilder();

  constructor(private serviceProducto:ProductoService, private sevicioMarca: MarcaService,
              private sevicioCategoria: CategoriaService,
              private serviceUnitMed: UnidadmedidaService,
              private fb: FormBuilder) {
    this.fbr = this.fb
    this.formInial(this.fbr);
  }
  formInial(fb: FormBuilder){
    this.productForm = fb.group({
      idProducto: [null],
      nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(70)]],
      pu: [0, Validators.required],
      puOld: [0, Validators.required],
      utilidad: [0, Validators.required],
      stock: [0, Validators.required],
      stockOld: [0, Validators.required],
      categoria: [null, Validators.required],
      marca: [null, Validators.required],
      unidadMedida: [null, Validators.required]
    });
  }
  ngOnInit(): void {
    this.serviceProducto.productoSeleccionado$.subscribe(producto => {
      this.productoSeleccionado = producto;
      if(producto){
        this.productForm.patchValue(producto);
        this.productForm.patchValue({
          categoria: producto.categoria.idCategoria,
          marca: producto.marca.idMarca,
          unidadMedida: producto.unidadMedida.idUnidad
        });
      }
    });


    this.sevicioMarca.findAll();
    this.sevicioMarca.marcas$.subscribe(data=>{
      this.marcas=data;
    });


    this.sevicioCategoria.findAll().subscribe(data=>{
      this.sevicioCategoria.setCategoriaChange(data);
    });
    this.sevicioCategoria.getCategoriaChange().subscribe(data=>{
      this.categorias=data;
    });


    this.unidadMedidas$=this.serviceUnitMed.findAll();


  }


  saveProduct() {
    console.log(this.productoSeleccionado);
    if(this.productoSeleccionado!==null){
      const productoActualizado: Producto = {
        ...this.productoSeleccionado,
        ...this.productForm.value,
      };
      console.log(productoActualizado);
      this.serviceProducto.update(productoActualizado.idProducto, productoActualizado).subscribe(() => {
        this.productoSeleccionado = null;
        this.productForm.reset();
        this.formInial(this.fbr);
//this.productForm.clearValidators();
      });
    }else{
      if (this.productForm.valid) {
        console.log(this.productForm.value);
        const product: Producto = new Producto(
        this.productForm.value['idProducto'],
        this.productForm.value['nombre'],
        this.productForm.value['pu'],
        this.productForm.value['puOld'],
        this.productForm.value['utilidad'],
        this.productForm.value['stock'],
        this.productForm.value['stockOld'],
        this.productForm.value['categoria'],
        this.productForm.value['marca'],
        this.productForm.value['unidadMedida']
      );
        /*product.idProducto = this.productForm.value['idProducto'],
          product.nombre = this.productForm.value['nombre'],
          product.pu = this.productForm.value['pu'],
          product.puOld = this.productForm.value['puOld'],
          product.utilidad = this.productForm.value['utilidad'],
          product.stock = this.productForm.value['stock'],
          product.stockOld = this.productForm.value['stockOld'],
          product.categoria = this.productForm.value['categoria'],
          product.marca = this.productForm.value['marca'],
          product.unidadMedida = this.productForm.value['unidadMedida']  */
        console.log(product);
        this.serviceProducto.save(product).subscribe({
          next: (response) => {
            console.log('Producto guardado exitosamente', response);
          },
          error: (error) => {
            console.error('Error al guardar el producto', error);
          }
        });
        this.productSaved = true;
        this.formInial(this.fbr);
        setTimeout(() => this.productSaved = false, 3000);
      }else{
        console.log("Manda Error!")
        this.productForm.markAllAsTouched();
      }
    }
  }

  resetButton(){
    this.productoSeleccionado = null;
    this.formInial(this.fbr);
  }
}
