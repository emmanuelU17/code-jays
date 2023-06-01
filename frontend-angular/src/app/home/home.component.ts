import {Component, Inject} from '@angular/core';
import {catchError, map, Observable, of, tap} from "rxjs";
import {FileHandle, Pageable} from "../../../util/util";
import {HomeService} from "./service/home.service";
import {DOCUMENT} from "@angular/common";
import {Router} from "@angular/router";
import {PageEvent} from "@angular/material/paginator";
import {AuthService} from "../auth/service/auth.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {

  file_name: string = '';

  file: File | null = null;

  upload$: Observable<any>;

  images$: Observable<FileHandle[]>;

  page: number = 0;

  size: number = 10;

  total$: Observable<number>;

  constructor(
    private homeService: HomeService,
    public authService: AuthService,
    @Inject(DOCUMENT) private document: Document,
    private router: Router
  ) {
    // Fetch all images
    this.images$ = this.homeService.fetch_images$({
      'page': this.page,
      'size': this.size
    });

    // Fetch total number of images needed for pagination
    this.total$ = this.homeService.fetch_total_elements$();

    // Upload observable
    this.upload$ = new Observable<any>();
  }

  /** Pagination logic */
  changePage(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;

    this.images$ = this.homeService.fetch_images$({
      'page': event.pageIndex,
      'size': event.pageSize
    })
  }

  /** Responsible for verifying file uploaded is in image and then updating file FormGroup */
  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.file = file;
      this.file_name = file.name;
    }
  }

  /** Method responsible for cancelling an upload */
  cancel_upload(): void {
    this.file = null;
    this.file_name = '';
  }

  /** Method is responsible for uploading an image */
  upload_image(): void {
    if (this.file == null)
      return;

    const formData: FormData = new FormData();
    formData.append('file', this.file);

    this.upload$ = this.homeService.upload_image$(formData).pipe(
      map(res => ({ res })),
      catchError(err => {
        // If 401 load auth component and expire cookie that validates if user is logged in.
        if (err.status === 401) {
          this.document.cookie = 'isLoggedIn=; Max-Age=0';
          this.router.navigate(['/authentication']);
        }
        return of(err)
      }),
      tap({
        next: res => {
          // If new image is uploaded, refresh component taking into consideration pagination
          if (res.status >= 200 && res.status < 300) {
            // Refresh component
            this.total$ = this.homeService.fetch_total_elements$();

            // Refresh component
            this.images$ = this.homeService.fetch_images$({
              'page': this.page,
              'size': this.size
            });

            // Update file
            this.file = null;
            this.file_name = '';
          }
          // End of if
        }
      })
    );
  }

}
