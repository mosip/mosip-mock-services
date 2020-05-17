import { Injectable } from '@angular/core';
import { of } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class DataService {
  masterData = {
    'biometric-types': [
      {
        type : 'FINGERPRINT',
        'device-types': ['SLAP', 'SINGLE', 'CAMERA'],
        segments : ['LEFT SLAP', 'RIGHT SLAP', 'TWO THUMBS', 'LEFT THUMB', 'RIGHT THUMB', 'LEFT INDEX', 'RIGHT INDEX']
      },
      {
        type : 'IRIS',
        'device-types': ['MONOCULAR', 'BINOCULAR', 'CAMERA']
      },
      {
        type : 'FACE',
        'device-types': ['STILL', 'VIDEO']
      }
    ],
    'mds-spec-versions' : ['0.9.2', '0.9.3', '0.9.4'],
    process : ['REGISTRATION', 'AUTHENTICATION']
  };


  tests = [
    {
      testId: 'finger-discover',
      testDescription: 'discover the fingerprint device and mds',
      'request-generator' : 'discover',
      validator : 'discover',
      'ui-input' : [
        {
          field : 'port',
          behavior : ''
        }
      ],
      'applies-to' : {
        process : ['REGISTRATION', 'AUTHENTICATION'],
        'biometric-type' : ['FINGERPRINT'],
        'device-type' : ['SLAP', 'FINGER'],
        'mds-spec-version' : []
      }
    },
    {
      testId: 'finger-capture-exception',
      testDescription: 'capture fingerprint with exception',
      'request-generator' : 'finger-capture-exception',
      validator : 'finger-capture-exception',
      'ui-input' : [
        {
          field : 'segment',
          behavior : ''
        },
        {
          field : 'exceptions',
          behavior : ''
        }
      ],
      'applies-to' : {
        process : ['REGISTRATION'],
        'biometric-type' : ['FINGERPRINT'],
        'device-type' : ['SLAP'],
        'mds-spec-version' : []
      }
    }
  ];

  constructor() { }

  getMasterData() {
    return of(this.masterData);
    // return this.masterData;
  }

  getTests() {
      return of(this.tests);
  }
}
