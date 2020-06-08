import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {AppComponent} from './app.component';
import {NewTestComponent} from './components/new-test/new-test.component';
import {TestRunsComponent} from './components/test-runs/test-runs.component';
import {DiscoverDevicesComponent} from './components/discover-devices/discover-devices.component';
import {RunComponent} from './components/run/run.component';


const routes: Routes = [
  {
    path: '',
    redirectTo: 'test-runs',
    pathMatch: 'full',
  },
  {path: 'new-test', component: NewTestComponent},
  {path: 'test-runs', component: TestRunsComponent},
  {path: 'run', component: RunComponent},
  {path: 'discover-devices', component: DiscoverDevicesComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
