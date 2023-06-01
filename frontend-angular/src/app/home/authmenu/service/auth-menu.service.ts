import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from "@angular/common/http";
import {environment} from "../../../../environments/environment";
import {BehaviorSubject, map, Observable} from "rxjs";
import {FileHandle, ImageResponse, ResetPassword} from "../../../../../util/util";
import {FileService} from "../../../service/file.service";

/** All routes are protected in this class. User needs to be authenticated to access */
@Injectable({
  providedIn: 'root'
})
export class AuthMenuService {

  HOST: string | undefined;

  private onProfileUpdate$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor(private http: HttpClient, private fileService: FileService) {
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
    return this.http.post(this.HOST + "/api/v1/auth/logout", {
      headers: { 'content-type': 'application/json' },
        observe: 'response',
        withCredentials: true
    });
  }

  /** Method gets user profile picture and then converts the byte array to a SafeURL */
  fetch_profile_picture(): Observable<FileHandle> {
    return this.http.get<ImageResponse>(this.HOST + "/api/v1/account", {
      withCredentials: true
    }).pipe(map((res: ImageResponse) => this.fileService.createImageFromByte(res)));
  }

  /** Method updates user profile picture */
  upload_profile_picture$(): Observable<HttpResponse<string>> {
    return this.http.post<HttpResponse<string>>(this.HOST + "/api/v1/account/upload", {
      withCredentials: true
    });
  }

  /** Method makes a call to our server to reset password */
  reset_password(obj: ResetPassword): Observable<HttpResponse<any>> {
    return this.http.put(this.HOST + "/api/v1/auth/logout", JSON.stringify(obj),
      {
        headers: { 'content-type': 'application/json' },
        observe: 'response',
        withCredentials: true
      }
    );
  }

}
