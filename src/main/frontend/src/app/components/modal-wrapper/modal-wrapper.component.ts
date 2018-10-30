import { Component, ViewChild, Input } from "@angular/core";

import { NgbModal, NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";

import { ManageNotificationComponent } from "../manage-notification/manage-notification.component";
import { ManageTourComponent } from "../manage-tour/manage-tour.component";
import { ManageServiceComponent } from "../manage-service/manage-service.component";
import { ManageEventComponent } from "../manage-event/manage-event.component";

import { ManageFleetComponent } from "../manage-fleet/manage-fleet.component";
import { ManageVehicleDetailComponent } from "../manage-vehicle-detail/manage-vehicle-detail.component";

import { ServiceDetailComponent } from "../service-detail/service-detail.component";

import { ManageShiftPlanComponent } from "../manage-shiftPlan/manage-shiftPlan.component";

import { ManageTourToVehicleComponent } from "../manage-tour-to-vehicle/manage-tour-to-vehicle.component";
import { FeedbackDetailComponent } from "../feedback-detail/feedback-detail.component";
import { EventDetailComponent } from "../event-detail/event-detail.component";
import { ReplacementVehicleComponent } from "../replacementVehicle/replacementVehicle.component";

import { ConfirmComponent } from "../confirm/confirm.component";


@Component({
  selector: "app-modal-wrapper",
  template: ""
})
export class ModalWrapperComponent {
  @Input("selectedComponent") selectedComponent;

  private componentMap = {
    "manage-notification": ManageNotificationComponent,
    "manage-tour": ManageTourComponent,
    "manage-service": ManageServiceComponent,
    "manage-fleet": ManageFleetComponent,
    "manage-vehicle": ManageVehicleDetailComponent,
    "service-detail": ServiceDetailComponent,
    "manage-event": ManageEventComponent,
    "shift-plan": ManageShiftPlanComponent,
    "tour-to-vehicle": ManageTourToVehicleComponent,
    "feedback-detail": FeedbackDetailComponent,
    "event-detail": EventDetailComponent,
    "confirm": ConfirmComponent,
    "replaceVehicle": ReplacementVehicleComponent
  };

  private sizeMap = {
    "manage-notification": "md",
    "manage-tour": "md",
    "manage-service": "lg",
    "manage-event": "md",
    "manage-fleet": "md",
    "manage-vehicle": "fullWidth",
    "service-detail": "lg",
    "shift-plan": "md",
    "tour-to-vehicle": "lg",
    "feedback-detail": "md",
    "event-detail": "md"
  };

  constructor(private modalService: NgbModal) {
  }

  public show(payload, parameters, closeAction = res => {
  }, cancelAction = (res, modalService) => {
  }) {
    const modalRef = this.modalService
      .open(this.componentMap[this.selectedComponent], { size: this.sizeMap[this.selectedComponent] });

    modalRef.componentInstance.payload = payload;
    modalRef.componentInstance.parameters = parameters;

    modalRef.result
      .then(submitResult => closeAction(submitResult),
      cancelResult => cancelAction(cancelResult, this.modalService));
  }

}
