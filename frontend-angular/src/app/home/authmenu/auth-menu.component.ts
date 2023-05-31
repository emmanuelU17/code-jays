import {Component, Renderer2} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from "@angular/material/button";
import {MatMenuModule} from "@angular/material/menu";
import {AuthService} from "../../auth/service/auth.service";
import {catchError, map, Observable, of} from "rxjs";

@Component({
  selector: 'app-auth-menu',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatMenuModule],
  templateUrl: './auth-menu.component.html',
  styleUrls: ['./auth-menu.component.css']
})
export class AuthMenuComponent {

  logout$: Observable<any>;

  constructor(private renderer2: Renderer2, private authService: AuthService) {
    this.logout$ = new Observable<any>();
  }

  dropDown(): void {
    this.renderer2.selectRootElement('.dropdown-content', true).classList.toggle('toggle');
  }

  /** Logout Method */
  logout(): void {
    this.logout$ = this.authService.logoutApi().pipe(
      map(res => ({ res })),
      catchError(err => of(err))
    );
  }

  /** Method responsible for making call to server to reset password. It would be a private route */
  reset_password(): void {

  }

}
