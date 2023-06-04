import {Inject, Injectable} from '@angular/core';
import {BehaviorSubject, Observable, tap} from "rxjs";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {DOCUMENT} from "@angular/common";
import {AuthResponse} from "../../../../util/util";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  HOST: string | undefined;

  private isLoggedIn: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  private isAdmin: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor(private http: HttpClient, @Inject(DOCUMENT) private document: Document) {
    this.HOST = environment.domain;

    let cookie: string = this.getCookie('isLoggedIn');
    let validated: string = cookie === null ? '' : decodeURIComponent(cookie);
    this.isLoggedIn.next(!!cookie); // !!cookie checks if isLoggedIn is not null
    this.isAdmin.next(this.contains_admin(validated));
  }

  /** Responsible for validating if use is an admin on loading of App */
  private contains_admin(json: string): boolean {
    for (let str of json.split(",")) {
      let s: string = str.replaceAll(/[^a-zA-Z]/g, "");
      if (s.toLocaleLowerCase() === 'admin') {
        return true;
      }
    }
    return false;
  }

  /**
   * Getting the cookie. Link below for better understanding
   * https://www.w3schools.com/js/js_cookies.asp
   *
   * @return string
   * */
  private getCookie(cname: string): string {
    let name: string = cname + "=";
    let decodedCookie: string = decodeURIComponent(this.document.cookie);
    let ca: string[] = decodedCookie.split(';');
    for(let i: number = 0; i < ca.length; i++) {
      let c: string = ca[i];
      while (c.charAt(0) == ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) == 0) {
        return c.substring(name.length, c.length);
      }
    }
    return '';
  }

  /** Returns a boolean observable if a user is logged in */
  _isLoggedIn$(): Observable<boolean> {
    return this.isLoggedIn.asObservable();
  }

  /** Method is responsible for validating is user who signed is has a role admin. Returns a boolean observable */
  _isAdmin$(): Observable<boolean> {
    return this.isAdmin.asObservable();
  }

  /**
   * Method responsible for signing up employees. To access this API, one has to have a role
   * ADMIN
   *
   * @param obj
   * @return Observable
   * */
  register(obj: any): Observable<any> {
    return this
      .http
      .post<AuthResponse>(this.HOST + "api/v1/auth/register", JSON.stringify(obj),
        {
          headers: { 'content-type': 'application/json' },
          observe: 'body',
          responseType: 'json',
          withCredentials: true
        }
      );
  }

  /**
   * Method makes the api call to our server
   * It accepts an object of user credentials (email, password).
   *
   * @param obj
   * @return Observable
   * */
  login(obj: any): Observable<HttpResponse<AuthResponse>> {
    return this
      .http
      .post<AuthResponse>(
        this.HOST + "api/v1/auth/login", JSON.stringify(obj),
        {
          headers: { 'content-type': 'application/json' },
          observe: 'response',
          responseType: 'json',
          withCredentials: true
        }
      )
      .pipe(tap({
        next: res => {
          let cookie: string = this.getCookie('isLoggedIn');
          let validated: string = cookie === null ? '' : decodeURIComponent(cookie);
          this.isLoggedIn.next(true);
          this.isAdmin.next(this.contains_admin(validated));
        }
      }));
  }

}
