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
  name: string;
  media_type: string;
  bytes: string;
}

export interface Pageable {
  page: number;
  size: number;
}

export interface ResetPassword {
  password: string;
  confirm_password: string;
}
