import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from "./components/login/login.component";
import {CollectionComponent} from "./components/main/collection.component";
import {EntryDetailComponent} from "./components/entry/entry-detail/entry-detail.component";
import {EntryResolver} from "./components/entry/entry.resolver";
import {UploadComponent} from "./components/entry/upload/upload.component";

const routes: Routes = [
    {path: '', redirectTo: '/collection/personal', pathMatch: 'full'},
    {path: 'collection/:name', component: CollectionComponent},
    {path: 'upload', component: UploadComponent},
    {path: 'entry/:id', component: EntryDetailComponent, resolve: {entry: EntryResolver}},
    {path: 'login', component: LoginComponent},
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
