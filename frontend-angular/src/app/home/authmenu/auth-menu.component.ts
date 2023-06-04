import {Component, Renderer2} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from "@angular/material/button";
import {MatMenuModule} from "@angular/material/menu";
import {catchError, map, Observable, of, takeUntil} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {AccountComponent} from "./account/account.component";
import {AuthMenuService} from "./service/auth-menu.service";
import {SafeUrl} from "@angular/platform-browser";
import {UnsubscribeService} from "../../service/unsubscribe/unsubscribe.service";

@Component({
  selector: 'app-auth-menu',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatMenuModule],
  templateUrl: './auth-menu.component.html',
  styleUrls: ['./auth-menu.component.css']
})
export class AuthMenuComponent extends UnsubscribeService {

  logout$: Observable<any>;

  safeUrl: SafeUrl | undefined;

  constructor(private renderer2: Renderer2, public authMenuService: AuthMenuService, private dialog: MatDialog) {
    super();
    this.fetch();
    this.logout$ = new Observable<any>();
    this.on_profile_photo_updated();
  }

  /** Validates if a request should be made to the server inorder to fetch user image */
  fetch(): void {
    this.authMenuService.isAuthenticated().pipe(takeUntil(this.unsubscribe$)).subscribe({
      next: bool => {
        if (bool) {
          this.authMenuService.fetch_profile_picture().pipe(takeUntil(this.unsubscribe$)).subscribe({
            next: fileHandle => {
              this.safeUrl = fileHandle.url;
            }
          })
        }
        // End of if
      }
    });
  }

  dropDown(): void {
    this.renderer2.selectRootElement('.dropdown-content', true).classList.toggle('toggle');
  }

  /** Logout Method */
  logout(): void {
    this.logout$ = this.authMenuService
      .logoutApi().pipe(map(res => ({ res })), catchError(err => of(err)))
    this.logout$.subscribe({
      next: value => {

      }
    });
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
    this.authMenuService._on_profile_update$().pipe(takeUntil(this.unsubscribe$)).subscribe({
      next: bool => {
        if (bool) {
          this.fetch();
        }
        // End of if
      }
    })
  }

}
