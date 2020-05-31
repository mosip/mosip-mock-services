import { Injectable } from '@angular/core';

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

  getAvailablePorts() {
    if (!localStorage.getItem('deviceInfo')) {
      return [];
    }
    const ports = Object.keys(JSON.parse(localStorage.getItem('deviceInfo')));
    // console.log(ports);
    return ports;
  }

  getDevicesByPortNumber(port: string) {
    if (!localStorage.getItem('deviceInfo')) {
      return [];
    }
    return JSON.parse(localStorage.getItem('deviceInfo'))[port];
  }
}
