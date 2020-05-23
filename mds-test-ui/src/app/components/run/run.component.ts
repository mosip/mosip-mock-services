import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-run',
  templateUrl: './run.component.html',
  styleUrls: ['./run.component.css']
})
export class RunComponent implements OnInit {
  private id: string;
  run;

  constructor(private route: ActivatedRoute, private router: Router) { }

  ngOnInit(): void {
    // this.id = this.route.snapshot.paramMap.get('id');
    // this.run = this.router.getCurrentNavigation().extras.state;
    console.log(history.state.data);
    this.run = history.state.data;
  }

}
