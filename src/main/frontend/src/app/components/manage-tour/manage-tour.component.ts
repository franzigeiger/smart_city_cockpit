import { Component, OnInit, Input } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { TimetableService } from "../../services/timetable.service";
import * as moment from "moment";

@Component({
  selector: "app-manage-tour",
  templateUrl: "./manage-tour.component.html",
  styles: []
})

export class ManageTourComponent implements OnInit {
  @Input("payload") payload;
  @Input("parameters") parameters;

  model = { isOutbound: true, dateTime: "", assignedVehicle: "", tourId: 0 };
  inboundStation;
  outboundStation;
  title = "Add Tour";
  lineID;

  assignableVehicles = [];
  assignedVehicle = [];

  vehicleSelectSettings = {
    singleSelection: true,
    enableSearchFilter: true
  };

  constructor(public activeModal: NgbActiveModal, private timetableService: TimetableService) { }

  /**
   * checks whether dialogue is shown to add new tour or edit existing tour
   * and sets model accordingly.
   */
  async ngOnInit() {
    const { row, isOutbound, inboundStation, outboundStation, lineID } = this.payload;

    this.inboundStation = inboundStation;
    this.outboundStation = outboundStation;
    this.lineID = lineID;

    if (this.parameters.mode === "add") {
      // console.log("@TODO ADD TOUR handling", this.payload);
      console.log(this.payload)
    } else if (this.parameters.mode === "edit") {
      // row: [..., vehicleName, date, time at first station, ...]
      // we use time at first station to check which vehicles are also free.
      // we use the vehicleName to make sure that the user can exit editing without modifications
      const dateToCheck = row[2] + ", " + row[3];
      this.model.isOutbound = isOutbound;
      this.title = "Reassign Vehicle";
      this.model.tourId = row[0];

      this.assignableVehicles = await this.timetableService.getAvailableVehicles(this.lineID, moment(dateToCheck, "MM-DD, hh:mm").valueOf());
      this.assignableVehicles = this.assignableVehicles.map(v => ({ itemName: v, id: v }));
      // this.assignableVehicles.push({ itemName: row[1], id: row[1] });
      this.assignableVehicles.sort(function(a, b) { return (a.id > b.id) ? 1 : ((b.id > a.id) ? -1 : 0); });

      this.model.assignedVehicle = row[1];
      if (row[1]) {
        this.assignedVehicle = [{ itemName: row[1], id: row[1] }];
      }
    }
  }

  removeVehicle() {
    this.model.assignedVehicle = "";
    this.assignedVehicle = [];
  }

  updateAvailableVehicles = async () => {
    this.assignableVehicles = await this.timetableService.getAvailableVehicles(this.lineID, moment(this.model.dateTime).valueOf());
    this.assignableVehicles = this.assignableVehicles.map(v => ({ id: v, itemName: v }));

    this.assignableVehicles.sort(function(a, b) { return (a.id > b.id) ? 1 : ((b.id > a.id) ? -1 : 0); });
  }

  handleAssignVehicle = e => {
    if (e.length > 0) {
      this.model.assignedVehicle = e[0].id;
    } else {
      this.model.assignedVehicle = "";
    }
  }

}
