import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {AppComponent} from './app.component';
import {NewTestComponent} from './components/new-test/new-test.component';


const routes: Routes = [
  {
    path: '',
    redirectTo: 'new-test',
    pathMatch: 'full',
  },
  {path: 'new-test', component: NewTestComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
