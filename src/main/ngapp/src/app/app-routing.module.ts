import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from "./components/login/login.component";
import {MainComponent} from "./components/main/main.component";
import {PartDetailsComponent} from "./components/parts/part-details/part-details.component";
import {PartDetailsResolver} from "./components/parts/part-details/part.details.resolver";

const routes: Routes = [
    {path: '', component: MainComponent},
    {path: 'login', component: LoginComponent},
    {path: 'entry/:partId', component: PartDetailsComponent, resolve: {partDetails: PartDetailsResolver}}
    // {path: 'register'},
    // {path: 'forgot-password'},
    // {path: ''},
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
