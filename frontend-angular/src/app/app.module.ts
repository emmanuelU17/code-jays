import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HomeComponent} from './home/home.component';
import {MatMenuModule} from "@angular/material/menu";
import {MatButtonModule} from "@angular/material/button";
import {AuthMenuComponent} from "./home/authmenu/auth-menu.component";
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {MatDialogModule} from "@angular/material/dialog";
import {AuthComponent} from "./auth/auth.component";
import {ReactiveFormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {MatPaginatorModule} from "@angular/material/paginator";
import {FooterComponent} from "./footer/footer.component";
import {NgOptimizedImage} from "@angular/common";
import {ImagesComponent} from "./images/images.component";

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    AuthComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatMenuModule,
    HttpClientModule,
    MatButtonModule,
    AuthMenuComponent,
    BrowserAnimationsModule,
    FontAwesomeModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatPaginatorModule,
    FooterComponent,
    NgOptimizedImage,
    ImagesComponent
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
