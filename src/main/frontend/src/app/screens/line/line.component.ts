import { Component, OnInit, ViewChild, OnDestroy } from "@angular/core";
import { NetworkService } from "../../services/network.service";
import { ActivatedRoute } from "@angular/router";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { ModalWrapperComponent } from "../../components/modal-wrapper/modal-wrapper.component";
import { TableFilterComponent } from "../../components/table-filter/table-filter.component";
import { NotificationsService } from "../../services/notifications.service";
import { UtilService } from "../../services/util.service";
import { ServiceService } from "../../services/service.service";
import { StopsService } from "../../services/stops.service";

import { D3Service, D3 } from "d3-ng2-service";
import { FleetService } from "../../services/fleet.service";

import * as moment from "moment";
import { FeedbackService } from "../../services/feedback.service";
import { ToastrService } from "ngx-toastr";

@Component({
  selector: "app-line",
  templateUrl: "./line.component.html",
  styleUrls: ["./line.component.scss"],
})
export class LineComponent implements OnInit, OnDestroy {
  @ViewChild("managenotification") ManageNotificationModal: ModalWrapperComponent;
  @ViewChild("manageservice") ManageServiceModal: ModalWrapperComponent;
  @ViewChild("servicedetail") ServiceDetailModal: ModalWrapperComponent;
  @ViewChild("feedbackdetail") feedbackDetailModal: ModalWrapperComponent;
  @ViewChild("manageVehicleDetails") ManageVehicleDetails: ModalWrapperComponent;

  @ViewChild("notificationfilter") notificationFilter: TableFilterComponent;
  @ViewChild("servicesfilter") servicesFilter: TableFilterComponent;
  @ViewChild("feedbackfilter") feedbackFilter: TableFilterComponent;

  styles = {
    container: {
      "display": "flex",
      "flex-direction": "column",
    },
    header: {
      "display": "flex",
      "flex-direction": "row",
      "justify-content": "space-between",
    },
    table: {
      "margin-top": "12px",
      "margin-bottom": "30px",
    }
  };

  // @TODO: make sure to keep this updated with true calculated svg dimensions.
  private outerWidth = window.outerWidth;
  private outerHeight = 260;

  private xColumn = "lon";
  private yColumn = "lat";
  private svg;
  private tooltip;
  private vehicleTooltip;
  private g;

  // we use a linear scale, scaling itself doesn't really matter
  private d3: D3;
  private xScale;
  private yScale;

  private interval;
  private svgTrain = `
    M19,63h54.5c1.1,0,2.1-0.1,3.2-0.4c2.5-0.6,4.399-2.5,5.1-5S81.7,52.5,79.9,50.7
    L71,42.2c-3.6-3.4-8.2-5.3-13.1-5.3H19c-0.6,0-1,0.4-1,1v24C18,62.6,18.4,63,19,63z
    M20,60h38.5l0.7,1H20V60z M79.8,57.1C79.3,58.9,78,60.2,76.2,60.7
    c-0.9,0.2-1.8,0.3-2.7,0.3h-12l-1.7-2.6C59.6,58.2,59.3,58,59,58H20v-2h60C80,56.4,79.9,56.7,79.8,57.1z
    M76.1,50H63v-7h5.7C69,43.2,76.1,50,76.1,50z M57.8,39c2.8,0,5.601,0.7,8,2H62c-0.6,0-1,0.4-1,1v9c0,0.6,0.4,1,1,1
    h16.2   c0.599,0.599,1.006,1.08,1.399,2H20v-2h8c0.6,0,1-0.4,1-1v-9c0-0.6-0.4-1-1-1h-8v-2
    H57.8z M20,50v-7h7v7H20z M82,65v2H18v-2H82zM33,52h17c0.6,0,1-0.4,1-1v-9c0-0.6-0.4-1-1-1
    H33c-0.6,0-1,0.4-1,1v9C32,51.6,32.4,52,33,52z M34,43h15v7H34V43z
  `;

  private routeSub;
  private stopSub;
  private lineData;
  private lineState;
  private fleetState;
  public lineID;
  public lineName;
  private networkDataLines;
  private networkForCurrentLine;
  private lineIDtoColor;
  private stopIDtoName;
  private lineIDtoName;
  public selectedStop = null;
  private closedStops = [];
  public selectedNotification = [];
  public selectedService = [];

