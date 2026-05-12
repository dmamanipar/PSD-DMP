import {Component, signal} from '@angular/core';
import {ReportesService} from "../../servicio/reportes.service";
import {PdfViewerModule} from "ng2-pdf-viewer";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {MaterialModule} from "../../material/material.module";

@Component({
  selector: 'app-main-reportes',
  standalone: true,
  imports: [
    MaterialModule,
    PdfViewerModule,
    FormsModule
  ],
  templateUrl: './main-reportes.component.html',
  styleUrl: './main-reportes.component.css'
})
export class MainReportesComponent {
  pdfSrc!: string;

  selectedFiles!: FileList;
  filename!: string;
  imageData!: SafeResourceUrl;
  imageId: number = 0;
  imageSignal = signal(null);

  constructor(
    private reportesService: ReportesService,
    private sanitizer: DomSanitizer
  ) {
  }

  downloadReport() {
    this.reportesService.generateReport().subscribe(data => {
      this.createAndDownloadBlob(data, 'report.pdf');
    });
  }

  private createAndDownloadBlob(data: Blob, filename: string) {
    const url = window.URL.createObjectURL(data);
    const a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  viewReport() {
    this.reportesService.generateReport().subscribe(data => {
      this.pdfSrc = window.URL.createObjectURL(data);
    });
  }

  selectFile(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input?.files?.length) {
      this.selectedFiles = input.files;
      this.filename = input.files[0].name;
    }
  }
  upload() {
    if (this.selectedFiles?.item(0)) {
      this.reportesService.saveFile(this.selectedFiles.item(0)).subscribe();
    }
  }
  viewImage() {
    this.reportesService.readFile(this.imageId).subscribe(data => {
      this.convertToBase64(data);
    });
  }
  private convertToBase64(data: Blob) {
    const reader = new FileReader();
    reader.readAsDataURL(data);
    reader.onloadend = () => {
      this.applySanitizer(reader.result as string);
    };
  }
  private applySanitizer(base64: string) {
    this.imageData = this.sanitizer.bypassSecurityTrustResourceUrl(base64);
// @ts-ignore
    this.imageSignal.set(this.imageData);
  }

  uploadCloud() {
    if (this.selectedFiles?.item(0)) {
      this.reportesService.saveFileCloud(this.selectedFiles.item(0)).subscribe();
    }
  }

}
