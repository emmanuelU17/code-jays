import {Component, Renderer2} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from "@angular/material/button";
import {MatMenuModule} from "@angular/material/menu";
import {catchError, map, Observable, of} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {AccountComponent} from "./account/account.component";
import {FileHandle} from "../../../../util/util";
import {AuthMenuService} from "./service/auth-menu.service";

@Component({
  selector: 'app-auth-menu',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatMenuModule],
  templateUrl: './auth-menu.component.html',
  styleUrls: ['./auth-menu.component.css']
})
export class AuthMenuComponent {

  logout$: Observable<any>;

  profile_picture$: Observable<FileHandle>;

  constructor(private renderer2: Renderer2, private authMenuService: AuthMenuService, private dialog: MatDialog) {
    this.profile_picture$ = this.authMenuService.fetch_profile_picture();
    this.logout$ = new Observable<any>();
    this.on_profile_photo_updated();
  }

  dropDown(): void {
    this.renderer2.selectRootElement('.dropdown-content', true).classList.toggle('toggle');
  }

  /** Logout Method */
  logout(): void {
    this.logout$ = this.authMenuService.logoutApi().pipe(
      map(res => ({ res })),
      catchError(err => of(err))
    );
  }

  /** Self-explanatory method. It opens/displays Account Component */
  open_account_modal(): void {
    this.dialog.open(AccountComponent, {
      height: 'fit-content',
      width: 'calc(500px + 1vw)',
    });
  }

  /** Method acts as a listener. It refreshes users profile picture when he/she updates it */
  on_profile_photo_updated(): void {
    this.authMenuService._on_profile_update$().subscribe({
      next: bool => {
        if (bool) {
          this.profile_picture$ = this.authMenuService.fetch_profile_picture();
        }
      }
    })
  }

}
