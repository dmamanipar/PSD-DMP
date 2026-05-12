import {Component, OnInit} from '@angular/core';
import {MaterialModule} from "../../../material/material.module";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {MarcaService} from "../../../servicio/marca.service";
import {Marca} from "../../../modelo/Marca";
import {switchMap} from "rxjs";

@Component({
  selector: 'app-form-marca',
  standalone: true,
  imports: [MaterialModule, ReactiveFormsModule, RouterLink],
  templateUrl: './form-marca.component.html',
  styleUrl: './form-marca.component.css'
})
export class FormMarcaComponent implements OnInit{
  form!: FormGroup;
  id!: number;
  isEdit!: boolean;

  constructor(
    private route: ActivatedRoute,
    private marcaService: MarcaService,
    private router: Router
  ){

  }
  ngOnInit(): void {
    this.form = new FormGroup({
      idMarca: new FormControl(0),
      nombre: new FormControl('', [Validators.required, Validators.minLength(3), Validators.maxLength(70)]),
      /*lastName: new FormControl('', [Validators.required, Validators.minLength(3), Validators.maxLength(70)]),
      dni: new FormControl('', Validators.required),
      address: new FormControl('', Validators.required),
      phone: new FormControl('', Validators.pattern('[0-9]+')),
      email: new FormControl('', Validators.email),*/
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
      this.marcaService.findById(this.id).subscribe(data => {
        this.form = new FormGroup({
          idMarca: new FormControl(data.idMarca),
          nombre: new FormControl(data.nombre, [Validators.required, Validators.minLength(3), Validators.maxLength(70)]),
         /* lastName: new FormControl(data.lastName, Validators.required),
          dni: new FormControl(data.dni, Validators.required),
          address: new FormControl(data.address, Validators.required),
          phone: new FormControl(data.phone, Validators.pattern('[0-9]+')),
          email: new FormControl(data.email, Validators.email),*/
        });
      });
    }
  }

  operate(){
    const patient: Marca = new Marca(this.form.value['idMarca'],this.form.value['nombre'] );
    //patient.idMarca = this.form.value['idMarca'];
    //patient.nombre = this.form.value['nombre'];
    /*patient.lastName = this.form.value['lastName'];
    patient.dni = this.form.value['dni'];
    patient.phone = this.form.value['phone'];
    patient.address = this.form.value['address'];
    patient.email = this.form.value['email'];*/

    if(this.isEdit){
      //UPDATE
      //PRACTICA COMUN PERO NO IDEAL
      this.marcaService.update(this.id, patient).subscribe( ()=> {
        this.marcaService.findAll();
        this.marcaService.marcas$.subscribe(data => {
          //this.marcaService.seleccionarMarca(data);
          //this.marcaService.setPatientChange(data);
          this.marcaService.setMessageChange('UPDATED!');
        });
      });
    }else{
      //INSERT
      this.marcaService.save(patient)
        .pipe(switchMap( () => {
          return this.marcaService.marcas$;
        }))
        .subscribe(data => {
          //this.marcaService.setPatientChange(data);
          this.marcaService.setMessageChange('CREATED!');
        });
    }

    this.router.navigate(['pages/marca']);
  }

  get f(){
    return this.form.controls;
  }

  checkFirstName(): boolean{
    let firstName: string = this.form.value['firstName'];
    return firstName.length === 0;
  }

}
