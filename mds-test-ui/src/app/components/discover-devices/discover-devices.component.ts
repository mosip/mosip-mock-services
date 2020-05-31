import { Component, OnInit } from '@angular/core';
import {MdsService} from '../../services/mds/mds.service';
import {DataService} from '../../services/data/data.service';
import {LocalStorageService} from '../../services/local-storage/local-storage.service';
import {MatSelectChange} from '@angular/material/select';

@Component({
  selector: 'app-discover-devices',
  templateUrl: './discover-devices.component.html',
  styleUrls: ['./discover-devices.component.css']
})
export class DiscoverDevicesComponent implements OnInit {
  discoveryResponse: any;
  infoResponse: any;
  availablePorts: string[];
  devices = [];
  constructor(
    private mdsService: MdsService,
    private dataService: DataService,
    private localStorageService: LocalStorageService
  ) { }

  ngOnInit(): void {
    this.availablePorts = this.localStorageService.getAvailablePorts();
  }

  discover(port: string) {
    this.mdsService.discover(port).subscribe(
      response => this.discoveryResponse = response,
      error => window.alert(error)
    );
  }

  getInfo(port: string) {
    this.mdsService.getInfo(port).subscribe(
      response => {
        this.infoResponse = response;
        this.dataService.decodeDeviceInfo(response).subscribe(
          decodedDeviceInfo => this.localStorageService.addDeviceInfos(port, decodedDeviceInfo),
          error => window.alert(error)
        );
      },
      error => window.alert(error)
    );
  }

  OnPortSelect(port) {
    this.devices = this.localStorageService.getDevicesByPortNumber(port);
  }

  scan() {
    // this.mdsService.scan();
  }
}
