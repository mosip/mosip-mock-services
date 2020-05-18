import { Component, OnInit } from '@angular/core';
import { DataService } from '../../services/data/data.service';
import { map } from 'rxjs/operators';


@Component({
  selector: 'app-new-test',
  templateUrl: './new-test.component.html',
  styleUrls: ['./new-test.component.css']
})
export class NewTestComponent implements OnInit {
  title = 'mds-test-ui';
  tests: any;
  masterData: any;
  deviceTypes: [];
  selectedTests = [];
  selectedBiometricType: any;
  selectedDeviceType: any;
  selectedMdsVersion: any;
  selectedProcess: any;

  constructor(private dataService: DataService) {
  }


  ngOnInit() {
    this.masterData = this.dataService.getMasterData().subscribe(
      masterData => this.masterData = masterData,
      error => window.alert(error)
    );

  }

  OnBiometricSelect(event) {
    this.deviceTypes = event.value.deviceType;
  }


  OnGetTestsClicked() {
    const requestBody = {
      biometricType: this.selectedBiometricType.type,
      deviceType: this.selectedDeviceType,
      mdsSpecificationVersion: this.selectedMdsVersion,
      process: this.selectedProcess
    };
    // console.log(requestBody);
    this.dataService.getTests(requestBody)
      .subscribe(
          tests => this.tests = tests,
        error => window.alert(error)
      );
  }

  OnCreateRunClicked() {
    const requestBody = {
      biometricType: this.selectedBiometricType.type,
      deviceType: this.selectedDeviceType,
      mdsSpecificationVersion: this.selectedMdsVersion,
      process: this.selectedProcess,
      tests: this.selectedTests
    };
    console.log(this.selectedTests);
    this.dataService.createRun(requestBody)
      .pipe(
        map((body: any) => {
          return body.runId;
        })
      )
      .subscribe(
          runId => window.alert('created. Run ID: ' + runId),
          error => window.alert(error)
      );
  }
}
