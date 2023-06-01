import {Injectable} from '@angular/core';
import {FileHandle, ImageResponse, Pageable} from "../../../../util/util";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {map, Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {FileService} from "../../service/file.service";

@Injectable({
  providedIn: 'root'
})
export class HomeService {

  HOST: string | undefined;

  constructor(private http: HttpClient, private fileService: FileService) {
    this.HOST = environment.domain;
  }

  /** Method fetches total number of images from our server */
  fetch_total_elements$(): Observable<number> {
    return this.http.get<number>(this.HOST + "/api/v1/image/total", {
      headers: {'content-type': 'application/json'}
    });
  }

  /** Method fetches a list of images from our server using pagination */
  fetch_images$(page: Pageable): Observable<FileHandle[]> {
    return this.http.get<ImageResponse[]>(this.HOST + "/api/v1/image", {
      headers: { 'content-type': 'application/json' },
      params: { 'page': page.page, 'size': page.size },
    }).pipe(map((arr: ImageResponse[]) => arr.map((res: ImageResponse) => this.fileService.createImageFromByte(res))));
  }

  /** Method is responsible for making a post api call to our server to upload an Image. It accepts a Form Data */
  upload_image$(data: FormData): Observable<HttpResponse<string>> {
    return this.http.post<any>(this.HOST + '/api/v1/image', data, {
      withCredentials: true
    });
  }

}
