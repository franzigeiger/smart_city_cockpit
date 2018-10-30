import {Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {NetworkService} from "../../services/network.service";
import {FleetService} from "../../services/fleet.service";
import {UtilService} from "../../services/util.service";
import {EventService} from "../../services/event.service";
import {FeedbackService} from "../../services/feedback.service";
import {ServiceService} from "../../services/service.service";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ProgressBar, ScrollPanel, ScrollPanelModule} from "primeng/primeng";
import {setColumnDefaults} from "@swimlane/ngx-datatable/release/utils";
import {TourService} from "../../services/tour.service";
import {ModalWrapperComponent} from "../../components/modal-wrapper/modal-wrapper.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";


@Component({
  selector: "app-status",
  templateUrl: "./status.component.html",
  styleUrls: ["./status.component.scss"]
})
export class StatusComponent implements OnInit, OnDestroy {

  @ViewChild("eventdetail") eventDetailModal: ModalWrapperComponent;

  constructor(private router: Router,
              private modalService: NgbModal,
              private networkService: NetworkService,
              private fleetService: FleetService,
              private feedbackService: FeedbackService,
              private eventService: EventService,
              private serviceService: ServiceService,
              private tourService: TourService,
              public util: UtilService,
              private toastr: ToastrService) {
  }

  // to iterate over object
  Object = Object;

  redLines = [];
  yellowLines = [];
  greenLines = [];

  redVehiclesCount = 0;
  yellowVehiclesCount = 0;
  greenVehiclesCount = 0;

  vehicleTypes = ["Tube", "Bus"]; // also defines order
  vehicleTypeInfo = {};

  allNetworkData;

  feedbackTypes;
  feedbackTypeGroups = {};

  serviceRequestTypes;
  serviceRequestTypeGroups = {};

  currentEvents;
  upcomingEvents;

  private interval;
  private intervalTime = 10000;

  networkStateChartData;
  fleetStateChartData;

  unassignedTours;

  vehicleIDtoType;

  private loadingToast;
  loadingStatus = {
    feedbacks: false,
    unassigendTours: false,
    events: false,
    serviceRequests: false,
    network: false,
    fleet: false,
  };
  loadingProgress = 0;

  async ngOnInit() {
    // Show Toast only first time loading
    setTimeout(() => {
      this.loadingToast = this.toastr.info(this.util.spinner, "Loading...", {
        disableTimeOut: true,
        enableHtml: true
      });
    });

    // on init first fill status
    this.fillStatus();

    // Set interval for refreshing Status
    this.interval = setInterval(_ => {
      this.fillStatus();
    }, this.intervalTime);

  }

  /**
   * This method checks the loadingSatus-Object for every Element of the status page
   * if there are no more parts to load, hide loading Toast
   */
  checkCloseLoadingToast() {
    // set Progress Bar at top of page (loaded elements divieded by number of status elements)
    this.loadingProgress = (Object.values(this.loadingStatus).filter(e => e === true).length
      * 100 / Object.values(this.loadingStatus).length);

    if (Object.values(this.loadingStatus).indexOf(false) === -1) {
      // Reset Progressbar after 5s to start again for next refresh
      const interval = setInterval(() => {
        this.loadingProgress = 0;
        clearInterval(interval);
      }, 5000);
      // Reset loading States
      for (const key of Object.keys(this.loadingStatus)) {
        this.loadingStatus[key] = false;
      }

      // Hide Loading Toast (only first time relevant)
      setTimeout(() => {
        this.toastr.clear(this.loadingToast.toastId);
      });
    }
  }

  /**
   * Loads all Status elements
   * these are async functions, so they can all be loaded in parallel
   * in case of error, the others will still work
   * @returns {Promise<void>}
   */
  async fillStatus() {
    this.doFeedbacks();
    this.doUnassigendTours();
    this.doEvents();
    this.doServiceRequests();
    this.doNetwork();
    this.doFleet();
  }

  ngOnDestroy() {
    clearInterval(this.interval);
    if (this.loadingToast) {
      this.toastr.clear(this.loadingToast.toastId);
    }
  }

  goToLine(line) {
    this.router.navigate(["/line", line.lineID]);
  }

  goToFeedbackType(type: string) {
    this.router.navigate(["/feedback"], {queryParams: {ID: type}});
  }

