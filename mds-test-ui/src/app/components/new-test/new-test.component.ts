import { Component, OnInit } from '@angular/core';
import {DataService} from '../../services/data/data.service';


@Component({
  selector: 'app-new-test',
  templateUrl: './new-test.component.html',
  styleUrls: ['./new-test.component.css']
})
export class NewTestComponent implements OnInit {
  title = 'mds-test-ui';
  tests: any;
  masterData: any;
  biometricTypes: [];
  deviceTypes: [];
  mdsSpecVersions: [];
  process: [];
  selectedTests = [];
  selectedBiometricType: any;
  selectedDeviceType: any;
  selectedMdsVersion: any;
  selectedProcess: any;

  constructor(private dataService: DataService) {
  }


  ngOnInit() {
    this.masterData = this.dataService.getMasterData().subscribe(
      (masterData => {
        this.masterData = masterData;
        this.biometricTypes = this.masterData['biometric-types'];
        this.mdsSpecVersions = this.masterData['mds-spec-versions'];
        this.process = this.masterData.process;
      })
    );

  }

  OnBiometricSelect(event) {
    this.deviceTypes = event.value['device-types'];
  }


  OnGetTestsClicked() {
    const requestBody = {
      biometricType: this.selectedBiometricType,
      deviceType: this.selectedDeviceType,
      mdsSpecificationVersion: this.selectedMdsVersion,
      process: this.selectedProcess
    };
    console.log(requestBody);
    this.dataService.getTests()
      .subscribe(
          tests => {
            this.tests = tests;
            console.log(this.tests);
          }
      );
  }

  OnCreateRunClicked() {
    console.log(this.selectedTests);
  }
}
