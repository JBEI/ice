import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LoginComponent} from './components/login/login.component';
import {CollectionComponent} from './components/main/collection.component';
import {HeaderComponent} from './components/header/header.component';
import {NgbDropdownModule, NgbModule, NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {EntryDetailComponent} from './components/entry/entry-detail/entry-detail.component';
import {UploadComponent} from './components/entry/upload/upload.component';
import {registerAllModules} from "handsontable/registry";
import {HotTableModule} from "@handsontable/angular";
import {MainSidebarMenuComponent} from './components/main-sidebar-menu/main-sidebar-menu.component';
import {FooterComponent} from './components/footer/footer.component';
import {FolderComponent} from "./components/collection/folder/folder.component";
import {PersonalCollectionComponent} from './components/collection/personal-collection/personal-collection.component';
import {PartsTableComponent} from "./components/parts-table/parts-table.component";
import {ProfileComponent} from './components/profile/profile/profile.component';
import {CreateNewEntryComponent} from './components/entry/create-new-entry/create-new-entry.component';
import {
    CreateNewFolderModalComponent
} from './components/modal/create-new-folder-modal/create-new-folder-modal.component';
import {
    SequenceVisualizationComponent
} from './components/entry/widgets/sequence-visualization/sequence-visualization.component';
import {FolderActionsComponent} from './components/folder-actions/folder-actions.component';
import {ConfigureComponent} from './components/configure-component/configure.component';
import {ShareFolderModalComponent} from './components/modal/share-folder-modal/share-folder-modal.component';
import {DateFieldComponent} from "./components/entry/widgets/date-field/date-field.component";
import {NgOptimizedImage} from "@angular/common";
import {ConfirmActionComponent} from "./components/shared/confirm-action/confirm-action.component";
import {UploadToFolderModalComponent} from './components/modal/upload-to-folder-modal/upload-to-folder-modal.component';
import {AddToFolderModalComponent} from './components/modal/add-to-folder-modal/add-to-folder-modal.component';
import {FilterPipe} from './filters/filter.pipe';
import {SequenceAnalysisComponent} from "./components/entry/sequence-analysis/sequence-analysis.component";
import {TextFieldComponent} from "./components/entry/widgets/text-field/text-field.component";
import {SelectFieldComponent} from "./components/entry/widgets/select-field/select-field.component";
import {
    UserWithEmailFieldComponent
} from "./components/entry/widgets/user-with-email-field/user-with-email-field.component";
import {TextareaFieldComponent} from "./components/entry/widgets/textarea-field/textarea-field.component";
import {BooleanFieldComponent} from "./components/entry/widgets/boolean-field/boolean-field.component";
import {MultiTextFieldComponent} from "./components/entry/widgets/multi-text-field/multi-text-field.component";
import {ExportEntriesComponent} from './components/entry/modal/export-entries/export-entries.component';
import {CreateSamplesComponent} from './components/entry/modal/create-samples/create-samples.component';
import {AdminComponent} from "./components/admin/admin/admin.component";
import {AdminUsersComponent} from './components/admin/admin-users/admin-users.component';
import {
    AdminWebOfRegistriesComponent
} from './components/admin/admin-web-of-registries/admin-web-of-registries.component';
import {AdminSampleRequestsComponent} from './components/admin/admin-sample-requests/admin-sample-requests.component';
import {AdminPublicGroupsComponent} from './components/admin/admin-public-groups/admin-public-groups.component';
import {AdminCustomFieldsComponent} from './components/admin/admin-custom-fields/admin-custom-fields.component';
import {
    EditCustomFieldModalComponent
} from './components/modal/edit-custom-field-modal/edit-custom-field-modal.component';

// register Handsontable's modules
registerAllModules();

// import {
//     registerCellType,
//     NumericCellType,
// } from 'handsontable/cellTypes';
//
// import {
//     registerPlugin,
//     UndoRedo,
// } from 'handsontable/plugins';
//
// registerCellType(NumericCellType);
// registerPlugin(UndoRedo);

@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        CollectionComponent,
        HeaderComponent,
        EntryDetailComponent,
        UploadComponent,
        MainSidebarMenuComponent,
        FooterComponent,
        FolderComponent,
        PersonalCollectionComponent,
        PartsTableComponent,
        ProfileComponent,
        CreateNewEntryComponent,
        CreateNewFolderModalComponent,
        SequenceVisualizationComponent,
        FolderActionsComponent,
        ConfigureComponent,
        ShareFolderModalComponent,
        DateFieldComponent,
        ConfirmActionComponent,
        UploadToFolderModalComponent,
        AddToFolderModalComponent,
        FilterPipe,
        ExportEntriesComponent,
        CreateSamplesComponent,
        AdminComponent,
        AdminUsersComponent,
        AdminWebOfRegistriesComponent,
        AdminSampleRequestsComponent,
        AdminPublicGroupsComponent,
        AdminCustomFieldsComponent,
        EditCustomFieldModalComponent,
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        NgbDropdownModule,
        FormsModule,
        NgbModule,
        NgbNavModule,
        HttpClientModule,
        HotTableModule,
        ReactiveFormsModule,
        NgOptimizedImage,
        SequenceAnalysisComponent,
        TextFieldComponent,
        SelectFieldComponent,
        UserWithEmailFieldComponent,
        TextareaFieldComponent,
        BooleanFieldComponent,
        MultiTextFieldComponent,
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
