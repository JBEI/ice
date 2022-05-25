import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from "./components/login/login.component";
import {CollectionComponent} from "./components/main/collection.component";
import {EntryDetailComponent} from "./components/entry/entry-detail/entry-detail.component";
import {EntryResolver} from "./components/entry/entry.resolver";
import {UploadComponent} from "./components/entry/upload/upload.component";
import {AuthGuard} from "./guards/auth.guard";
import {FolderComponent} from "./components/collection/folder/folder.component";
import {ProfileComponent} from "./components/profile/profile/profile.component";
import {CreateNewEntryComponent} from "./components/entry/create-new-entry/create-new-entry.component";
import {EntryFieldsResolver} from "./components/entry/entry-fields.resolver";

const routes: Routes = [
    {path: '', redirectTo: '/collection/personal', pathMatch: 'full'},
    {
        path: 'collection/:name', component: CollectionComponent, canActivate: [AuthGuard],
        children: [{
            path: 'folder/:id', component: FolderComponent
        }]
    },
    {path: 'upload', component: UploadComponent},
    {path: 'entry/:id', component: EntryDetailComponent, resolve: {entry: EntryResolver}},
    {path: 'login', component: LoginComponent},
    {path: 'profile/:id', component: ProfileComponent},
    {path: 'create/:type', component: CreateNewEntryComponent, resolve: {fields: EntryFieldsResolver}}
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
