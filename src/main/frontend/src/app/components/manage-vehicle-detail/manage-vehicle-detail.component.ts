import { Component, OnInit, Input, ViewChild } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { Router } from "@angular/router";
import { ServiceService } from "../../services/service.service";
import { FeedbackService } from "../../services/feedback.service";
import { FleetService } from "../../services/fleet.service";


import { UtilService } from "../../services/util.service";



@Component({
  selector: "app-manage-vehicle-detail",
  templateUrl: "./manage-vehicle-detail.component.html",
  styles: []
})
export class ManageVehicleDetailComponent implements OnInit {

  @Input("payload") payload;
  @Input("parameters") parameters;
  @ViewChild("replaceVehicleModal") ReplacementVehicleComponent;
  @ViewChild("confirm") ConfirmComponent;



  model = {
    vehicleID: "", deleted: "", type: "", problems: [],
    replaceVehicle: () => { }, shutDownVehicle: () => { }, showShiftPlan: () => { }, shutDownVehicleCallback: () => { }
  };


  vehiclePreselect = null;

  feedbackColumnMapping = {
    "Affected Vehicle / Stop / Line": "affected",
    "Reason": "reason",
    "Description": "description",
    "Created": "timestampFormatted",
  };
  feedbackColumns = [{
    name: "Affected Vehicle / Stop / Line",
    prop: "affected",
    flexGrow: 1
  }, {
    name: "Reason",
    prop: "reason",
    flexGrow: 1
  }, {
    name: "Created",
    prop: "timestampFormatted",
    flexGrow: 1.5
  }, {
    name: "Description",
    prop: "description",
    flexGrow: 3
  }];
  filteredFeedbackData = [];

  servicesColumns = Object.keys(this.servicesService.servicesColumnMapping)
    .map(key => ({ name: key, prop: this.servicesService.servicesColumnMapping[key] }));
  filteredServicesData = [];

  styles = {
    container: {
      "display": "flex",
      "flex-direction": "column",
    },
    header: {
      "display": "flex",
      "flex-direction": "row",
      "justify-content": "space-between",
      "margin-top": "24px",
      "margin-left": "6px",
      "margin-right": "6px",
    },
    table: {
      "margin-top": "30px",
    }
  };

  keyToCol = {
    "vehicleID": "ID",
    "type": "Type",
    "vehicleProblems": "Problems"
  };

  loadingIndicator;

  constructor(public activeModal: NgbActiveModal,
    private router: Router,
    private servicesService: ServiceService,
    private fleetService: FleetService,
    private feedbackService: FeedbackService,
    private util: UtilService) { }


  async ngOnInit() {
    this.loadingIndicator = true;
    this.model = this.payload.data;
    this.model.replaceVehicle = this.replaceVehicle.bind(this);
    this.model.shutDownVehicle = this.shutDownVehicle.bind(this);
    this.model.shutDownVehicleCallback = this.payload.shutDownVehicleCallback || (() => {});
    this.model.showShiftPlan = this.showShiftPlan.bind(this);
    this.vehiclePreselect = { column: "Subject ID", filter: this.payload.data.vehicleID };
    const unfilteredServices = await this.servicesService.getData();
    this.filteredServicesData = unfilteredServices.filter(service => service.objectId === this.payload.data.vehicleID);
    const unfilteredFeedbacks = await this.feedbackService.getAllFeedbacks();
    unfilteredFeedbacks.forEach(f => {
      // Target and Creation Time with Formatted Strings
      f.timestampFormatted = this.util.makeDateStr(f.timestamp);
      f.targetFormatted = f.targetId; // Only vehicles so always targetId
      f.affected = f.targetFormatted === "" ? f.type : f.type + ": " + f.targetFormatted;
    });
    this.filteredFeedbackData = unfilteredFeedbacks.filter(feedback =>
      feedback.targetId === this.payload.data.vehicleID && !feedback.finished);
    this.loadingIndicator = false;
  }

  goToServices() {
    this.activeModal.dismiss("close clicked");
    this.router.navigate(["/service"], { queryParams: { ID: this.payload.data.vehicleID } });
  }

  goToFeedbacks() {
    this.activeModal.dismiss("close clicked");
    this.router.navigate(["/feedback"], { queryParams: { ID: "Vehicle: " + this.payload.data.vehicleID } });
  }

  displayColVal(key, i) {
    // check if value is of type array
    if (this.payload.data[key].constructor === "Array") {
      console.log(this.payload.data[key]);
      return this.payload.data[key].actualProblems.join(", ");
    }
    return this.payload.data[key];
  }

  showShiftPlan() {
    this.router.navigate(["/shiftPlan"], { queryParams: { ID: this.model.vehicleID, type: this.model.type } });
  }

  /** Replace selected vehicle with a replacement vehicle */
  replaceVehicle() {
    this.ReplacementVehicleComponent.show({ vehicleID: this.model.vehicleID, type: this.model.type }, {},
      selectedVehicle => this.fleetService.setReplacementVehicle(this.model.vehicleID, selectedVehicle[0].id));
  }

  /** Set selected vehicle to status: deleted */
  async shutDownVehicle() {
    this.ConfirmComponent.show({
      header: "shutdown",
      description: `Do you really want to shutdown <b> ${this.model.type} #${this.model.vehicleID}</b>?`
    }, {}, this.model.shutDownVehicleCallback
    );
  }
}