  /** Table Column Mapping + Data */
  notificationPreselect = null;
  notificationColumnMapping = {
    "Description": "description",
    "Affected Stops": "stops"
  };
  notificationColumns = Object.keys(this.notificationColumnMapping)
    .map(key => ({ name: key, prop: this.notificationColumnMapping[key] }));
  notificationData = [];
  filteredNotificationData = [];

  feedbackPreselect = null;
  feedbackColumnMapping = {
    "Affected Vehicle / Stop / Line": "affected",
    "Reason": "reason",
    "Created": "timestampFormatted",
  };
  feedbackColumns = [{
    name: "Affected Vehicle / Stop / Line",
    prop: "affected",
    flexGrow: 1.5
  }, {
    name: "Reason",
    prop: "reason",
    flexGrow: 1
  }, {
    name: "Created",
    prop: "timestampFormatted",
    flexGrow: 1
  }];
  feedbackData = [];
  filteredFeedbackData = [];

  servicesPreselect = null;
  servicesColumns = Object.keys(this.serviceService.servicesColumnMapping)
    .map(key => ({ name: key, prop: this.serviceService.servicesColumnMapping[key] }));
  servicesData = [];

  filteredServicesData = [];

  vehiclePositionRefresher;
  stateRefresher;

  private loadingToast;

  constructor(private d3Service: D3Service, private networkService: NetworkService, private fleetService: FleetService,
    private route: ActivatedRoute, private modalService: NgbModal, private notificationsService: NotificationsService,
    private util: UtilService, private serviceService: ServiceService, private feedbackService: FeedbackService,
    private toastr: ToastrService, private stopsService: StopsService) {

    this.d3 = d3Service.getD3();
    this.xScale = this.d3.scaleLinear().range([0, 1]);
    this.yScale = this.d3.scaleLinear().range([0, 1]);
  }

  refreshState = async () => {
    this.lineState = (await this.networkService.getState(this.lineID)).lineStates[0];

    this.fleetState = await this.fleetService.getState();

    // Line-index form network-data index
    let networkDataIndex;
    // Get overall network data
    const allNetworkData = await this.networkService.getAllData();
    this.networkDataLines = allNetworkData.lines;
    // get lineName from ID and key from networkDataArray
    this.networkDataLines.forEach((line, key) => {
      if (line.lineID === this.lineID) {
        this.lineName = line.name;
        networkDataIndex = key;
      }
    });
    this.networkForCurrentLine = this.networkDataLines[networkDataIndex];

    this.g.selectAll("circle").data(this.networkForCurrentLine.stops).attr("fill", this.stationColor(this.lineState.stopIdToStopStates));

    this.g.selectAll("text").data(this.networkForCurrentLine.stops)
      .attr("fill", this.stationTextColor(this.lineState.stopIdToStopStates));
  }

  // refreshes vehicle positions, triggers rerender
  refreshPositions = async () => {
    // fetch updated positions
    this.lineData.vehicles = await this.networkService.getCurrentVehicles(this.lineID);

    const stops = this.networkForCurrentLine.stops.map(datom => datom.id);

    // render vehicles with updated positions
    this.updateVehiclePositions(this.lineData.vehicles, stops);

  }

