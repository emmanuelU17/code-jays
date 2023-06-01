import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {SafeUrl} from "@angular/platform-browser";

@Component({
  selector: 'app-images',
  standalone: true,
  imports: [CommonModule, FontAwesomeModule],
  templateUrl: './images.component.html',
  styleUrls: ['./images.component.css']
})
export class ImagesComponent {

  @Input() safeUrl?: SafeUrl;

  @Input() name?: string;

}
