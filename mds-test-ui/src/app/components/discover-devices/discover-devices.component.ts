import { Component, OnInit } from '@angular/core';
import {MdsService} from '../../services/mds/mds.service';

@Component({
  selector: 'app-discover-devices',
  templateUrl: './discover-devices.component.html',
  styleUrls: ['./discover-devices.component.css']
})
export class DiscoverDevicesComponent implements OnInit {
  discoveryResponse: any;
  infoResponse: Object;
  constructor(private mdsService: MdsService) { }

  ngOnInit(): void {
  }

  discover(port: string) {
    this.mdsService.discover(port).subscribe(
      response => this.discoveryResponse = response,
      error => window.alert(error)
    );
  }

  getInfo(port: string) {
    this.mdsService.getInfo(port).subscribe(
      response => this.infoResponse = response,
      error => window.alert(error)
    );
  }
}
