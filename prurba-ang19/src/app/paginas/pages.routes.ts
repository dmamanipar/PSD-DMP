import { Routes } from '@angular/router';
import {FormProductoComponent} from "./form-producto/form-producto.component";
import {ListaProductoComponent} from "./lista-producto/lista-producto.component";
import {MainCategoriaComponent} from "./main-categoria/main-categoria.component";
import {MainMarcaComponent} from "./main-marca/main-marca.component";
import {FormMarcaComponent} from "./main-marca/form-marca/form-marca.component";
import {MainUnidadmedidaComponent} from "./main-unidadmedida/main-unidadmedida.component";
import {FormUnidadmedidaComponent} from "./main-unidadmedida/form-unidadmedida/form-unidadmedida.component";
import {MainVentaComponent} from "./main-venta/main-venta.component";
import {MainReportesComponent} from "./main-reportes/main-reportes.component";
import {FormxProductoComponent} from "./main-producto/formx-producto/formx-producto.component";
import {MainProductoComponent} from "./main-producto/main-producto.component";
import { DashboardComponent } from './dashboard/dashboard.component';
import { Not403Component } from './not403/not403.component';
import { certGuard } from '../guard/cert.guard';

export const pagesRoutes: Routes = [
  { path: 'dashboard', component: DashboardComponent, canActivate:[certGuard] },
  {
    path: 'product',
    component: ListaProductoComponent,
    children: [
      { path: 'new', component: FormProductoComponent },
      { path: 'edit/:id', component: FormProductoComponent },
    ],  canActivate:[certGuard]
  },
  { path: 'categoria', component: MainCategoriaComponent , canActivate:[certGuard]},
  //{ path: 'categoria', component: MainCategoriaComponent },
  {
    path: 'marca',
    component: MainMarcaComponent,
    children: [
      { path: 'new', component: FormMarcaComponent },
      { path: 'edit/:id', component: FormMarcaComponent },
    ],canActivate:[certGuard]
  },
  {
    path: 'unidadmedida',
    component: MainUnidadmedidaComponent,
    children: [
      { path: 'new', component: FormUnidadmedidaComponent },
      { path: 'edit/:id', component: FormUnidadmedidaComponent },
    ], canActivate:[certGuard]
  },
  {
    path: 'venta',component:MainVentaComponent , canActivate:[certGuard]
  },
  {
    path: 'reporte',component:MainReportesComponent , canActivate:[certGuard]
  },

  {
    path: 'productox',
    component: MainProductoComponent,
    children: [
      { path: 'new', component: FormxProductoComponent },
      { path: 'edit/:id', component: FormxProductoComponent },
    ], canActivate:[certGuard]
  },
  { path: 'not-403', component: Not403Component},

];
