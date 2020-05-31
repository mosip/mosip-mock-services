import { Injectable } from '@angular/core';
import {of, throwError} from 'rxjs';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {catchError} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class MdsService {
  private mdsUrl: string;

  constructor(private httpClient: HttpClient) { }

  discover(port: string) {
    this.mdsUrl = environment.mds_url + port + '/device';
    return this.httpClient.request('MOSIPDISC', this.mdsUrl)
      .pipe(
        catchError(this.handleError)
      );
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
      console.error(
        `Backend returned code ${error.status}, ` +
        `body was: ${error.error}`);
    }
    // return an observable with a user-facing error message
    return throwError(
      'Something bad happened; please try again later.');
  }

  scan() {

  }
}
