import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from "@angular/common/http";
import {environment} from "../../../../environments/environment";
import {BehaviorSubject, map, Observable} from "rxjs";
import {FileHandle, ImageResponse, ResetPassword} from "../../../../../util/util";
import {FileService} from "../../../service/file/file.service";
import {AuthService} from "../../../auth/service/auth.service";

/** All routes are protected in this class. User needs to be authenticated to access */
@Injectable({
  providedIn: 'root'
})
export class AuthMenuService {

  HOST: string | undefined;

  private onProfileUpdate$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor(private http: HttpClient, private fileService: FileService, private authService: AuthService) {
    this.HOST = environment.domain;
  }

  set_profile_update(bool: boolean): void {
    this.onProfileUpdate$.next(bool);
  }

  _on_profile_update$(): Observable<boolean> {
    return this.onProfileUpdate$.asObservable();
  }

  /**
   * Makes an api call to our server to log out
   *
   * @return Observable
   * */
  logoutApi(): Observable<any> {
    return this.http.get(this.HOST + "api/v1/auth/logout", {
        withCredentials: true
    });
  }

  /** Method gets user profile picture and then converts the byte array to a SafeURL */
  fetch_profile_picture(): Observable<FileHandle> {
    return this.http.get<ImageResponse>(this.HOST + "api/v1/account", {
      withCredentials: true
    }).pipe(map((res: any) => {
      // Replace res to in case a user has not added a profile picture
      const image: ImageResponse = res === null ? {name: '', media_type: '', bytes: ''} : {
        name: res.body.name,
        media_type: res.body.media_type,
        bytes: res.body.bytes
      };

      return this.fileService.createImageFromByte(image);
    }));
  }

  /** Method updates user profile picture */
  upload_profile_picture$(data: FormData): Observable<number> {
    return this.http.post<Response>(this.HOST + "api/v1/account/upload", data, {
      observe: 'response',
      withCredentials: true
    }).pipe(map((res: HttpResponse<Response>) => res.status));
  }

  /** Method makes a call to our server to reset password */
  reset_password(obj: ResetPassword): Observable<HttpResponse<ResetPassword>> {
    return this.http.put<HttpResponse<ResetPassword>>(this.HOST + "api/v1/account", obj, {
      headers: { 'content-type': 'application/json' },
      withCredentials: true
    });
  }

  isAuthenticated(): Observable<boolean> {
    return this.authService._isLoggedIn$();
  }

}
