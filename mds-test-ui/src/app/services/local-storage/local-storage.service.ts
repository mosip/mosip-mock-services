import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {
  constructor() { }

  addDeviceInfos(port: string, decodedDeviceInfo: any) {
      console.log(decodedDeviceInfo);
      // const devices = {
      //   port: port,
      //   devices: decodedDeviceInfo
      // };
      // const devices = {};
      // devices[port] = decodedDeviceInfo;
      let deviceInfo = {};
      if (!localStorage.getItem('deviceInfo')) {
        localStorage.setItem('deviceInfo', JSON.stringify(deviceInfo));
      }
      deviceInfo = JSON.parse(localStorage.getItem('deviceInfo'));
      deviceInfo[port] = decodedDeviceInfo;
      localStorage.setItem('deviceInfo', JSON.stringify(deviceInfo));
  }

  addDeviceDiscover(port: string, deviceDiscover: any) {
    console.log(deviceDiscover);
    // const devices = {
    //   port: port,
    //   devices: decodedDeviceInfo
    // };
    // const devices = {};
    // devices[port] = decodedDeviceInfo;
    let discover = {};
    if (!localStorage.getItem('discover')) {
      localStorage.setItem('discover', JSON.stringify(discover));
    }
    discover = JSON.parse(localStorage.getItem('discover'));
    discover[port] = deviceDiscover;
    localStorage.setItem('discover', JSON.stringify(discover));
  }
  // using device info
  // getAvailablePorts() {
  //   if (!localStorage.getItem('deviceInfo')) {
  //     return [];
  //   }
  //   const ports = Object.keys(JSON.parse(localStorage.getItem('deviceInfo')));
  //   // console.log(ports);
  //   return ports;
  // }

  getAvailablePorts() {
    if (!localStorage.getItem('discover')) {
      return [];
    }
    const ports = Object.keys(JSON.parse(localStorage.getItem('discover')));
    // console.log(ports);
    return ports;
  }
// using device info
  // getDevicesByPortNumber(port: string) {
  //   if (!localStorage.getItem('deviceInfo')) {
  //     return [];
  //   }
  //   return JSON.parse(localStorage.getItem('deviceInfo'))[port];
  // }


  getDevicesByPortNumber(port: string) {
    if (!localStorage.getItem('discover')) {
      return [];
    }
    return JSON.parse(localStorage.getItem('discover'))[port];
  }

}
