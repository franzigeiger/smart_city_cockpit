import { Component, OnInit, Input, ViewChild } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { TourService } from "../../services/tour.service";

@Component({
  selector: "app-manage-fleet",
  templateUrl: "./manage-shiftPlan.component.html",
  styles: []
})

export class ManageShiftPlanComponent implements OnInit {
  @Input("payload") payload;
  @Input("parameters") parameters;
  @ViewChild("confirm") ConfirmComponent;

  model = {
    vehicleID: "", tourID: "", vehicleType: "", date: "", name: "", durationHours: "", durationMinutes: "",
    startTime: "", endTime: ""
  };

  // CallBack triggered after confirmation of removal
  private callBack = () => { };


  constructor(public activeModal: NgbActiveModal, private tourService: TourService) { }

  ngOnInit() {
    Object.assign(this.model, this.payload,
      // Split second data column of selected tour
      // which contains both lineName and tourID due to GoogleChart-Timeline restrictions
      { name: this.payload.data[1].split("|")[0].trim(), tourID: this.payload.data[1].split("#")[1].trim() });
    this.callBack = this.parameters.callBack;
  }
  /** Removes the selected vehicle from the selected tour */
  async removeVehicleFromTour() {
    this.activeModal.close();
    this.ConfirmComponent.show({
      header: "Removal", description: `Do you really want to remove <b>
      ${this.model.vehicleType} #${this.model.vehicleID} </b> from <b>Tour #${this.model.tourID}</b>? `
    }, {},
      // Confirm shutdown
      async _ => {
        await this.tourService.removeVehicleFromTour(this.model.vehicleID, this.model.tourID);
        this.callBack();
      },
      // Cancel shutdown -> show vehicle detail modal again
      (cancelResult, modalService) => {
        const modalRef = modalService.open(ManageShiftPlanComponent);
        modalRef.componentInstance.payload = this.payload;
        modalRef.componentInstance.parameters = this.parameters;
      }
    );

  }

}
