import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MaterialModule} from "../../../material/material.module";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Categoria} from "../../../modelo/Categoria";
import {CategoriaService} from "../../../servicio/categoria.service";
import {switchMap} from "rxjs";
import {FormControl, FormGroup, FormsModule, NgForm, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-categoria-dialog',
  standalone: true,
  imports: [MaterialModule, FormsModule, ReactiveFormsModule],
  templateUrl: './categoria-dialog.component.html',
  styleUrl: './categoria-dialog.component.css'
})
export class CategoriaDialogComponent implements OnInit{
  @ViewChild('categoriaForm') categoriaForm!: NgForm ;
  form!: FormGroup;

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: Categoria,
    private categoriaService: CategoriaService,
    private _dialogRef: MatDialogRef<CategoriaDialogComponent>

  ){}
  ngOnInit(): void {
    if(this.data!==undefined){
      console.log(this.data['nombre']);

      this.form = new FormGroup({
        idCategoria: new FormControl(this.data['idCategoria']),
        nombre: new FormControl(this.data['nombre'], [Validators.required, Validators.minLength(3), Validators.maxLength(70)])
      });


    }else{
      this.form = new FormGroup({
        idCategoria: new FormControl(0),
        nombre: new FormControl('', [Validators.required, Validators.minLength(3), Validators.maxLength(70)])
      });
    }
  }

  close(){
    this._dialogRef.close();
  }

  operate(){
    const categoria: Categoria = new Categoria(this.form.value['idCategoria'], this.form.value['nombre']);
    //categoria.idCategoria = this.form.value['idCategoria'];
    //categoria.nombre = this.form.value['nombre'];

    if(this.categoriaForm.valid){
      if(categoria.idCategoria > 0){
        //UPDATE
        this.categoriaService.update(categoria.idCategoria, categoria)
          .pipe(switchMap( ()=> this.categoriaService.findAll() ))
          .subscribe(data => {
            this.categoriaService.setCategoriaChange(data);
            this.categoriaService.setMessageChange('UPDATED!');
            this.close();
          });

      }else{
        //INSERT
        this.categoriaService.save(categoria)
          .pipe(switchMap( ()=> this.categoriaService.findAll() ))
          .subscribe(data => {
            this.categoriaService.setCategoriaChange(data);
            this.categoriaService.setMessageChange('CREATED!');
            this.close();
          });
      }
    }else{
      console.log("Error....")
    }

  }

  get f(){
    return this.form.controls;
  }

}
