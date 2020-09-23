import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from "./components/login/login.component";

const routes: Routes = [
    // {path: ''},
    {path: 'login', component: LoginComponent},
    // {path: 'register'},
    // {path: 'forgot-password'},
    // {path: ''},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
