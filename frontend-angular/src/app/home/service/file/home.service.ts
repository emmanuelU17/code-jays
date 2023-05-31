import {Injectable} from '@angular/core';
import {FileHandle, ImageResponse, Pageable} from "../../../../../util/util";
import {DomSanitizer} from "@angular/platform-browser";
import {HttpClient} from "@angular/common/http";
import {map, Observable} from "rxjs";
import {environment} from "../../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class HomeService {

  HOST: string | undefined;

  constructor(private sanitizer: DomSanitizer, private http: HttpClient) {
    this.HOST = environment.domain;
  }

  /**
   * Method allows for displaying images. It accepts a parameter ImageResponse which is gotten from our server
   *
   * @param res of type ImageResponse
   * @return FileHandle * This is a custom Interface created in util.ts*
   * */
  createImageFromByte(res: ImageResponse): FileHandle {
    let blob: Blob = this.ByteToBlob(res.bytes_array, res.media_type);
    let file: File = new File([blob], res.image_name, { type: res.media_type});
    return {
      file: file,
      url: this.sanitizer.bypassSecurityTrustUrl(window.URL.createObjectURL(file)),
      image_name: res.image_name
    };
  }

  /** Method converts from a byte array to Blob. Which is what our browser reads */
  private ByteToBlob(bytes: any, mediaType: string): Blob {
    const byteString: string = window.atob(bytes);
    const arrayBuffer: ArrayBuffer = new ArrayBuffer(byteString.length);
    const int8Array: Uint8Array = new Uint8Array(arrayBuffer);
    for (let i: number = 0; i < byteString.length; i++) {
      int8Array[i] = byteString.charCodeAt(i);
    }
    return new Blob([int8Array], {type: mediaType});
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
      headers: {'content-type': 'application/json'},
      params: {
        'page': page.page,
        'size': page.size
      },
    }).pipe(map((arr: ImageResponse[]) => arr.map((res: ImageResponse) => this.createImageFromByte(res))));
  }

  /** Method is responsible for making a post api call to our server to upload an Image. It accepts a Form Data */
  upload_image$(data: FormData): Observable<any> {
    return this.http.post<any>(this.HOST + '/api/v1/image', data, {
      withCredentials: true
    });
  }

}
