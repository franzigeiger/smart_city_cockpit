import { BrowserModule } from "@angular/platform-browser";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { RouterModule, Routes } from "@angular/router";
import { HttpModule, XHRBackend } from "@angular/http";
import { HttpClientModule } from "@angular/common/http";
import { NgbModule } from "@ng-bootstrap/ng-bootstrap";
import { AngularOpenlayersModule } from "ngx-openlayers";
import { CommonModule } from "@angular/common";
import { ToastrModule } from "ngx-toastr";

import { AppComponent } from "./app.component";
import { ApiXHRBackend } from "./urlconfig";

import { NetworkComponent } from "./screens/network/network.component";
import { FleetComponent } from "./screens/fleet/fleet.component";
import { ServiceComponent } from "./screens/service/service.component";
import { EventsComponent } from "./screens/events/events.component";
import { FeedbackComponent } from "./screens/feedback/feedback.component";
import { StatusComponent } from "./screens/status/status.component";
import { PageNotFoundComponent } from "./screens/page-not-found/page-not-found.component";
import { LineComponent } from "./screens/line/line.component";
import { TimetableComponent } from "./screens/timetable/timetable.component";

import { ModalWrapperComponent } from "./components/modal-wrapper/modal-wrapper.component";
import { ManageNotificationComponent } from "./components/manage-notification/manage-notification.component";
import { ManageTourComponent } from "./components/manage-tour/manage-tour.component";
import { ManageServiceComponent } from "./components/manage-service/manage-service.component";
import { ManageFleetComponent } from "./components/manage-fleet/manage-fleet.component";
import { ManageVehicleDetailComponent } from "./components/manage-vehicle-detail/manage-vehicle-detail.component";
import { ManageShiftPlanComponent } from "./components/manage-shiftPlan/manage-shiftPlan.component";
import { ServiceDetailComponent } from "./components/service-detail/service-detail.component";
import { TableFilterComponent } from "./components/table-filter/table-filter.component";
import { ManageEventComponent } from "./components/manage-event/manage-event.component";
import { ManageTourToVehicleComponent } from "./components/manage-tour-to-vehicle/manage-tour-to-vehicle.component";
import { ShiftPlanComponent } from "./screens/shift-plan/shift-plan.component";
import { FeedbackDetailComponent } from "./components/feedback-detail/feedback-detail.component";
import { EventDetailComponent } from "./components/event-detail/event-detail.component";
import { ConfirmComponent } from "./components/confirm/confirm.component";
import { ReplacementVehicleComponent } from "./components/replacementVehicle/replacementVehicle.component";


import { ConfigComponent } from "./screens/config/config.component";

import { AngularMultiSelectModule } from "angular2-multiselect-dropdown/angular2-multiselect-dropdown";

// Calendar + ScrollPanel NgModule
import { CalendarModule } from "primeng/components/calendar/calendar";
import { ProgressBarModule, ScrollPanelModule } from "primeng/primeng";

/* Services*/
import { NetworkService } from "./services/network.service";
import { ServiceService } from "./services/service.service";
import { EventService } from "./services/event.service";
import { NotificationsService } from "./services/notifications.service";
import { TableUtilsService } from "./services/table-utils.service";
import { FleetService } from "./services/fleet.service";
import { TimetableService } from "./services/timetable.service";
import { UtilService } from "./services/util.service";
import { FeedbackService } from "./services/feedback.service";
import { ConfigService } from "./services/config.service";
import { TourService } from "./services/tour.service";
import { StopsService } from "./services/stops.service";

import { NgxDatatableModule } from "@swimlane/ngx-datatable";

// service for D3 js-library
import { D3Service } from "d3-ng2-service";

// Google charts
import { Ng2GoogleChartsModule } from "ng2-google-charts";

// Custom Pipes
import { ObjNgFor } from "./pipes/objNgFor.pipe";


const appRoutes = [
  { path: "timetable/:id", component: TimetableComponent },
  { path: "status", component: StatusComponent },
  { path: "network", component: NetworkComponent },
  { path: "fleet", component: FleetComponent },
  { path: "service", component: ServiceComponent },
  { path: "events", component: EventsComponent },
  { path: "feedback", component: FeedbackComponent },
  { path: "shiftPlan", component: ShiftPlanComponent },
  { path: "config", component: ConfigComponent },
  { path: "line/:id", component: LineComponent },
  { path: "", redirectTo: "/status", pathMatch: "full" },
  { path: "**", component: PageNotFoundComponent },
];

@NgModule({
  declarations: [
    AppComponent,
    NetworkComponent,
    FleetComponent,
    ServiceComponent,
    EventsComponent,
    FeedbackComponent,
    StatusComponent,
    PageNotFoundComponent,
    LineComponent,
    ModalWrapperComponent,
    ManageNotificationComponent,
    TimetableComponent,
    ManageTourComponent,
    ManageServiceComponent,
    ManageEventComponent,
    ManageFleetComponent,
    ServiceDetailComponent,
    TableFilterComponent,
    ManageVehicleDetailComponent,
    ManageShiftPlanComponent,
    ObjNgFor,
    ShiftPlanComponent,
    ConfigComponent,
    ManageTourToVehicleComponent,
    FeedbackDetailComponent,
    EventDetailComponent,
    ConfirmComponent,
    ReplacementVehicleComponent
  ],
  entryComponents: [
    ManageNotificationComponent,
    ManageTourComponent,
    ManageServiceComponent,
    ManageEventComponent,
    ManageFleetComponent,
    ManageVehicleDetailComponent,
    ManageShiftPlanComponent,
    ServiceDetailComponent,
    ManageTourToVehicleComponent,
    FeedbackDetailComponent,
    EventDetailComponent,
    ConfirmComponent,
    ReplacementVehicleComponent
  ],
  imports: [
    NgbModule.forRoot(),
    BrowserModule,
    CommonModule,
    ToastrModule.forRoot(), // ToastrModule added
    HttpModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forRoot(appRoutes, { useHash: true }),
    AngularMultiSelectModule,
    NgxDatatableModule,
    AngularOpenlayersModule,
    Ng2GoogleChartsModule,
    BrowserAnimationsModule,
    CalendarModule,
    ScrollPanelModule,
    ProgressBarModule
  ],
  providers: [
    D3Service,
    NetworkService,
    {
      provide: XHRBackend,
      useClass: ApiXHRBackend
    },
    ServiceService,
    TableUtilsService,
    EventService,
    FleetService,
    TimetableService,
    NotificationsService,
    TimetableService,
    UtilService,
    StopsService,
    FeedbackService,
    ConfigService,
    TourService],
  bootstrap: [AppComponent]
})
export class AppModule {
}
