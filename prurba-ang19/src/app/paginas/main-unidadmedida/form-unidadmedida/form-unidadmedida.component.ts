import {Component, OnInit} from '@angular/core';
import {MaterialModule} from "../../../material/material.module";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgIf} from "@angular/common";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {MarcaService} from "../../../servicio/marca.service";

import {Marca} from "../../../modelo/Marca";
import {switchMap} from "rxjs";
import {UnidadMedida} from "../../../modelo/UnidadMedida";
import {MatSnackBar} from "@angular/material/snack-bar";
import {UnidadmedidaService} from "../../../servicio/unidadmedida.service";

@Component({
  selector: 'app-form-unidadmedida',
  standalone: true,
  imports: [MaterialModule, FormsModule,  ReactiveFormsModule, RouterLink],
  templateUrl: './form-unidadmedida.component.html',
  styleUrls: ['./form-unidadmedida.component.css']
})
export class FormUnidadmedidaComponent implements OnInit{

  form!: FormGroup;
  id!: number;
  isEdit!: boolean;

  constructor(
    private route: ActivatedRoute,
    private unidadMedidaService: UnidadmedidaService,
    private router: Router,
    private _snackBar: MatSnackBar
  ){

  }
  ngOnInit(): void {
    this.form = new FormGroup({
      idUnidad: new FormControl(0),
      nombreMedida: new FormControl('', [Validators.required, Validators.minLength(3), Validators.maxLength(70)])
    });

    this.route.params.subscribe(data => {
      this.id = data['id'];
      this.isEdit = data['id'] != null;
      this.initForm();
    });
  }

  initForm(){
    if(this.isEdit){
      //precargar los datos
      this.unidadMedidaService.findById(this.id).subscribe(data => {
        this.form = new FormGroup({
          idUnidad: new FormControl(data.idUnidad),
          nombreMedida: new FormControl(data.nombreMedida, [Validators.required, Validators.minLength(3), Validators.maxLength(70)]),
        });
      });
    }
  }


  get f(){
    return this.form.controls;
  }

  operate(){
    const um: UnidadMedida = new UnidadMedida(
      this.form.value['idUnidad'],
      this.form.value['nombreMedida'] );
    //um.idUnidad = this.form.value['idUnidad'];
    //um.nombreMedida = this.form.value['nombreMedida'];

    if(this.isEdit){
      this.unidadMedidaService.update(um.idUnidad, um)
        .pipe(switchMap( ()=> this.unidadMedidaService.findAll() ))
        .subscribe(data => {
          this.unidadMedidaService.setUnidadMedidaChange(data);
          this.unidadMedidaService.setMessageChange('UPDATED!');
          this.toastMsg("Se ha Modificado correctamente");
        });

    }else{
      this.unidadMedidaService.save(um)
        .pipe(switchMap( ()=> this.unidadMedidaService.findAll() ))
        .subscribe(data => {
          this.unidadMedidaService.setUnidadMedidaChange(data);
          this.unidadMedidaService.setMessageChange('CREATED!');
          this.toastMsg("Se ha Creado correctamente");
        });
    }

    this.router.navigate(['pages/unidadmedida']);
  }

  toastMsg(msg: string): void {
    this._snackBar.open(msg, 'INFO', { duration: 2000, verticalPosition: 'top', horizontalPosition: 'right'});
  }
}