  async ngOnInit() {
    // we use a timeout to avoid issues with angular's lifecycle.
    // this would also be necessary if we were using a sync ngOnInit instead of async.
    setTimeout(() => {
      this.loadingToast = this.toastr.info(this.util.spinner, "Loading...", {
        disableTimeOut: true,
        enableHtml: true
      });
    });

    // listen to lineID route parameter
    this.routeSub = this.route.params.subscribe(async params => {
      this.lineID = params["id"];
      // Line-index form network-data index
      let networkDataIndex;

      // Get overall network data
      const allNetworkData = await this.networkService.getAllData();

      if (!allNetworkData || allNetworkData.length === 0) {
        return;
      }

      this.networkDataLines = allNetworkData.lines;
      this.lineIDtoColor = allNetworkData.lineIDtoColor;
      this.stopIDtoName = allNetworkData.stopIDtoName;
      this.lineIDtoName = allNetworkData.lineIDtoName;

      // get lineName from ID and key from networkDataArray
      this.networkDataLines.forEach((line, key) => {
        if (line.lineID === this.lineID) {
          this.lineName = line.name;
          networkDataIndex = key;
        }
      });

      // Get line and fleet State
      this.lineState = (await this.networkService.getState(this.lineID)).lineStates[0];

      this.fleetState = await this.fleetService.getState();

      // Setup tables
      this.lineData = await this.networkService.getLineData(this.lineID);
      this.feedbackData = this.lineData["feedbacks"];
      this.feedbackData.sort((a, b) => a.timestamp > b.timestamp ? 1 : -1);

      this.feedbackData.forEach(f => {
        // Target and Creation Time with Formatted Strings
        f.timestampFormatted = this.util.makeDateStr(f.timestamp);
        switch (f.type) {
          case "Vehicle":
            f.targetFormatted = f.targetId;
            break;
          case "Line":
            f.targetFormatted = this.lineIDtoName[f.targetId];
            break;
          case "Stop":
            f.targetFormatted = this.stopIDtoName[f.targetId] + " (" + f.targetId + ")";
            break;
          default:
            f.targetFormatted = f.targetId;
        }
        f.affected = f.targetFormatted === "" ? f.type : f.type + ": " + f.targetFormatted;
      });


      // @TODO: replace once services from backend is correct
      this.servicesData = this.lineData["services"];
      this.servicesData.forEach(service => {
        service.dueDate = this.util.makeDateStr(service.dueDate);
        service.priority = this.serviceService.getPriority(service.priority);
      });
      this.notificationData = this.lineData["notifications"];

      this.networkForCurrentLine = this.networkDataLines[networkDataIndex];

      // continuously refresh stop issues
      this.stateRefresher = setInterval(this.refreshState, 15000);

      // continuously refresh vehicle positions
      this.vehiclePositionRefresher = setInterval(this.refreshPositions, 1000);

      // Render visualization
      this.render(this.networkForCurrentLine.stops, this.lineData);

      // Get current state of stops and color accordingly
      const newStopsState = await this.stopsService.getStopsState();
      if (newStopsState.stops) {
        this.closedStops = newStopsState.stops;
        this.paintStops.bind(this)();
      }

      // initialize filters
      this.filteredNotificationData = this.notificationData;
      this.filteredServicesData = this.servicesData;
      this.filteredFeedbackData = this.feedbackData;

      // listen to queryParams (optional stopName, stopID)
      // and filter accordingly
      this.stopSub = this.route.queryParams.subscribe(async queryParams => {
        if (Object.keys(queryParams).length === 0) {
          return;
        }

        this.notificationPreselect = { column: "Affected Stops", filter: queryParams.stopID };
        this.feedbackPreselect = { column: "Affected Vehicle / Stop / Line", filter: queryParams.stopID };
        this.servicesPreselect = { column: "Affected Vehicle / Stop", filter: queryParams.stopID };

        this.selectStop({ id: queryParams.stopID });
        this.updateFilters();
      });

      setTimeout(() => {
        this.toastr.clear(this.loadingToast.toastId);
      });
    });
  }


