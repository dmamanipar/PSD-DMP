import { Component, OnInit } from '@angular/core';
import {MaterialModule} from "../../material/material.module";
import {RouterLink, RouterLinkActive, RouterOutlet} from "@angular/router";
import { Acceso } from '../../modelo/Acceso';
import { AuthService } from '../../servicio/auth.service';
import { AccesoService } from '../../servicio/acceso.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [MaterialModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent implements OnInit{
  accesos!: Acceso[];
  constructor(
  private authService: AuthService,
 private accesoService: AccesoService,
  ){}

  logout(){
  this.authService.logout();
  }

 ngOnInit(): void {
 this.accesoService.getAccesosChange().subscribe(data => this.accesos = data);
 }

}
