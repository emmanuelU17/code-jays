import {SafeUrl} from "@angular/platform-browser";

export interface AuthResponse {
  principal: string;
}

export interface FileHandle {
  file: File;
  url: SafeUrl;
  image_name: string;
}

export interface ImageResponse {
  image_name: string;
  media_type: string;
  bytes_array: any;
}

export interface Pageable {
  page: number;
  size: number;
}