  ngOnDestroy() {
    if (this.interval) {
      clearInterval(this.interval);
    }
    if (this.vehiclePositionRefresher) {
      clearInterval(this.vehiclePositionRefresher);
    }
    if (this.stateRefresher) {
      clearInterval(this.stateRefresher);
    }
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }
    if (this.stopSub) {
      this.stopSub.unsubscribe();
    }
    if (this.loadingToast) {
      this.toastr.clear(this.loadingToast.toastId);
    }
  }


  // paint Stops. If called with a stop, potentially toggle its color
  paintStops(stop = null) {
    const that = this;
    const d3 = this.d3;

    that.g.selectAll("g").selectAll("text")
      .style("text-decoration", function(d) {
        return that.isStopOpen(d.id) ? "none" : "line-through";
      })
      .attr("fill", function(d) {
        if (d.id === stop) {
          // if we look at the toggled stop, we might have to recalc its color in case of reopening
          return !that.isStopOpen(d.id) ? "#777" : that.stationColor(that.lineState.stopIdToStopStates)(d, 0);
        } else {
          return !that.isStopOpen(d.id) ? "#777" : d3.select(this).attr("fill");
        }
      });
  }

  // toggles between open and closed state for a given stop
  // also updates the UI accordingly
  toggleStopOpen = stop => {
    this.isStopOpen(stop) ? this.closedStops.push(stop) : this.closedStops.splice(this.closedStops.indexOf(stop), 1);

    this.stopsService.toggleStopStatus(stop);

    this.paintStops.bind(this)(stop);
  }

  // check whether a stop is currently open
  isStopOpen = stop => !(this.closedStops.indexOf(stop) > -1);

  /**
   * handles the selection of a stop in lineview
   * @param e event containing clicked stop info
   */
  selectStop(e) {
    // no stop / other stop is currently selected -> select (new) stop
    if (!this.selectedStop || this.selectedStop.id !== e.id) {
      this.selectedStop = { id: e.id, name: this.stopIDtoName[e.id] };

      // filter tables according to selection
      this.notificationPreselect = { column: "Affected Stops", filter: e.id };
      this.feedbackPreselect = { column: "Affected Vehicle / Stop / Line", filter: e.id };
      this.servicesPreselect = { column: "Affected Vehicle / Stop", filter: e.id };
      this.updateFilters();

      // selected stop is already selected -> unselect current stop
    } else if (this.selectedStop.id === e.id) {
      this.selectedStop = null;
      this.resetFilters("all");
    }

    // update opacity for selected stations
    this.g.selectAll("circle")
      .style("opacity", d => this.stationOpacity(d));
    this.g.selectAll("g").select("text")
      .style("opacity", d => this.stationOpacity(d));
  }

  /**
   * Compute relevant station opacity using this.selectedStop
   * @param {[type]} d [description]
   */
  stationOpacity = d => {
    if (this.selectedStop && d.id === this.selectedStop.id) {
      return 1.0;
    } else if (this.selectedStop) {
      return 0.3;
    } else {
      return 1;
    }
  }

  mouseMoveStation() {
    const top = (this.d3.event.clientY - 10);
    const left = (this.d3.event.clientX + 30);
    this.tooltip
      .style("top", `${top}px`)
      .style("left", `${left}px`);
  }

  private extractNotificationTooltipInfo(lineData, id) {
    const notificationFilterer = (notification, nID) => {
      let canStay = false;
      notification.stops.forEach(s => {
        if (s === nID) {
          canStay = true;
        }
      });
      return canStay;
    };

    const filteredNotifications = lineData.notifications.filter(n => notificationFilterer(n, id));
    let res = "";
    if (filteredNotifications.length > 5) {
      res = filteredNotifications.slice(0, 5).map(n => n.description).join("<br>");
      res += "<br>...";
    } else {
      res = filteredNotifications.map(n => n.description).join("<br>");
    }
    if (res.length !== 0) {
      return "<b>Notifications:</b><br>" + res;
    } else {
      return "";
    }
  }

  /** event handler for mouseover on circle
   @param lineData data from line, returning new function with lineData embedded
   @param d data from station
   */
  private mouseOverStation = lineData => d => {
    const notificationInfo = this.extractNotificationTooltipInfo(lineData, d.id);
    const problemInfo = this.util.formatProblems(this.lineState.stopIdToStopStates[d.id].problems);
    const target = this.d3.event.currentTarget;
    this.d3.select(target).attr("fill", "#000");
    this.tooltip
      .style("visibility", "visible")
      .html(`<b>Station: </b>${d.name.split("Underground Station")[0].trim()} <br>
        <b>ID: </b>${d.id}<br>
        <p>${problemInfo}</p>
        <p>${notificationInfo}</p>
       `);
  }

  mouseMoveVehicle() {
    const top = (this.d3.event.clientY - 10);
    const left = (this.d3.event.clientX + 30);
    this.vehicleTooltip
      .style("top", `${top}px`)
      .style("left", `${left}px`);
  }

  private mouseOverVehicle = d => {
    let problems = [];
    this.fleetState.forEach(vehicle => {
      if (vehicle.vehicleID === d.id) {
        problems = vehicle.problems;
      }
    });
    const problemInfo = this.util.formatProblems(problems);

    this.vehicleTooltip
      .style("visibility", "visible")
      .html(`<b>Vehicle: </b>${d.name}<br>
        <b>Type: </b>${d.type}<br>
        ${this.util.formatDelay(d.delayTime)}<br>
        ${problemInfo}`);
  }

  /** position calculation of vehicle path */
  calcPositionPath = stations => (d, idx) => {

    const isOutbound = d.finalStop === stations[0];

    const prevMult = stations.indexOf(d.actual_position.prevStop) + 1;
    const onPrevMult = d.actual_position.isOnPrev ? 1 : 0;

    if (isOutbound) { // driving "left"
      const x = 2 + 40 * prevMult + 18 * onPrevMult;
      const y = 105; // - 10 * onPrevMult
      return `translate(${x}, ${y}) rotate(0) scale(-0.45,0.45)`;
    } else { // driving "right"
      const x = -2 + 40 * prevMult - 18 * onPrevMult;
      const y = 65; // - 10 * onPrevMult
      return `translate(${x}, ${y}) rotate(0) scale(0.45)`;
    }
  }

  /** Position calulation of tooltipCover rect */
  calcPositionRect = stations => (d, idx) => {

    const isInbound = d.finalStop === stations[0];

    const prevMult = stations.indexOf(d.actual_position.prevStop) + 1;
    const onPrevMult = d.actual_position.isOnPrev ? 1 : 0;

    if (isInbound) { // driving "left"
      const x = 40 * prevMult + 18 * onPrevMult;
      const y = 118; //  - 10 * onPrevMult
      return `translate(${x}, ${y}) scale(-1,1)`;
    } else { // driving "right"
      const x = 3 + 40 * prevMult - 18 * onPrevMult;
      const y = 79; // - 10 * onPrevMult
      return `translate(${x}, ${y}) scale(1)`;
    }
  }

  vehicleColor = (d, idx) => {
    // TODO is this efficient enough? or is it better to map all vehicles in obj and access it
    let color = "#000";
    this.fleetState.forEach(vehicle => {
      if (vehicle.vehicleID === d.id) {
        color = this.util.getRGBStateColor(vehicle.state);
      }
    });
    return color;
  }

  stationColor = stopIdToStopStates => (d, idx) => {
    return this.util.getRGBStateColor(stopIdToStopStates[d.id].state);
  }

  stationTextColor = stopIdToStopStates => (d, idx) => {
    return this.util.getRGBStateColor(stopIdToStopStates[d.id].state);
  }

  /**
   * Start render process
   * @param data General Data from network
   * @param lineData specific data from line
   */
  render(data, lineData): void {
    const d3 = this.d3;
    const that = this;

    that.svg = d3.select("#tubeVisRoot")
      .append("svg");

    // create tooltip
    that.tooltip = d3.select("#tubeVisRoot")
      .append("div")
      .attr("class", "d3-tooltip");

    // create vehicle tooltip
    that.vehicleTooltip = d3.select("#tubeVisRoot")
      .append("div")
      .attr("class", "d3-tooltip");

    // create inner svg
    that.g = that.svg.append("g");

    that.xScale.domain(d3.extent(data, d => d[that.xColumn]));
    that.yScale.domain(d3.extent(data, d => d[that.yColumn]));

    //  connect two stations with a line
    const centralRoute = that.g.selectAll("line").data(data)
      .enter().append("line")
      .attr("stroke", d => `#${that.lineIDtoColor[that.lineID]}`)
      .attr("stroke-width", 5)
      .attr("stroke-linecap", "round")
      .attr("x1", (d, i) => (40) * (i + 1))
      .attr("y1", d => 50)
      .attr("x2", (d, i) => data[i + 1] ? (40) * (i + 2) : (40) * (i + 1))
      .attr("y2", (d, i) => 50);


    // create all stations and assign the corresponding event handlers
    const items = that.g.selectAll("circle").data(data);
    items.enter().append("circle")
      .on("click", e => that.selectStop(e))
      .on("mousemove", that.mouseMoveStation.bind(that))
      .on("mouseover", that.mouseOverStation(lineData))
      .on("mouseout", _ => {
        const target = d3.event.currentTarget;
        d3.select(target).attr("fill", that.stationColor(that.lineState.stopIdToStopStates));
        that.tooltip
          .style("visibility", "hidden");
      })
      .attr("id", d => d.station_code)
      .attr("cx", (d, i) => 40 * (i + 1))
      .attr("cy", d => 50)
      .attr("r", d => "5")
      .attr("stroke", "#000")
      .attr("fill", this.stationColor(that.lineState.stopIdToStopStates));

    const gStationNames = that.g.selectAll("gStationNames")
      .data(data)
      .enter().append("g")
      .attr("transform", (d, i) => `translate(${40 * (i + 1)},50)`);

    gStationNames.append("text")
      .on("click", e => that.selectStop(e))
      .attr("transform", (d, i) => `translate(5,-8) rotate(-45)`)
      .attr("font-size", 16)
      .text(d => {
        const split = d.name.split("Underground Station")[0].trim();
        if (split.slice(20)) {
          return split.slice(0, 20) + "...";
        } else {
          return split;
        }
      })
      .attr("fill", this.stationTextColor(that.lineState.stopIdToStopStates));

    // ======================================================================
    // test Vehicles @TODO remove
    // connect two stations with a line
    // inbound line
    const centralRouteInbound = that.g.selectAll("line2").data(data)
      .enter().append("line")
      .attr("stroke", `#${that.lineIDtoColor[that.lineID]}`)
      .attr("opacity", 0.3)
      .attr("stroke-width", 5)
      .attr("stroke-linecap", "round")
      .attr("x1", (d, i) => (40) * (i + 1))
      .attr("y1", d => 90)
      .attr("x2", (d, i) => data[i + 1] ? (40) * (i + 2) : (40) * (i + 1))
      .attr("y2", (d, i) => 90);

    // outbound line
    const centralRouteOutbound = that.g.selectAll("line2").data(data)
      .enter().append("line")
      .attr("stroke", `#${that.lineIDtoColor[that.lineID]}`)
      .attr("opacity", 0.3)
      .attr("stroke-width", 5)
      .attr("stroke-linecap", "round")
      .attr("x1", (d, i) => (40) * (i + 1))
      .attr("y1", d => 130)
      .attr("x2", (d, i) => data[i + 1] ? (40) * (i + 2) : (40) * (i + 1))
      .attr("y2", (d, i) => 130);
    const symbol = d3.symbol().size(<any>[150]); // symbol generator

    const stations = data.map(datom => datom.id);

    that.svg.attr("width", that.g.node().getBBox().width + 40);
    that.svg.attr("height", that.g.node().getBBox().height + 40);
    that.g.attr("transform", "translate(0," + (that.g.node().getBBox().height - 120) + ")");

    // render vehicles
    that.updateVehiclePositions(lineData.vehicles, stations);
  }

  /**
   * Render/update vehicles
   * @param {object} vehicleData: vehicle data of getLineData
   * @param {object} stations: stations ids for position calculation
   */
  updateVehiclePositions(vehicleData, stations) {
    const that = this;
    // Data joins
    const vehicles = that.g
      .selectAll("path")
      .data(vehicleData); // join new data with path elements
    const tooltipCovers = that.g
      .selectAll("rect")
      .data(vehicleData); // join new data with rect elements
    // Enter
    const vContainer = vehicles.enter().append("g");
    vContainer.append("path").attr("d", this.svgTrain)
      // Enter + Update
      .merge(vehicles) // Update vehicle position
      .attr("fill", this.vehicleColor)
      .attr("transform", this.calcPositionPath(stations));
    // create/add a rectangle for a better tooltip cover
    // Enter
    vContainer.append("rect").attr("width", 35).attr("height", 20).attr("fill", "none")
      .attr("pointer-events", "all")
      .on("mousemove", that.mouseMoveVehicle.bind(that))
      .on("mouseover", that.mouseOverVehicle.bind(that))
      .on("mouseout", _ => { that.vehicleTooltip.style("visibility", "hidden"); })
      .on("click", e => {
        this.fleetState.forEach(vehicle => {
          if (vehicle.vehicleID === e.id) {
            this.ManageVehicleDetails.show({
              data: vehicle, shutDownVehicleCallback: async _ => {
                await this.fleetService.deleteVehicle(vehicle.vehicleID);
              }
            }, {});
          }
        });
      })
      // Enter + Update
      .merge(tooltipCovers) // Update tooltipCover position
      .attr("transform", this.calcPositionRect(stations));

    // Exit
    vehicles.exit().remove();
    tooltipCovers.exit().remove();
  }

  // ======================================================================

  /**
   *
   * Table Filters
   *
   */

  /**
   * uses this.notificationPreselect, .servicesPreselect, .feedbackPreselect
   * to update table filters.
   */
  updateFilters() {
    this.filterNotificationTable(this.notificationPreselect.column, this.notificationPreselect.filter);
    this.filterFeedbackTable(this.feedbackPreselect.column, this.feedbackPreselect.filter);
    this.filterServicesTable(this.servicesPreselect.column, this.servicesPreselect.filter);
  }

  /** resets table for selected IDs
   id: notification / services / feedback / all */
  resetFilters(id) {
    if (id === "notification" || id === "all") {
      this.filteredNotificationData = this.notificationData;
      this.notificationFilter.resetFilter();
    }

    if (id === "services" || id === "all") {
      this.filteredServicesData = this.servicesData;
      this.servicesFilter.resetFilter();
    }

    if (id === "feedback" || id === "all") {
      this.filteredFeedbackData = this.feedbackData;
      this.feedbackFilter.resetFilter();
    }
  }

  /**
   * Filters the notification table using a column with a filter
   */
  filterNotificationTable = (col, filter) => {
    this.filteredNotificationData = this.notificationData.filter(d => {
      const key = this.notificationColumnMapping[col];

      // In case we use an array value as column, we first need to convert it to a
      // string representation
      const cellVal = key ? d[key].toString() : "";
      return !filter ? 1 : cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
  }

  /**
   * Filters the services table using a column with a filter
   */
  filterServicesTable = (col, filter) => {
    this.filteredServicesData = this.servicesData.filter(d => {
      const key = this.serviceService.servicesColumnMapping[col];

      // In case we use an array value as column, we first need to convert it to a
      // string representation
      const cellVal = key ? d[key].toString() : "";
      return !filter ? 1 : cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
  }

  /**
   * Filters the feedback table using a column with a filter
   */
  filterFeedbackTable = (col, filter) => {
    this.filteredFeedbackData = this.feedbackData.filter(d => {
      const key = this.feedbackColumnMapping[col];

      // In case we use an array value as column, we first need to convert it to a
      // string representation
      const cellVal = key ? d[key].toString() : "";
      return !filter ? 1 : cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
  }

  /**
   *
   * NOTIFICATIONS
   *
   */

  /** add notification button */
  addNotification() {
    this.ManageNotificationModal.show({ networkData: this.networkDataLines },
      { mode: "add", preFilter: this.lineName }, this.createNotification);
  }

  // arrow fn to bind this.
  createNotification = async notification => {
    const stops = notification.stops.map(stop => stop.id);

    // create in backend
    const result = await this.notificationsService.createNotification(stops, notification.description, this.lineID);
    if (result.ok) {
      await this.reloadTables();
      this.resetFilters("notification");
    }
  }

  deleteNotification = async row => {
    const result = await this.notificationsService.deleteNotification(row.stops, row.description, this.lineID);
    if (result.ok) {
      await this.reloadTables();
      this.resetFilters("notification");
    }
  }

  selectNotification = event => {
    this.selectedNotification = [];
    if (event.type === "click" && event.column.name !== "Actions") {
      event.cellElement.blur();
      this.editNotification(event.row, this.notificationData.findIndex(r => r.id === event.row.id));
    }
  }

  editNotification = (row, rowIndex) => {
    this.ManageNotificationModal.show(
      { networkData: this.networkDataLines, idx: rowIndex, row: row },
      { mode: "edit", preFilter: this.lineName },
      this.updateNotification);
  }

  updateNotification = async notification => {
    const oldNotification = this.notificationData[notification.idx];
    const newStops = notification.stops.map(stop => stop.id);

    // update backend
    await this.notificationsService.deleteNotification(oldNotification.stops, oldNotification.description, this.lineID);
    const result = await this.notificationsService.createNotification(newStops, notification.description, this.lineID);

    if (result.ok) {
      await this.reloadTables();
      this.resetFilters("notification");
    }
  }

  /**
   *
   * SERVICES
   *
   */

  selectService = event => {
    this.selectedService = [];
    if (event.type === "click" && event.column.name !== "Actions") {
      event.cellElement.blur();
      this.showServiceDetail(event.row);
    }
  }

  showServiceDetail(row) {
    this.ServiceDetailModal.show(row, {});
  }

  /** add service button */
  addService() {
    const payload = {
      availableVehicles: this.lineData.vehicles.map(v => ({ id: v.id, name: v.name })),
      availableStops: this.networkForCurrentLine.stops.map(s => ({ id: s.id, name: s.name })),
    };

    if (this.servicesFilter.getFilter() && this.servicesFilter.getFilter() !== "") {
      let type = "";
      // check if filter id is in available Stops or Vehicles and preselect type
      payload.availableStops.forEach(s => {
        if (this.servicesFilter.getFilter() === s.id) {
          type = "Stop";
        }
      });
      payload.availableVehicles.forEach(v => {
        if (this.servicesFilter.getFilter() === v.id) {
          type = "Vehicle";
        }
      });
      if (type !== "") {
        payload["prefilter"] = {type: type, id: this.servicesFilter.getFilter()};
      }
    }

    this.ManageServiceModal.show(
      payload,
      { mode: "add" },
      this.createService);
  }

  createService = async service => {
    service.dueDate = this.util.makeDateStr(service.dueDate);
    service.state = "Open";

    const editedService = Object.assign({}, service);

    editedService.referenceId = editedService.objectId;
    editedService.type = editedService.objectType;
    editedService.dueDate = moment(editedService.dueDate).valueOf();
    editedService.priority = this.serviceService.getPriority(editedService.priority);

    editedService.feedback = service.feedback.map(fb => fb.id);
    service.feedback = service.feedback.map(fb => fb.itemName);

    const result = await this.serviceService.createService(editedService);
    if (result.ok) {
      await this.reloadTables();
      this.resetFilters("services");
      // If the service request was for a feedback update feedbacks
      this.feedbackFilter.updateFilter();
    }


  }

  /**
   *
   * FEEDBACK
   *
   */

  /** Listener for datatable
   * onclick open Modal
   * @param event
   */
  onActivateFeedback(event) {
    if (event.type === "click" && event.column.prop !== "actions") {
      event.cellElement.blur();
      this.showFeedbackDetail(event.row);
    }
  }

  /**
   * Opens a feedback-detail modal
   * @param row (of datatable)
   */
  showFeedbackDetail(row) {
    this.feedbackDetailModal.show(row, {}, this.handleModal.bind(this));
  }

  handleModal = (payload) => {
    if (payload.action === "setFinished") {
      this.finishFeedback(payload, null);
    } else if (payload.action = "openServiceRequest") {
      this.openServiceRequest(payload, null);
    }
  }

  /** click to finish feedback */
  finishFeedback = async (row, rowIndex) => {
    const result = await this.feedbackService.setFeedbackFinished(row.id);
    if (result.ok) {
      await this.reloadTables();
      this.resetFilters("feedback");
    }
  }

  /**
   * Opens modal for new Service-Request and passes arguemnts for feedback, id and type
   * @param row
   * @param rowIndex
   */
  openServiceRequest = (row, rowIndex) => {
    const payload = {
      prefilter: {
        feedback: { id: row.id, itemName: row.description },
        id: row.targetId,
        type: row.type
      }
    };
    this.ManageServiceModal.show(payload, {mode: "add"}, this.createService);
  }

  /**
   * Checks if this cell is linked to modal and set cursor to pointer
   * @param {any} row
   * @param {any} column
   * @param {any} value
   * @returns {any}
   */
  getCellClass({row, column, value}): any {
    return {
      "use-pointer": column.prop !== "action"
    };
  }

  // reloads Service, Feedback and Notification Data
  async reloadTables() {
    setTimeout(() => {
      this.loadingToast = this.toastr.info(this.util.spinner, "Refreshing Tables...", {
        disableTimeOut: true,
        enableHtml: true
      });
    });
    // Setup tables
    const newlineData = await this.networkService.getLineData(this.lineID);
    this.feedbackData = newlineData["feedbacks"];
    this.feedbackData.sort((a, b) => a.timestamp > b.timestamp ? 1 : -1);

    this.feedbackData.forEach(f => {
      // Target and Creation Time with Formatted Strings
      f.timestampFormatted = this.util.makeDateStr(f.timestamp);
      switch (f.type) {
        case "Vehicle":
          f.targetFormatted = f.targetId;
          break;
        case "Line":
          f.targetFormatted = this.lineIDtoName[f.targetId];
          break;
        case "Stop":
          f.targetFormatted = this.stopIDtoName[f.targetId] + " (" + f.targetId + ")";
          break;
        default:
          f.targetFormatted = f.targetId;
      }
      f.affected = f.targetFormatted === "" ? f.type : f.type + ": " + f.targetFormatted;
    });

    this.servicesData = newlineData["services"];
    this.servicesData.forEach(service => {
      service.dueDate = this.util.makeDateStr(service.dueDate);
      service.priority = this.serviceService.getPriority(service.priority);
    });
    this.notificationData = newlineData["notifications"];

    setTimeout(() => {
      this.toastr.clear(this.loadingToast.toastId);
    });
  }
}
