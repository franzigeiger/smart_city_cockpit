import { Component, OnInit, OnDestroy, ViewChild } from "@angular/core";
import { TableUtilsService } from "../../services/table-utils.service";
import { ActivatedRoute, Router } from "@angular/router";
import { ModalWrapperComponent } from "../../components/modal-wrapper/modal-wrapper.component";
import { FleetService } from "../../services/fleet.service";
import { UtilService } from "../../services/util.service";
import { TableFilterComponent } from "../../components/table-filter/table-filter.component";
import { ConfirmComponent } from "../../components/confirm/confirm.component";
import { ReplacementVehicleComponent } from "../../components/replacementVehicle/replacementVehicle.component";
import { ToastrService } from "ngx-toastr";

@Component({
  selector: "app-fleet",
  templateUrl: "./fleet.component.html",
  styles: [],
})

export class FleetComponent implements OnInit, OnDestroy {
  @ViewChild("manageFleet") ManageFleetModal: ModalWrapperComponent;
  @ViewChild("manageVehicleDetails") ManageVehicleDetails: ModalWrapperComponent;
  @ViewChild("fleetFilter") fleetFilter: TableFilterComponent;
  @ViewChild("confirm") ConfirmComponent: ModalWrapperComponent;
  @ViewChild("replaceVehicleModal") ReplacementVehicleComponent: ModalWrapperComponent;

  fleetColumnsMapping = {
    "Type": "type",
    "ID": "vehicleID",
    "Availability": "deleted",
    "State": "state",
    "Problems": "problems"
  };
  availabilityMapping = {
    "true": "Shut down",
    "false": "Available"
  };
  stateMapping = {
    "Green": "OK",
    "Yellow": "Warning",
    "Red": "Critical"
  };

  fleetColumns = Object.keys(this.fleetColumnsMapping)
    .map(key => ({ name: key, prop: this.fleetColumnsMapping[key] }));
  fleetData = [];
  filteredFleetData = [];
  fleetPreselect = null;

  // Loading Indicator for fleet table
  loadingIndicator = true;


  private tableUtils: TableUtilsService;
  private checkNumericHeader;
  // update interval for fleet table
  private interval;
  // Refresh interval time
  private intervalTime = 10000;
  private currFilterCol = null;
  private currFilterInput = null;

