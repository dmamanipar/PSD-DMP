import { Component } from '@angular/core';
import { MaterialModule } from '../../material/material.module';
import { AuthService } from '../../servicio/auth.service';
import { Router, RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [MaterialModule, ReactiveFormsModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  user!: string;
  clave!: string;
  backgroundImageUrl = `${environment.baseHref}wallpaper.jpg`;
  constructor(
  private authService: AuthService,
  private router: Router
  ){ }

  login(){
  this.authService.login(this.user, this.clave).subscribe(data => {
    console.log("DMP")
    console.log(data);
    sessionStorage.setItem(environment.TOKEN_NAME, data.token);
    sessionStorage.setItem(environment.DATA_USERLOGIN, data.idUsuario.toString());
    //let token: string = sessionStorage.getItem(environment.TOKEN_NAME);
    this.router.navigate(['/pages/dashboard']);
  });
  }

}
