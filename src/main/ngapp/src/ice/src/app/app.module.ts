import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {UploadComponent} from './components/upload/upload.component';
import {registerAllModules} from "handsontable/registry";
import {HotTableModule} from "@handsontable/angular";

// register Handsontable's modules
registerAllModules();

@NgModule({
  declarations: [
    AppComponent,
    UploadComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HotTableModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