  goToServiceType(type: string) {
    this.router.navigate(["/service"], {queryParams: {type: type}});
  }

  showEventDetail(event) {
    this.eventDetailModal.show(event, {});
  }

  goToFleetType(state: string) {
    this.router.navigate(["/fleet"], {queryParams: {state: state}});
  }

  goToUnassignedTour(tour) {
    this.router.navigate(["/timetable", tour.lineId], {queryParams: {tourId: tour.tourId}});
  }


  async doFeedbacks() {
    const feedbackData = await this.feedbackService.getAllFeedbacks();
    const pendingFeedbacks = feedbackData.filter(f => !f.finished);
    // Group feedbacks by type
    this.feedbackTypeGroups = pendingFeedbacks.reduce(function (acc, f) {
      if (!acc[f["type"]]) {
        acc[f["type"]] = [];
      }
      acc[f["type"]].push(f);
      return acc;
    }, {});
    this.feedbackTypes = Object.keys(this.feedbackTypeGroups);
    this.loadingStatus.feedbacks = true;
    this.checkCloseLoadingToast();
  }

  async doUnassigendTours() {
    // Unassigned Tours can directly printed with html
    this.unassignedTours = await this.tourService.getAllFreeToursForStatus();
    this.loadingStatus.unassigendTours = true;
    this.checkCloseLoadingToast();
  }

  async doEvents() {
    const eventData = await this.eventService.getAllData();
    eventData.forEach(e => {
      // replace Date Millis with formatted Date
      e.startDateTimeFormatted = this.util.makeDateStr(e.start);
      e.endDateTimeFormatted = this.util.makeDateStr(e.end);
      e.isStartpage = true;
    });
    // put all events into categories (current, upcoming, old are ignored)
    this.currentEvents = eventData.filter(e => !(e.end < Date.now()) && (e.start < Date.now()));
    this.upcomingEvents = eventData.filter(e => !(e.end < Date.now()) && !(e.start < Date.now()));
    this.loadingStatus.events = true;
    this.checkCloseLoadingToast();
  }

  async doServiceRequests() {
    const serviceRequests = await this.serviceService.getData();

    const openServiceRequests = serviceRequests.filter(sr => sr.state === "Open");
    // Group service requests by type
    this.serviceRequestTypeGroups = openServiceRequests.reduce(function (acc, sr) {
      if (!acc[sr["serviceType"]]) {
        acc[sr["serviceType"]] = [];
      }
      acc[sr["serviceType"]].push(sr);
      return acc;
    }, {});
    this.serviceRequestTypes = Object.keys(this.serviceRequestTypeGroups);
    this.loadingStatus.serviceRequests = true;
    this.checkCloseLoadingToast();
  }

  async doNetwork() {
    // first fetch all network data if not already done
    if (!this.allNetworkData) {
      this.allNetworkData = await this.networkService.getAllData();
    }

    const networkState = await this.networkService.getState();
    networkState.lineStates.forEach(line => {
      line.allStopProblems = [];
      line.stops.forEach(stop => {
        stop.problems.forEach(p => {
          line.allStopProblems.push({id: stop.id, problem: p});
        });
      });
    });
    // network state
    // Sort problems descending by critical-count
    networkState.lineStates.sort(this.compareNetworkStates);
    // Line states
    this.redLines = networkState.lineStates.filter(l => l.state === "Red");
    this.yellowLines = networkState.lineStates.filter(l => l.state === "Yellow");
    this.greenLines = networkState.lineStates.filter(l => l.state === "Green");

    this.setNetworkChartData();

    this.loadingStatus.network = true;
    this.checkCloseLoadingToast();
  }

  /**
   * This methods is for sorting network states. It takes to states and compares and returns
   * which is the "higher one"
   * @param networkStateA
   * @param networkStateB
   * @returns {number}
   */
  compareNetworkStates = (networkStateA, networkStateB) => {
    if (networkStateA.problems.length < networkStateB.problems.length) {
      return 1;
    }
    if (networkStateA.problems.length > networkStateB.problems.length) {
      return -1;
    }
    // line-problems length is equal
    if (networkStateA.allStopProblems.length < networkStateB.allStopProblems.length) {
      return 1;
    }
    if (networkStateA.allStopProblems.length > networkStateB.allStopProblems.length) {
      return -1;
    }
    return 0;
  }

