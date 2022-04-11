import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LoginComponent} from "./components/login/login.component";
import {HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {MainComponent} from './components/main/main.component';
import {ContentsComponent} from './components/collection/contents/contents.component';
import {NgbDropdownModule, NgbNavModule, NgbPaginationModule, NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {PartsTableComponent} from './components/parts-table/parts-table.component';
import {PartDetailsComponent} from './components/parts/part-details/part-details.component';
import {PartDetailsResolver} from "./components/parts/part-details/part.details.resolver";
import {PartGeneralInformationComponent} from './components/parts/part-general-information/part-general-information.component';
import {SequenceComponent} from "./components/visualization/sequence/sequence.component";
import {HeaderComponent} from './components/header/header.component';
import {OptionsFieldComponent} from './components/parts/widgets/options-field/options-field.component';
import {WithEmailFieldComponent} from './components/parts/widgets/with-email-field/with-email-field.component';
import { AutoCompleteFieldComponent } from './components/parts/widgets/auto-complete-field/auto-complete-field.component';
import { SearchBarComponent } from './components/search/search-bar/search-bar.component';

@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        MainComponent,
        ContentsComponent,
        PartsTableComponent,
        PartDetailsComponent,
        PartGeneralInformationComponent,
        SequenceComponent,
        HeaderComponent,
        OptionsFieldComponent,
        WithEmailFieldComponent,
        AutoCompleteFieldComponent,
        SearchBarComponent
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        HttpClientModule,
        FormsModule,
        NgbTooltipModule,
        NgbPaginationModule,
        NgbDropdownModule,
        NgbNavModule
    ],
    providers: [PartDetailsResolver],
    bootstrap: [AppComponent]
})
export class AppModule {
}
