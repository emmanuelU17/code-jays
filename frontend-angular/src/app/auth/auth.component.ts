import {AfterViewInit, ChangeDetectorRef, Component, TemplateRef, ViewChild} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {catchError, map, Observable, of, tap} from "rxjs";
import {Router} from "@angular/router";
import {AuthService} from "./service/auth.service";

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent implements AfterViewInit {

  message: string = '';

  @ViewChild('signup') signUpTemplate!: TemplateRef<any>;

  @ViewChild('login') loginTemplate!: TemplateRef<any>;

  activeTemplate: TemplateRef<any>;

  registerForm: FormGroup = new FormGroup({
    username: new FormControl("", [Validators.required, Validators.min(8), Validators.max(30)]),
    email: new FormControl("", [Validators.required, Validators.email]),
    password: new FormControl("", [Validators.required, Validators.min(8), Validators.max(30)]),
    confirm_password: new FormControl("", [Validators.required, Validators.min(8), Validators.max(30)]),
  });

  signInForm: FormGroup = new FormGroup({
    email: new FormControl("", [Validators.required]),
    password: new FormControl("", [Validators.required]),
  });

  register$: Observable<any>;

  login$: Observable<any>;

  constructor(private authService: AuthService, private changeDetectorRef: ChangeDetectorRef, private router: Router) {
    this.register$ = new Observable<any>();
    this.login$ = new Observable<any>();
    this.activeTemplate = this.signUpTemplate;
  }

  ngAfterViewInit(): void {
    this.activeTemplate = this.signUpTemplate;
    this.changeDetectorRef.detectChanges();
  }

  onClickSignUp(): void {
    this.signInForm.reset();
    this.activeTemplate = this.signUpTemplate;
  }

  onClickSignIn(): void {
    this.registerForm.reset();
    this.activeTemplate = this.loginTemplate;
  }

  /** Method is responsible for displaying the correct toast message based on template */
  /**
   * Method responsible for registering new employees. Only an ADMIN can access this API
   *
   * @return void
   * */
  on_submit_register(): void {
    this.message = '';
    if (this.registerForm.get('password')?.value !== this.registerForm.get('confirm_password')?.value) {
      this.message = "Passwords do not match";
      return;
    }

    const email: string = this.registerForm.get('email')?.value;
    this.register$ = this.authService.register({
      'email': email.trim(),
      'password': this.registerForm.get('password')?.value
    }).pipe(
      map(res => ({ res })),
      catchError(err => of(err)),
      tap({
        next: res => {
          if (res.status >= 200 && res.status < 300) {
            this.registerForm.reset();
          }
        }
      }),
    );
  }

  /**
   * This method sends info gotten from the user to our serve to be authenticated. If response is a success, user is
   * sent to home where he/she can access functionalities based on role.
   * */
  on_submit_login(): void {
    this.message = '';
    this.login$ = this.authService.login(this.signInForm.value).pipe(
      tap({
        next: res => {
          if (res.status >= 200 && res.status < 300) {
            this.signInForm.reset();
            this.router.navigate(['/']);
          }
        }
      }),
      map(res => ({ res })),
      catchError(err => of(err))
    );
  }

}
