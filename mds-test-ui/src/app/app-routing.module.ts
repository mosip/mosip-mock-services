import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {AppComponent} from './app.component';
import {NewTestComponent} from './components/new-test/new-test.component';
import {TestRunsComponent} from './components/test-runs/test-runs.component';


const routes: Routes = [
  {
    path: '',
    redirectTo: 'test-runs',
    pathMatch: 'full',
  },
  {path: 'new-test', component: NewTestComponent},
  {path: 'test-runs', component: TestRunsComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