  private loadingToast;

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
      "margin-top": "12px",
    }
  };

  constructor(tableUtils: TableUtilsService, private fleetService: FleetService, private util: UtilService,
    private router: Router, private toastr: ToastrService, private route: ActivatedRoute) {
    this.tableUtils = tableUtils;
    this.checkNumericHeader = this.tableUtils.checkNumericHeader(["VehicleID", "Seats"]);
  }

  async ngOnInit() {
    setTimeout(() => {
      this.loadingToast = this.toastr.info(this.util.spinner, "Loading...", { disableTimeOut: true, enableHtml: true });
    });
    this.fillFleetTable();

    // preselect category of states
    const state = this.route.snapshot.queryParamMap.get("state");
    if (state) {
      this.fleetPreselect = { column: "State", filter: state };
      this.filterFleetTable(this.fleetPreselect.column, this.fleetPreselect.filter);
    }


    this.interval = setInterval(_ => this.fillFleetTable(), this.intervalTime);

  }

  ngOnDestroy() {
    // clear fleet table refilling interval
    clearInterval(this.interval);
    if (this.loadingToast) {
      this.toastr.clear(this.loadingToast.toastId);
    }
  }

  /** Gets All Fleet Data & puts it into the table */
  async fillFleetTable() {
    this.loadingIndicator = true;
    const res = await this.fleetService.getAllData();
    const res2 = await this.fleetService.getState();
    this.fleetData = res.data;
    // assign problems / state to corresponding vehicle
    this.fleetData.map((val, index) => {
      // find corresponding vehicle
      const vehicle = res2.filter(val2 => val.vehicleID === val2.vehicleID)[0];
      if (vehicle) { // If vehicle is found, assign problems / state value
        val.problems = vehicle.problems;
        val.state = vehicle.state;
      }
    });
    this.filteredFleetData = this.fleetData;
    if (this.fleetFilter.model.column !== "nofilter") {
      this.fleetFilter.updateFilter();
    }
    this.loadingIndicator = false;

    setTimeout(() => {
      this.toastr.clear(this.loadingToast.toastId);
    });
  }
  /** Opens the Add-Vehicle Modal */
  addVehicle() {
    const payload = { types: ["Bus", "Tube"] };
    this.ManageFleetModal.show({ data: payload }, {},
      this.createVehicle.bind(this));
  }
  /**
  * Creates a new vehicle and refills the fleet table
  * @param vehicle: data of the vehcile which shall be created
  */
  async createVehicle(vehicle) {
    // Add unique vehicle identifier
    const vName = vehicle.type === "Bus" ? `B_${vehicle.name}` : `T_${vehicle.name}`;
    await this.fleetService.createVehicle(vName, vehicle.type);
    // refill fleet table
    this.fillFleetTable();
  }
  /**
  * Navigate to Shift Plan view for the selected vehicle
  * @param event: click Event
  * @param row: data of selected row
  */
  showShiftPlan(event, row) {
    this.router.navigate(["/shiftPlan"], { queryParams: { ID: row.vehicleID, type: row.type } });
  }
  /** Opens vehicle detail modal
  * @param event: click Event
  * @param row: data of selected row
  */
  showVehicleDetails(event, row) {
    this.ManageVehicleDetails.show({
      data: row,
      shutDownVehicleCallback: this.shutDownVehicleCallback.bind(this, event, row)
    }, {});
  }
  /** Navigates to service-view with vehicleID as filter param
  * @param event: click Event
  * @param row: data of selected row
  */
  showServices(event, row) {
    this.router.navigate(["/service"], { queryParams: { ID: row.vehicleID } });
  }
  /** Set selected vehicle to status: deleted
  * @param event: click Event
  * @param row: data of selected row
  */
  async shutDownVehicle(event, row) {
    this.ConfirmComponent.show({
      header: "shutdown",
      description: `Do you really want to shutdown <b> ${row.type} #${row.vehicleID}</b>?`
    }, {},
      this.shutDownVehicleCallback.bind(this, event, row)
    );
  }

  /** Refill fleet table */
  async shutDownVehicleCallback(event, row) {
    await this.fleetService.deleteVehicle(row.vehicleID);
    this.fillFleetTable();
  }

  /** Replace selected vehicle with a replacement vehicle */
  replaceVehicle(event, row) {
    this.ReplacementVehicleComponent.show({ vehicleID: row.vehicleID, type: row.type }, {},
      selectedVehicle => this.fleetService.setReplacementVehicle(row.vehicleID, selectedVehicle[0].id));
  }

  /** Listener for datatable
   * onclick open Modal
   * @param event
   */
  onActivate(event) {
    if (event.type === "click" && event.column.prop !== "actions") {
      event.cellElement.blur();
      this.showVehicleDetails(event, event.row);
    }
  }

  /**
   * Checks if this cell is linked to modal and set cursor to pointer
   * @param {any} column
   * @returns {any}
   */
  getCellClass({ column }): any {
    return {
      "use-pointer": column.prop !== "action"
    };
  }


  /**
   * Filters the fleet table using a column with a filter
   */
  filterFleetTable = (col, filter) => {
    this.currFilterCol = col;
    this.currFilterInput = filter;
    this.filteredFleetData = this.fleetData.filter(d => {
      const key = this.fleetColumnsMapping[col];
      let cellVal = key && d[key] ? d[key].toString() : "";
      // Convert propblem array to sting representation
      if (key === "problems" && d[key]) {
        cellVal = d[key].map(p => p.description).join(", ");
      }
      // d.deleted contains boolean values -> Need to compare to undefined
      if (key === "deleted" && d[key] !== void 0) {
        cellVal = this.availabilityMapping[d[key]];
      }
      if (key === "state" && d[key]) {
        cellVal = this.stateMapping[d[key]];
      }
      return !filter ? 1 : cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
  }
}
