import { Component, OnInit, ViewChild, AfterViewChecked } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { ModalWrapperComponent } from "../../components/modal-wrapper/modal-wrapper.component";
import { FleetService } from "../../services/fleet.service";
import { TourService } from "../../services/tour.service";
import { NetworkService } from "../../services/network.service";
import { UtilService } from "../../services/util.service";
import { D3Service, D3 } from "d3-ng2-service";
import * as moment from "moment";

declare var Gantt: any;

@Component({
  selector: "app-shift-plan",
  templateUrl: "./shift-plan.component.html",
  styleUrls: ["./shift-plan.component.scss"]
})
export class ShiftPlanComponent implements OnInit, AfterViewChecked {
  @ViewChild("chart") chart: any;
  @ViewChild("manageShiftPlan") ManageShiftPlan: ModalWrapperComponent;
  @ViewChild("manageTourToVehicle") ManageTourToVehicleComponent: ModalWrapperComponent;

  private d3: D3;

  // vehicle ID of vehicle whose shift plan shall be shown
  vehicleID;
  // type of of vehicle whose shift plan shall be shown
  vehicleType;
  // date of datepicker; Default Value: Today
  date = new Date();
  // current shown calendar week
  calWeek = this.util.extractWeekNum(this.date);
  // maps lineIDs to their unique color; neccessary for correct visualization in timeline
  private lineIDtoColor;
  private dataTableAttrs = ["Day", "Data", { type: "string", role: "style" }, "Start", "End"];
  // all tours received from backend
  private tours;
  public shiftPlanData: any = {
    chartType: "Timeline",
    dataTable: [],
    options: {
      height: 350,
      tooltip: {
        isHtml: true
      }
    }
  };

  private mapDay = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  private availableDays = new Set();

  constructor(private route: ActivatedRoute, private fleetService: FleetService, private tourService: TourService,
    private util: UtilService, private networkService: NetworkService, private d3Service: D3Service) {
    this.d3 = d3Service.getD3();
  }

  /** Gets relevant vehicle-data for selected vehicle */
  async ngOnInit() {
    this.route.queryParams.subscribe(async queryParams => {
      const networkData = await this.networkService.getAllData();
      this.lineIDtoColor = networkData.lineIDtoColor;
      this.vehicleID = queryParams["ID"];
      this.vehicleType = queryParams["type"];
      this.loadTours();
    });
  }

  /** Load all tours of respective vehicle and refill shiftPlan timeline */
  async loadTours() {
    this.tours = await this.fleetService.getShiftPlan(this.vehicleID);
    // must remove tooltip from DOM manually --> @BUG of ng2-google-charts
    if (this.chart && this.chart.getHTMLTooltip().tooltipDOMElement.nativeElement) {
      this.chart.getHTMLTooltip().tooltipDOMElement.nativeElement.remove();
    }
    this.fillShiftPlan();
  }

  /** highlight selected day in timeline, reassign mouseover and mouseclick event handlers to free places  */
  ngAfterViewChecked() {
    const d3 = this.d3;
    const util = this.util;
    const that = this;
    if (!d3.select("rect").empty()) {
      const day = +util.extractDay(this.date);
      let pos = -1;
      if (this.availableDays.has(+day)) {
        for (let i = 0; i <= day; i++) {
          if (this.availableDays.has(i)) {
            pos++;
          }
        }
      }
        // Set text color of selected day to white
        d3.selectAll("text").filter(function(d) { return this["textContent"] === that.mapDay[day]; }).attr("fill", "#fff");
        // highlight entire timeline of day
        d3.selectAll("rect").filter((_, index) => index === pos).attr("fill", "rgba(35,153,229,0.8)");
        // Change mouse cursor when hovering over free place of timeline
        d3.selectAll("rect").on("mouseover", function() {
          const elem: any = this;
          d3.select(this).style("cursor", "pointer");
        });

        // Set click-event handler to timelines, in order to add tours to vehicle
        d3.selectAll("rect").on("click", function() {
          const elem: any = this;
          let clickedDay;
          // User clicked on free space in timeline
          if (!+elem.getAttribute("stroke-width")) {
            d3.selectAll("rect").filter(function(d, index) {
              if (this === elem) {
                clickedDay = index;
              }
              return true;
            });
            const selectedDay = d3.selectAll("text").filter((_, index) => clickedDay === index).text();
            that.addTourToVehicle(selectedDay);
          }
        });
      }
    }

