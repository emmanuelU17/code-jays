import {Component, Renderer2} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {faList} from '@fortawesome/free-solid-svg-icons';
import {IconDefinition} from "@fortawesome/free-solid-svg-icons";
import {AuthComponent} from "./auth/auth.component";
import {AuthService} from "./auth/service/auth.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title: string = 'Code Jays';

  faList: IconDefinition = faList;

  constructor(private render: Renderer2, private dialog: MatDialog, public authService: AuthService) { }

  /** Method is responsible for displaying nav bar */
  displayNavLinks(): void {
    this.render.selectRootElement('.nav-links', true).classList.toggle('active');
  }

  /** Method is responsible for opening auth modal */
  open_auth_modal(): void {
    this.dialog.open(AuthComponent, {
      height: '900px',
      width: '900px',
    });
  }

}
