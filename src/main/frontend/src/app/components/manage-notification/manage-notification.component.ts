import { Component, OnInit, Input, DoCheck } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: "app-manage-notification",
  templateUrl: "./manage-notification.component.html",
  styles: []
})

export class ManageNotificationComponent implements DoCheck {
  @Input("payload") payload;
  @Input("parameters") parameters;

  model = { description: "", stops: null, name: "", idx: 0 };
  availableStops = [];
  private hasPreFiltered = false;
  private selectedMode = false;
  public title = "Add";

  multiSelectSettings = {
    text: "Select Stops",
    selectAllText: "Select All",
    unSelectAllText: "Unselect All",
    enableSearchFilter: true,
    badgeShowLimit: 5,
  };

  modelStops(name, networkData) {
    let modelledStops = [];
    networkData.forEach(line => {
      if (line.name === name) {
        modelledStops = line.stops.map(stop => ({ itemName: stop.name, id: stop.id }));
      }
    });
    return modelledStops;
  }

  buildSelectedStops(selectedStopIDs, networkData) {
    const builtStops = [];
    selectedStopIDs.forEach(stopID => {
      networkData.forEach(line => {
        line.stops.forEach(stop => {
          if (stop.id === stopID) {
            builtStops.push({ itemName: stop.name, id: stop.id });
          }
        });
      });
    });
    return builtStops;
  }

  constructor(public activeModal: NgbActiveModal) { }

  ngDoCheck() {
    // set prefilter if available
    if (this.parameters.preFilter && !this.hasPreFiltered) {
      this.hasPreFiltered = true;
      this.model.name = this.parameters.preFilter;
      this.filterStops(this.parameters.preFilter);
    }

    /**
     * checks whether dialogue is shown to edit existing notification
     * and sets model accordingly.
     */
    if (!this.selectedMode) {
      this.selectedMode = true;
      if (this.parameters.mode === "edit") {
        const { row, idx, networkData } = this.payload;

        this.model.stops = this.buildSelectedStops(row.stops, networkData);
        this.title = "Edit";
        this.model.description = row.description;
        this.model.idx = idx;
      }
    }
  }

  filterStops(name) {
    this.model.stops = null;
    this.availableStops = this.modelStops(name, this.payload.networkData);
  }

}
