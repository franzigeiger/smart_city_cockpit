import {Component, OnInit, Input, ViewChild, OnDestroy, ChangeDetectorRef} from "@angular/core";
import {ModalWrapperComponent} from "../../components/modal-wrapper/modal-wrapper.component";
import {TimetableService} from "../../services/timetable.service";
import {ActivatedRoute} from "@angular/router";
import {NetworkService} from "../../services/network.service";
import {TourService} from "../../services/tour.service";
import {UtilService} from "../../services/util.service";
import * as moment from "moment";
import { TableFilterComponent } from "../../components/table-filter/table-filter.component";
import { ToastrService } from "ngx-toastr";

@Component({
  selector: "app-timetable",
  templateUrl: "./timetable.component.html",
  styles: [],
})
export class TimetableComponent implements OnInit, OnDestroy {
  @ViewChild("filter") filter: TableFilterComponent;
  @ViewChild("managetour") ManageTourModal: ModalWrapperComponent;
  @Input("lineInfo") lineInfo;

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
    subheader: {
      "display": "flex",
      "flex-direction": "row",
      "margin-top": "30px",
    },
    table: {
      "margin-top": "18px",
    },
  };

  tableColumns;
  tableData;
  lineName;
  lineID;
  highlightInboundOutbound = true;
  public selectedRow = [];
  oldSelected;

  // Loading Indicator for timetable table
  loadingIndicator = true;

  routeSub;

  inboundStation;
  outboundStation = "";

  private inboundOutboundTableColumns = [];
  private inboundOutboundTableData = [];
  private filteredInboundOutboundTableData = [];
  public inboundOutboundC = 0;
  private inboundDeltas = [];
  private inboundAtco = "";

  private outboundInboundTableColumns = [];
  private outboundInboundTableData = [];
  private filteredOutboundInboundTableData = [];
  private outboundDeltas = [];
  private outboundAtco = "";

  private timetable;
  private loadingToast;

  constructor(private timetableService: TimetableService, private route: ActivatedRoute, private networkService: NetworkService,
              private tourService: TourService, private util: UtilService, private toastr: ToastrService, private changeDetector: ChangeDetectorRef) {
  }

  getCellClass = (data) => {
    return {"text-white": this.selectedRow.length !== 0 && this.selectedRow[0] === data.row};
  }

  mapColumns = (stops, nameMap) => (
    [{name: "Tour ID", prop: 0},
      {name: "Vehicle", prop: 1},
      {name: "Date", prop: 2},
      ...stops
        .map((stop, idx) =>
          ({
            name: nameMap[stop].split("Underground Station")[0].trim(),
            prop: idx + 3
          }))])

  onRowSelect({selected}) {
    if (this.oldSelected && this.oldSelected === selected[0]) {
      this.oldSelected = null;
      this.selectedRow = [];
    } else {
      this.oldSelected = selected[0];
    }
  }

  /**
   * Filters the current table using a column with a filter
   */
  filterTable = (col, filter) => {
    let findIPos = 0;
    for (let i = 0; i < this.inboundOutboundTableColumns.length; i++) {
      if (this.inboundOutboundTableColumns[i].name === col) {
        findIPos = i;
        break;
      }
    }
    let findOPos = 0;
    for (let i = 0; i < this.outboundInboundTableColumns.length; i++) {
      if (this.outboundInboundTableColumns[i].name === col) {
        findOPos = i;
        break;
      }
    }

    this.filteredInboundOutboundTableData = this.inboundOutboundTableData.filter(d => {
      if (!d[findOPos]) {
        if (!filter || filter === "") {
          return true;
        } else {
          return false;
        }
      }

      return !filter ? 1 : d[findIPos].toString().toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });

    this.filteredOutboundInboundTableData = this.outboundInboundTableData.filter(d => {
      if (!d[findOPos]) {
        if (!filter || filter === "") {
          return true;
        } else {
          return false;
        }
      }

      return !filter ? 1 : d[findOPos].toString().toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });

    if (this.highlightInboundOutbound) {
      this.tableData = this.filteredInboundOutboundTableData;
    } else {
      this.tableData = this.filteredOutboundInboundTableData;
    }
  }

  // formatTime = unformatted => new Date(unformatted).toLocaleString("en-GB");
  // we instantiate date once for better performance
  formatTime = unformatted => this.util.makeTimeOnly(unformatted);
  mapStopTimes = (stops, sorted) => sorted.map(s => this.formatTime(stops[s]));
  getTourDate = (stops, sorted) => this.util.makeDateOnly(stops[sorted[0]]);

  sortStops = stops =>
    Object.keys(stops)
      .sort((a, b) => stops[a] - stops[b])

  firstNameFromSortedStops = (network, sortedStops) =>
    network
      .stopIDtoName[sortedStops[0]]
      .split("Underground Station")[0]
      .trim()

  calculateDeltas = (order, stops) => {
    const timestamps = order.map(stopId => stops[stopId]);

    return timestamps.map((ts, idx) => {
      if (idx === 0) {
        return 0;
      } else {
        return ts - timestamps[idx - 1];
      }
    });
  }

  ngOnInit() {
    setTimeout(() => {
      this.loadingToast = this.toastr.info(this.util.spinner, "Loading...", {disableTimeOut: true, enableHtml: true});
    });

    // listen to lineID route parameter
    this.routeSub = this.route.params.subscribe(async params => {
      const network = await this.networkService.getAllData();
      this.lineID = params["id"];
      this.lineName = network.lineIDtoName[this.lineID];
      this.timetable = await this.timetableService.getTimetable(this.lineID);
      const outboundStops = this.timetable.outbound[0].stops;
      const sortedInboundOutbound = this.sortStops(outboundStops);
      this.inboundStation = this.firstNameFromSortedStops(network, sortedInboundOutbound);
      this.inboundAtco = sortedInboundOutbound[0];
      this.inboundOutboundTableColumns = this.mapColumns(sortedInboundOutbound, network.stopIDtoName);
      this.inboundOutboundTableData = this.timetable.outbound.map(r => [r.tourId, r.vehicleId, this.getTourDate(r.stops, sortedInboundOutbound), ...this.mapStopTimes(r.stops, sortedInboundOutbound)]);
      this.filteredInboundOutboundTableData = [...this.inboundOutboundTableData];
      this.outboundDeltas = this.calculateDeltas(sortedInboundOutbound, outboundStops);

      const inboundStops = this.timetable.inbound[0].stops;
      const sortedOutboundInbound = this.sortStops(inboundStops);
      this.outboundAtco = sortedOutboundInbound[0];
      this.outboundStation = this.firstNameFromSortedStops(network, sortedOutboundInbound);
      this.outboundInboundTableColumns = this.mapColumns(sortedOutboundInbound, network.stopIDtoName);
      this.outboundInboundTableData = this.timetable.inbound.map(r => [r.tourId, r.vehicleId, this.getTourDate(r.stops, sortedOutboundInbound), ...this.mapStopTimes(r.stops, sortedOutboundInbound)]);
      this.filteredOutboundInboundTableData = [...this.outboundInboundTableData];
      this.inboundDeltas = this.calculateDeltas(sortedOutboundInbound, inboundStops);

      // check if there is a queryParam for tour to preselect direction
      const tourId = this.route.snapshot.queryParamMap.get("tourId");

      if (tourId) {
        let isInboundOutbound = false;
        this.inboundOutboundTableData.forEach(td => {
          if (td[0] === +tourId) {
            isInboundOutbound = true;
          }
        });
        isInboundOutbound ? this.selectInboundOutbound(true) : this.selectOutboundInbound(true);

        // select row / just search inbound and outbound
        this.inboundOutboundTableData.forEach(td => {
          if (td[0] === +tourId) {
            this.selectedRow = [td];
          }
        });
        this.outboundInboundTableData.forEach(td => {
          if (td[0] === +tourId) {
            this.selectedRow = [td];
          }
        });
      } else {
        this.selectInboundOutbound();
      }

      setTimeout(() => {
        this.toastr.clear(this.loadingToast.toastId);
      });
    });
    this.loadingIndicator = false;
  }

  ngOnDestroy() {
    this.routeSub.unsubscribe();
    if (this.loadingToast) {
      this.toastr.clear(this.loadingToast.toastId);
    }
  }

  selectInboundOutbound(isOnInit = false) {
    this.tableColumns = this.inboundOutboundTableColumns;
    this.tableData = this.filteredInboundOutboundTableData;
    this.inboundOutboundC++;
    if (!this.highlightInboundOutbound && !isOnInit) {
      this.selectedRow = [];
      this.oldSelected = null;
      this.filter.resetFilter();
    }
    this.highlightInboundOutbound = true;
  }

  selectOutboundInbound(isOnInit = false) {
    this.tableColumns = this.outboundInboundTableColumns;
    this.tableData = this.filteredOutboundInboundTableData;
    if (this.highlightInboundOutbound && !isOnInit) {
      this.selectedRow = [];
      this.oldSelected = null;
      this.filter.resetFilter();
    }
    this.highlightInboundOutbound = false;
  }

  addTour() {
    this.ManageTourModal.show(
      {
        lineID: this.lineID,
        lineName: this.lineName,
        inboundStation: this.inboundStation,
        outboundStation: this.outboundStation
      },
      {mode: "add"},
      this.createTour);
  }

  stopTimesFromDeltas = (start, deltas) => {
    let deltaSum = 0;
    return deltas.map(delta => {
      deltaSum += delta;
      return start + deltaSum;
    });
  }


  createTour = tour => {
    const milliseconds = tour.dateTime.getTime();
    // local update
    let stopTimes = [];
    if (tour.isOutbound) {
      stopTimes = this.stopTimesFromDeltas(milliseconds, this.outboundDeltas);
    } else {
      stopTimes = this.stopTimesFromDeltas(milliseconds, this.inboundDeltas);
    }
    stopTimes = stopTimes.map(stopTime => this.formatTime(stopTime));

    const date = this.util.makeDateOnly(milliseconds);

    if (tour.isOutbound) {
      this.inboundOutboundTableData.push(["new tour", tour.assignedVehicle, date, ...stopTimes]);
      this.filteredInboundOutboundTableData = [...this.inboundOutboundTableData];
      this.selectInboundOutbound();
    } else {
      this.outboundInboundTableData.push(["new tour", tour.assignedVehicle, date, ...stopTimes]);
      this.filteredOutboundInboundTableData = [...this.outboundInboundTableData];
      this.selectOutboundInbound();
    }

    // post to backend
    const startStop = tour.isOutbound ? this.inboundAtco : this.outboundAtco;
    const endStop = tour.isOutbound ? this.outboundAtco : this.inboundAtco;
    const tourToCreate = {
      vehicleId: tour.assignedVehicle,
      lineId: this.lineID,
      startTimestamp: milliseconds,
      startStop: startStop,
      endStop: endStop,
    };

    this.tourService.createTour(tourToCreate);
  }

  // delete a tour
  deleteTour = () => {
    // we manually check for the tour ID to prevent issues with finding the right
    // row to delete when rows have been reordered / inserted / ...
    const row = this.selectedRow[0];
    for (let i = 0; i < this.tableData.length; i++) {
      if (this.tableData[i][0] === row[0]) {
        this.tableData.splice(i, 1);
        break;
      }
    }

    // post to backend
    this.tourService.removeTour(row[0]);
  }

  // call edit tour modal
  editTour = () => {
    this.ManageTourModal.show({row: this.selectedRow[0], lineID: this.lineID}, {mode: "edit"}, this.updateTour);
  }

  // update the edited tour (callback of modal)
  updateTour = tour => {
    let oldVehicle = null;
    // we manually check for the tour ID to prevent issues with finding the right
    // row to edit when rows have been reordered / inserted / ...
    for (let i = 0; i < this.tableData.length; i++) {
      if (this.tableData[i][0] === tour.tourId) {
        oldVehicle = this.tableData[i][1];
        this.tableData[i][1] = tour.assignedVehicle;
        break;
      }
    }

    if (tour.assignedVehicle.length === 0) {
      this.tourService.removeVehicleFromTour(oldVehicle, tour.tourId);
    } else {
      this.tourService.changeVehicleOnTour(tour.assignedVehicle, tour.tourId);
    }

    this.oldSelected = null;
    this.selectedRow = [];
  }
}
