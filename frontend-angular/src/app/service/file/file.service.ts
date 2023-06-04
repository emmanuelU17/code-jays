import {Injectable} from '@angular/core';
import {FileHandle, ImageResponse} from "../../../../util/util";
import {DomSanitizer} from "@angular/platform-browser";

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private sanitizer: DomSanitizer) { }

  /**
   * Method allows for displaying images. It accepts a parameter ImageResponse which is gotten from our server
   *
   * @param res of type ImageResponse
   * @return FileHandle is a custom Interface created in util.ts
   * */
  createImageFromByte(res: ImageResponse): FileHandle {
    const blob: Blob = this.ByteToBlob(res.bytes, res.media_type);
    const file: File = new File([blob], res.name, { type: res.media_type});
    return {
      file: file,
      url: this.sanitizer.bypassSecurityTrustUrl(window.URL.createObjectURL(file)),
      image_name: res.name
    };
  }

  /** Method converts from a byte array to Blob. Which is what our browser reads */
  private ByteToBlob(bytes: string, mediaType: string): Blob {
    const byteString: string = window.atob(bytes);
    const arrayBuffer: ArrayBuffer = new ArrayBuffer(byteString.length);
    const int8Array: Uint8Array = new Uint8Array(arrayBuffer);
    for (let i: number = 0; i < byteString.length; i++) {
      int8Array[i] = byteString.charCodeAt(i);
    }
    return new Blob([int8Array], { type: mediaType });
  }

}
