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
import {ProfileResolver} from "./resolvers/profile.resolver";
import {GeneralComponent} from "./components/profile/general/general.component";
import {ProfileGroupsComponent} from "./components/profile/profile-groups/profile-groups.component";
import {ProfileKeysComponent} from "./components/profile/profile-keys/profile-keys.component";
import {AdminComponent} from "./components/admin/admin/admin.component";
import {AdminGeneralComponent} from "./components/admin/admin-general/admin-general.component";
import {PageNotFoundComponent} from "./components/page-not-found/page-not-found.component";
import {AdminUsersComponent} from "./components/admin/admin-users/admin-users.component";
import {
    AdminWebOfRegistriesComponent
} from "./components/admin/admin-web-of-registries/admin-web-of-registries.component";
import {AdminSampleRequestsComponent} from "./components/admin/admin-sample-requests/admin-sample-requests.component";
import {AdminPublicGroupsComponent} from "./components/admin/admin-public-groups/admin-public-groups.component";

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
    {
        path: 'profile/:id', component: ProfileComponent, resolve: {profile: ProfileResolver},
        children: [
            {path: '', redirectTo: 'general', pathMatch: 'full'},
            {path: 'general', component: GeneralComponent},
            {path: 'groups', component: ProfileGroupsComponent},
            {path: 'keys', component: ProfileKeysComponent},
        ]
    },
    {
        path: 'admin', component: AdminComponent, // todo: guard
        children: [
            {path: '', redirectTo: 'general', pathMatch: 'full'},
            {path: 'general', component: AdminGeneralComponent},
            {path: 'users', component: AdminUsersComponent},
            {path: 'web', component: AdminWebOfRegistriesComponent},
            {path: 'samples', component: AdminSampleRequestsComponent},
            {path: 'groups', component: AdminPublicGroupsComponent}
        ]
    }, // todo resolver
    {path: 'create/:type', component: CreateNewEntryComponent},
    {path: 'register', component: RegisterComponent},
    {path: '**', component: PageNotFoundComponent}
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {onSameUrlNavigation: 'reload'})],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