    /** Fills the shiftPlan with tours of the repective vehicle */
    async fillShiftPlan() {
      const util = this.util;
      const d3 = this.d3;
      // reset shiftPlan dataTable & availableDays Set
      this.shiftPlanData.dataTable = [this.dataTableAttrs];
      this.availableDays.clear();

      this.tours.forEach((t, k) => {
        // Check if tour must be visualized
        if (util.extractWeekNum(t.startTime) === util.extractWeekNum(this.date)
          && util.extractYear(t.startTime) === util.extractYear(this.date)) {
          const startTimeHours = new Date(t.startTime).getHours();
          const endTimeHours = new Date(t.endTime).getHours();
          const startTimeMinutes = new Date(t.startTime).getMinutes();
          const endTimeMinutes = new Date(t.endTime).getMinutes();
          const day = +util.extractDay(t.startTime);
          this.availableDays.add(+day);
          const tour = [this.mapDay[day], `${t.name} | #${t.tourId}`, `#${this.lineIDtoColor[t.lineId]}`,
          new Date(0, 0, 0, startTimeHours, startTimeMinutes), new Date(0, 0, 0, endTimeHours, endTimeMinutes)];
          this.shiftPlanData.dataTable.push(tour);
        }
      });
      // sort shift plan dataTable according to the weekdays
      this.shiftPlanData.dataTable.sort((val1, val2) => {
        const timeDiff = this.mapDay.indexOf(val1[0]) - this.mapDay.indexOf(val2[0]);
        return timeDiff ? timeDiff : -1;
      });
      this.shiftPlanData = Object.create(this.shiftPlanData);
    }

    /** Change calendar week and refill shift plan */
    changeDate() {
      this.calWeek = this.util.extractWeekNum(this.date);
      this.fillShiftPlan();
    }

    /** Handle select/click event of tours in timeline
    * @param {Event} event: Select event of selected timeline object
    */
    onSelect(event) {
      // must remove tooltip from DOM manually --> @BUG of ng2-google-charts
      this.chart.getHTMLTooltip().tooltipDOMElement.nativeElement.remove();
      this.shiftPlanData = Object.create(this.shiftPlanData);
      const startTime = this.util.formatShiftPlanTime(event.selectedRowFormattedValues[3]);
      const endTime = this.util.formatShiftPlanTime(event.selectedRowFormattedValues[4]);
      const duration = moment.duration(+new Date(event.selectedRowFormattedValues[4]) - +new Date(event.selectedRowFormattedValues[3]));
      const durationMinutes = duration.asMinutes();
      const durationHours = duration.asHours().toPrecision(2);
      this.ManageShiftPlan.show({
        data: event.selectedRowFormattedValues, vehicleID: this.vehicleID, vehicleType: this.vehicleType,
        date: event.selectedRowFormattedValues[0], startTime, endTime, durationMinutes, durationHours
      }, {callBack: this.loadTours.bind(this)});
    }

    /** Opens ManageTourToVehicle modal*/
    addTourToVehicle(selectedDay = this.mapDay[+this.util.extractDay(this.date)]) {
      this.ManageTourToVehicleComponent.show({
        vehicleID: this.vehicleID, vehicleType: this.vehicleType, calWeek: this.calWeek,
        year: this.date.getFullYear(), selectedDay
      }, {},
        this.assignNewTourToVehicle.bind(this));
    }

    /** Adds a new tour to the currently selected vehicle */
    async assignNewTourToVehicle(model) {
      await this.tourService.changeVehicleOnTour(this.vehicleID, model.selectedTour[0].tourId);
      this.loadTours();
    }
  }
