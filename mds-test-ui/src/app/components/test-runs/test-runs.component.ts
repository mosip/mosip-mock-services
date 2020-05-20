import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
export interface Run {
  runId: string;
  runStatus: string;
  createdOn: string;
  tests: string;
}

/** Constants used to fill up our data base. */
const COLORS: string[] = [
  'maroon', 'red', 'orange', 'yellow', 'olive', 'green', 'purple', 'fuchsia', 'lime', 'teal',
  'aqua', 'blue', 'navy', 'black', 'gray'
];
const NAMES: string[] = [
  'Maia', 'Asher', 'Olivia', 'Atticus', 'Amelia', 'Jack', 'Charlotte', 'Theodore', 'Isla', 'Oliver',
  'Isabella', 'Jasper', 'Cora', 'Levi', 'Violet', 'Arthur', 'Mia', 'Thomas', 'Elizabeth'
];
@Component({
  selector: 'app-test-runs',
  templateUrl: './test-runs.component.html',
  styleUrls: ['./test-runs.component.css']
})
export class TestRunsComponent implements OnInit {
  displayedColumns: string[] = ['runId', 'runStatus', 'createdOn'];
  // dataSource: MatTableDataSource<Run>;
  dataSource: any;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  runs = [
    {
      runId: "1589998953988",
      runStatus: "Created",
      createdOn: "2020-05-20T18:22:33.988+0000",
      tests: [
        "discover"
      ],
      testReport: null
    }
    ];
  // runs = [
  //   {
  //     : 1,
  // runStatus: 2,
  // createdOn: 3,
  //   },
  //   {
  //     runId: 1,
  //     runStatus: 5,
  //     createdOn: 3,
  //   }
  // ];
  constructor() {
    // Create 100 users
    // const users = Array.from({length: 100}, (_, k) => createNewUser(k + 1));

    // Assign the data to the data source for the table to render
    // this.dataSource = new MatTableDataSource(this.runs);
    this.dataSource = this.runs;
  }

  ngOnInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }
}

// /** Builds and returns a new User. */
//   function createNewUser(id: number): UserData {
//     const name = NAMES[Math.round(Math.random() * (NAMES.length - 1))] + ' ' +
//       NAMES[Math.round(Math.random() * (NAMES.length - 1))].charAt(0) + '.';
//
//     return {
//       id: id.toString(),
//       name: name,
//       progress: Math.round(Math.random() * 100).toString(),
//       color: COLORS[Math.round(Math.random() * (COLORS.length - 1))]
//     };
// }