  async doFleet() {
    // first fetch all vehicle data if not already done
    if (!this.vehicleIDtoType) {
      const res = await this.fleetService.getAllData();
      this.vehicleIDtoType = res.vehicleIDtoType;
    }

    const fleetState = await this.fleetService.getState();
    // Define design of used vehicle types and reset
    this.vehicleTypeInfo["Bus"] = {
      heading: "Buses",
      icon: "fa-bus",
      vehicleIdsWarning: [],
      vehicleIdsCritical: [],
      criticalProblemCount: 0,
      warningProblemCount: 0
    };
    this.vehicleTypeInfo["Tube"] = {
      heading: "Tubes",
      icon: "fa-subway",
      vehicleIdsWarning: [],
      vehicleIdsCritical: [],
      criticalProblemCount: 0,
      warningProblemCount: 0
    };

    // Sort problems descending by critical-count
    fleetState.sort(this.compareFleetStates);

    this.redVehiclesCount = fleetState.filter(v => v.state === "Red").length;
    this.yellowVehiclesCount = fleetState.filter(v => v.state === "Yellow").length;
    this.greenVehiclesCount = fleetState.filter(v => v.state === "Green").length;

    // Aggregate fleet state
    fleetState.forEach(v => {
      if (v.state === "Red") {
        this.vehicleTypeInfo[this.vehicleIDtoType[v.vehicleID]].vehicleIdsCritical
          .push({id: v.vehicleID, problemCount: v.problems.length});
        this.vehicleTypeInfo[this.vehicleIDtoType[v.vehicleID]].criticalProblemCount += v.problems.length;
      } else if (v.state === "Yellow") {
        this.vehicleTypeInfo[this.vehicleIDtoType[v.vehicleID]].vehicleIdsWarning
          .push({id: v.vehicleID, problemCount: v.problems.length});
        this.vehicleTypeInfo[this.vehicleIDtoType[v.vehicleID]].warningProblemCount += v.problems.length;
      }
    });
    this.setFleetChartData();

    this.loadingStatus.fleet = true;
    this.checkCloseLoadingToast();
  }

  /**
   * This method is for sorting fleet states. It takes to states and copares and returns
   * which is the "higher one"
   * @param fleetStateA
   * @param fleetStateB
   * @returns {number}
   */
  compareFleetStates = (fleetStateA, fleetStateB) => {
    if (fleetStateA.problems.length < fleetStateB.problems.length) {
      return 1;
    }
    if (fleetStateA.problems.length > fleetStateB.problems.length) {
      return -1;
    }
    return 0;
  }

  /**
   * Sets the values and headings for Network Google Pie Chart.
   * This method is outsourced for better readability
   */
  setNetworkChartData() {
    // Set chart data
    this.networkStateChartData = {
      chartType: "PieChart",
      dataTable: [
        ["Task", "Network Problems"],
        ["OK", this.greenLines.length],
        ["Warning", this.yellowLines.length],
        ["Critical", this.redLines.length]
      ],
      options: {
        "colors": ["#28A745", "#FFC107", "#DC3545"],
        "legend": {
          "position": "none",
        },
        "height": 200,
        "chartArea": {
          "top": 15,
          "bottom": 15,
          "width": "100%",
          "height": "100%"
        },
        pieSliceText: "none"
      },
    };
  }

  /**
   * Sets the values and headings for Network Google Pie Chart.
   * This method is outsourced for better readability
   */
  setFleetChartData() {
    // Set chart data
    this.fleetStateChartData = {
      chartType: "PieChart",
      dataTable: [
        ["Task", "Fleet Problems"],
        ["OK", this.greenVehiclesCount],
        ["Warning", this.yellowVehiclesCount],
        ["Critical", this.redVehiclesCount]
      ],
      options: {
        "colors": ["#28A745", "#FFC107", "#DC3545"],
        "legend": {
          "position": "none",
        },
        "height": 200,
        "chartArea": {
          "top": 15,
          "bottom": 15,
          "width": "100%",
          "height": "100%"
        },
        pieSliceText: "none"
      },
    };
  }
}
