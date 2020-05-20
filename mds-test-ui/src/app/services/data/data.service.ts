import { Injectable } from '@angular/core';
import { throwError } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { catchError } from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class DataService {

  constructor(private httpClient: HttpClient) { }

  getMasterData() {
    return this.httpClient.get(environment.base_url + 'testmanager/masterdata')
      .pipe(
        catchError(this.handleError)
      );
  }

  getTests(requestBody) {
    return this.httpClient.post(environment.base_url + 'testmanager/test', requestBody)
      .pipe(
        catchError(this.handleError)
      );
  }

  createRun(requestBody) {
    return this.httpClient.post(environment.base_url + 'testmanager/createrun', requestBody)
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
}
