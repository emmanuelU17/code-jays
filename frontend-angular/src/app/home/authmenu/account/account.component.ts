import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatDialogRef} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {Observable, tap} from "rxjs";
import {MatIconModule} from "@angular/material/icon";
import {AuthMenuService} from "../service/auth-menu.service";

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, MatButtonModule, FormsModule, ReactiveFormsModule, MatIconModule],
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css']
})
export class AccountComponent {

  message: string = '';

  file_name: string = '';

  file: File | null = null;

  reset_password$: Observable<any>;

  picture$: Observable<any>

  resetForm: FormGroup = new FormGroup({
    password: new FormControl("", [Validators.required]),
    confirm_password: new FormControl("", [Validators.required]),
  });

  constructor(private dialogRef: MatDialogRef<AccountComponent>, private authMenuService: AuthMenuService) {
    this.reset_password$ = new Observable<any>();
    this.picture$ = new Observable();
  }

  /** Responsible for closing update modal */
  onNoClick(): void {
    this.dialogRef.close();
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

  /** Method responsible for uploading user profile picture */
  upload_profile_picture(): void {
    if (this.file === null)
      return;

    const formData: FormData = new FormData();
    formData.append('file', this.file);

    this.picture$ = this.authMenuService.upload_profile_picture$(formData).pipe(tap({
      next: res => {
        if (res >= 200 && res <= 300) {
          this.onNoClick();
          this.authMenuService.set_profile_update(true);
        }
      }
    }));
  }

  /** Method responsible for resetting password */
  on_submit_reset_password(): void {
    if (this.resetForm.get('password')?.value !== this.resetForm.get('confirm_password')?.value) {
      this.message = "Passwords do not match";
      return;
    }
    this.reset_password$ = this.authMenuService.reset_password(this.resetForm.value);
  }

}
