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
import {EntryResolver} from "./components/entry/entry.resolver";
import {PartVisualizationComponent} from "./components/part-visualization/part-visualization.component";
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
import {EntryFieldsResolver} from "./components/entry/entry-fields.resolver";
import {DatePickerComponent} from "./components/date-picker/date-picker.component";
import {TextFieldComponent} from './components/entry/widgets/text-field/text-field.component';

// register Handsontable's modules
registerAllModules();

@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        CollectionComponent,
        HeaderComponent,
        EntryDetailComponent,
        PartVisualizationComponent,
        UploadComponent,
        MainSidebarMenuComponent,
        FooterComponent,
        FolderComponent,
        PersonalCollectionComponent,
        PartsTableComponent,
        ProfileComponent,
        CreateNewEntryComponent,
        DatePickerComponent,
        TextFieldComponent
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
        ReactiveFormsModule
    ],
    providers: [EntryResolver, EntryFieldsResolver],
    bootstrap: [AppComponent]
})
export class AppModule {
}
