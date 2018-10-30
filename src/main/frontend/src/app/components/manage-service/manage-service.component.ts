import { Component, OnInit, Input, DoCheck } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { NetworkService } from "../../services/network.service";
import { FleetService } from "../../services/fleet.service";
import { FeedbackService } from "../../services/feedback.service";

@Component({
  selector: "app-manage-service",
  templateUrl: "./manage-service.component.html",
  styles: []
})

export class ManageServiceComponent implements OnInit, DoCheck {
  @Input("payload") payload;
  @Input("parameters") parameters;

  multiSelectSettings = {
    text: "Select Feedbacks",
    selectAllText: "Select All",
    unSelectAllText: "Unselect All",
    enableSearchFilter: true,
    badgeShowLimit: 5,
  };

  objectSelectSettings = {
    singleSelection: true,
    enableSearchFilter: true
  };

  model = {
    objectType: "", // stop / vehicle
    objectId: "",
    name: "",
    priority: "",
    dueDate: "",
    description: "",
    feedback: [],
    serviceType: "", // clean / maintenance
    idx: 0
  };
  types = ["Stop", "Vehicle"];
  serviceTypes = ["Cleaning", "Maintenance"];
  objectIds = {
    stop: null,
    vehicle: null,
  };
  priorities = ["1 - low", "2 - normal", "3 - urgent", "4 - immediate"];

  public target = [];
  public feedbacks = [];
  public unpreppedFeedbacks = [];
  private selectedMode = false;

  // availableStops = []

  constructor(public activeModal: NgbActiveModal,
    private networkService: NetworkService,
    private fleetService: FleetService,
    private feedbackService: FeedbackService) { }


  ngDoCheck() {
    /**
     * checks whether dialogue is shown to edit existing service
     * and sets model accordingly.
     */
    if (!this.selectedMode) {
      this.selectedMode = true;
      if (this.parameters.mode === "edit") {
        const { payload, idx } = this.payload;
        payload.serviceType = payload.serviceType.map(st => ({ id: st, itemName: st }));
        this.model = payload;
        this.model.idx = idx;
      }
    }
  }

  flatten(arr) {
    return (
      arr.reduce(
        (acc, val) => acc.concat(Array.isArray(val) ? this.flatten(val) : val),
        [])
    );
  }

  async ngOnInit() {
    this.unpreppedFeedbacks = await this.feedbackService.getAllFeedbacks();
    this.feedbacks = this.prepFeedbacks(this.unpreppedFeedbacks);

    // we are navigating with preselected options
    if (this.payload && this.payload.availableStops && this.payload.availableVehicles) {
      this.objectIds.stop = this.payload.availableStops;
      this.objectIds.vehicle = this.payload.availableVehicles;
      // remap to angular-multiselect format:
      this.objectIds.vehicle = this.objectIds.vehicle.map(vehicle => ({ id: vehicle.id, itemName: vehicle.name }));
      this.objectIds.stop = this.objectIds.stop.map(stop => ({ id: stop.id, itemName: stop.name }));

      // we don't have any preselections, thus we fetch all available stops & vehicles
    } else {
      const network = await this.networkService.getAllData();
      const lines = network.lines;
      const stops = this.flatten(lines.map(line => line.stops)).map(stop => ({ id: stop.id, itemName: stop.name }));
      this.objectIds.stop = stops;
      const fleet = await this.fleetService.getAllData();
      this.objectIds.vehicle = fleet.data.map(v => ({ id: v.vehicleID, itemName: v.vehicleID }));
    }

    if (this.payload && this.payload.prefilter) {
      this.model.objectType = this.payload.prefilter.type;
      this.model.objectId = this.payload.prefilter.id;

      // filter for correct target
      this.target = this.objectIds[this.model.objectType.toLowerCase()].filter(o => o.id === this.model.objectId);

      if (this.payload.prefilter.feedback) {
        this.model.feedback = [this.payload.prefilter.feedback];
      }
      this.filterAvailableFeedbacks(false);
    } else {
      this.filterAvailableFeedbacks(true);
    }
  }

  prepFeedbacks(unpreppedFeedbacks) {
    return unpreppedFeedbacks.map(fb => ({ itemName: fb.description, id: fb.id }));
  }

  filterAvailableFeedbacks = (doReset = true) => {
    const unpreppedFeedbacks = this.unpreppedFeedbacks.filter(fb => this.model.objectId === fb.targetId && !fb.finished);

    this.feedbacks = this.prepFeedbacks(unpreppedFeedbacks);

    // reset feedback
    if (doReset) {
      this.model.feedback = [];
    }
  }

  cleanModel(model) {
    // model.serviceType = model.serviceType.map(st => st.itemName);
    return model;
  }

  onObjectSelect = e => {
    if (e.length !== 0) {
      this.model.objectId = e[0].id;
    } else {
      this.model.objectId = "";
    }

    this.filterAvailableFeedbacks();
  }

}
