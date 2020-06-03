import { Injectable } from '@angular/core';
import {Observable, of, throwError} from 'rxjs';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {catchError} from 'rxjs/operators';
import {DataService} from '../data/data.service';
import {LocalStorageService} from '../local-storage/local-storage.service';

@Injectable({
  providedIn: 'root'
})
export class MdsService {
  private mdsUrl: string;

  constructor(
    private httpClient: HttpClient,
    private dataService: DataService,
    private localStorageService: LocalStorageService
  ) { }

  discover(port: string) {
    this.mdsUrl = environment.mds_url + port + '/device';
    // TODO: Add body for real mds
    // {
    //   type: 'Biometric Device';
    // }
    return this.httpClient.request('MOSIPDISC', this.mdsUrl)
      .pipe(
        catchError(this.handleError)
      );
    // const httpRequest = new HttpRequest('MOSIPDISC', this.mdsUrl, {
    //
    //     type: 'Biometric Device'
    //
    // });
    // return this.httpClient.request(httpRequest)
    //   .pipe(
    //     catchError(this.handleError)
    //   );
  }

  getInfo(port: string) {
    this.mdsUrl = environment.mds_url + port + '/info';
    return this.httpClient.request('MOSIPDINFO', this.mdsUrl)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse) {
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error.message);
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong,
      // console.error(
      //   `Backend returned code ${error.status}, ` +
      //   `body was: ${error.error}`);
    }
    // return an observable with a user-facing error message
    return throwError(
      'Something bad happened; please try again later.');
  }

  scanWithInfo() {
    // const ports = [];
    return new Observable(
      subscriber => {
        for (let i = 4501; i <= 4600; i++) {
          this.getInfo(i.toString()).subscribe(
            value => {
              this.dataService.decodeDeviceInfo(value).subscribe(
                decodedDeviceInfo => this.localStorageService.addDeviceInfos(i.toString(), decodedDeviceInfo),
                error => window.alert(error)
              );
            }
          );
        }
        subscriber.complete();
        return {unsubscribe() {}};
      }
    );
  }

  scan() {
    // const ports = [];
    return new Observable(
      subscriber => {
        for (let i = 4501; i <= 4600; i++) {
          // if (i == 4600) {
            this.discover(i.toString()).subscribe(
              value => {
                console.log(value);
                this.localStorageService.addDeviceDiscover(i.toString(), value);
              }
            );
          // }
        }
        subscriber.complete();
        return {unsubscribe() {}};
      }
    );
  }

  request(requestInfoDto: any) {
    return this.httpClient.request(requestInfoDto.verb, requestInfoDto.url, {body: requestInfoDto.body});
  }
}
