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
import {ConfigureComponent} from "./components/configure-component/configure.component";
import {RegisterComponent} from "./components/register/register.component";
import {SequenceAnalysisComponent} from "./components/entry/sequence-analysis/sequence-analysis.component";
import {GeneralInformationComponent} from "./components/entry/general-information/general-information.component";
import {ExperimentsComponent} from "./components/entry/experiments/experiments.component";
import {SamplesComponent} from "./components/entry/samples/samples.component";
import {HistoryComponent} from "./components/entry/history/history.component";

const routes: Routes = [
    {path: '', redirectTo: '/collection/personal', pathMatch: 'full'},
    {
        path: 'collection/:name', component: CollectionComponent, canActivate: [AuthGuard],
        children: [{
            path: 'folder/:id', component: FolderComponent
        }]
    },
    {path: 'upload/:type', component: UploadComponent},
    {path: 'configure', component: ConfigureComponent},
    {
        path: 'entry/:id', component: EntryDetailComponent, resolve: {entry: EntryResolver},
        children: [
            {
                path: '', redirectTo: 'general', pathMatch: 'full'
            },
            {
                path: 'samples', component: SamplesComponent
            },
            {
                path: 'experiments', component: ExperimentsComponent
            },
            {
                path: 'general', component: GeneralInformationComponent
            },
            {
                path: 'history', component: HistoryComponent
            },
            {
                path: 'sequences', component: SequenceAnalysisComponent
            }]
    },
    {path: 'login', component: LoginComponent},
    {path: 'profile/:id', component: ProfileComponent},
    {path: 'create/:type', component: CreateNewEntryComponent},
    {path: 'register', component: RegisterComponent}
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {onSameUrlNavigation: 'reload'})],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
