import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {LocalStorageService} from '../../services/local-storage/local-storage.service';
import {ComposeRequest} from '../../dto/compose-request';
import {DataService} from '../../services/data/data.service';
import {MdsService} from '../../services/mds/mds.service';

@Component({
  selector: 'app-run',
  templateUrl: './run.component.html',
  styleUrls: ['./run.component.css']
})
export class RunComponent implements OnInit {
  run;
  displayedColumns: string[] = ['testId', 'action'];
  // dataSource: MatTableDataSource<Run>;
  dataSource: any;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  tests = [];
  selectedDevice: any;
  devices = [];
  availablePorts: any;
  currentPort: any;
  requests = [];
  objectKeys = Object.keys;
  testReport : any;
  constructor(
    private localStorageService: LocalStorageService,
    private dataService: DataService,
    private mdsService: MdsService
  ) {
    // Create 100 users
    // const users = Array.from({length: 100}, (_, k) => createNewUser(k + 1));

    // Assign the data to the data source for the table to render
    // this.dataSource = new MatTableDataSource(this.runs);


  }

  ngOnInit(): void {
    console.log(history.state.data);
    this.run = history.state.data;
    this.availablePorts = this.localStorageService.getAvailablePorts();
    this.dataSource = this.run.tests;
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    this.fetchReport();
  }
  
  fetchReport(){
   this.dataService.getTestReport(this.run.runId).subscribe(
      body => {
        console.log(body);
      },
      error => window.alert(error)
    );
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  isComplete(row: any) {
    if (this.run.testReport) {
      if (this.run.testReport.hasOwnProperty(row)) {
        return true;
      }
    }
    return false;
  }

  onRunClicked() {
      const composeRequest = new ComposeRequest();
      const deviceDto = {
        port: this.currentPort,
        discoverInfo: JSON.stringify(this.selectedDevice)
      };
      this.run.tests.forEach(
        test => {
            this.dataService.composeRequest(this.run.runId, test, deviceDto).subscribe(
              body => {
                console.log(body);
                this.requests.push(body);
                this.requestMds(body, this.run.runId, test);
              },
              error => window.alert(error)
            );
        }
      );
  }

  requestMds(request, runId, testId) {
    this.mdsService.request(request.requestInfoDto).subscribe(
        response => {
          console.log(response);
          this.dataService.validateResponse(runId, testId, request, response).subscribe(
            result => console.log('result:' + result),
            error => window.alert(error)
          );
        },
      error => window.alert(error)
    );
  }

  OnPortSelect(value: any) {
    this.currentPort = value;
    this.devices = this.localStorageService.getDevicesByPortNumber(value);
  }
}
