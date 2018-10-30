import { Component, OnInit, Input, OnDestroy } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { FleetService } from "../../services/fleet.service";
import { UtilService } from "../../services/util.service";
import { ToastrService } from "ngx-toastr";


@Component({
  selector: "app-replacement-vehicles",
  templateUrl: "./replacementVehicle.component.html",
  styles: []
})

export class ReplacementVehicleComponent implements OnInit, OnDestroy {
  @Input("payload") payload;
  @Input("parameters") parameters;

  private loadingToast;

  model = { vehicleID: "", type: "", replacementVehicles: [], selectedVehicle: [] };

  // Select settings
  settings = { singleSelection: true, text: "Select replacement vehicle", enableSearchFilter: true };


  constructor(public activeModal: NgbActiveModal, private fleetService: FleetService, private toastr: ToastrService,
    private util: UtilService) { }

  async ngOnInit() {
    Object.assign(this.model, this.payload);
    setTimeout(() => {
      this.loadingToast = this.toastr.info(this.util.spinner, "Loading...", { disableTimeOut: true, enableHtml: true });
    });
    const res = await this.fleetService.getReplacementVehicle(this.model.vehicleID);
    this.toastr.clear(this.loadingToast.toastId);
    this.model.replacementVehicles = res.map(v => {
      // Do not show selected vehicle as possible replacement vehcicle
      if (v !== this.model.vehicleID) {
        return ({ id: v, itemName: v });
      }
    });
  }

  ngOnDestroy() {
    if (this.loadingToast) {
      this.toastr.clear(this.loadingToast.toastId);
    }
  }
}
