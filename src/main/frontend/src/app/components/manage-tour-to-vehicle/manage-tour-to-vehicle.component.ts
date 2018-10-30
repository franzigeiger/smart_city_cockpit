import { Component, OnInit, Input, ViewChild } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { TourService } from "../../services/tour.service";
import { UtilService } from "../../services/util.service";
import { TableFilterComponent } from "../../components/table-filter/table-filter.component";
import * as moment from "moment";


@Component({
  selector: "app-manage-tour-to-vehicle",
  templateUrl: "./manage-tour-to-vehicle.component.html",
  styles: []
})
export class ManageTourToVehicleComponent implements OnInit {

  @Input("payload") payload;
  @Input("parameters") parameters;

  @ViewChild("tourfilter") tourFilter: TableFilterComponent;

  model = { vehicleID: "", selectedDay: "", selectedDayFormatted: "", calWeek: "", year: "", selectedTour: [], vehicleType: "" };
  // Maps weekdays to their respective number representation
  weekdaysMapping = {
    "Sunday": "0",
    "Monday": "1",
    "Tuesday": "2",
    "Wednesday": "3",
    "Thursday": "4",
    "Friday": "5",
    "Saturday": "6"
  };

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
      "margin-left": "15px",
      "margin-right": "6px",
    },
    table: {
      "margin": "15px 15px 0 15px",
    }
  };
  tourData = [];
  filteredTourData = [];
  loadingIndicator = true;
  tourColumnsMapping = {
    "Tour ID": "tourId",
    "Line": "name",
    "Start Time": "startTime",
    "End Time": "endTime"
  };
  tourColumns = Object.keys(this.tourColumnsMapping)
    .map(key => ({ name: key, prop: this.tourColumnsMapping[key] }));

  constructor(public activeModal: NgbActiveModal, private tourService: TourService, private util: UtilService) {
    this.getCellClass = this.getCellClass.bind(this);
  }

  ngOnInit() {
    Object.assign(this.model, this.payload);
    this.changeDay();
  }

  /**
  * Format date for select box
  * @param {string} day: day of the formatted date
  * @return {string} formatted date
  */
  formatSelectDate(day: string): string {
    return moment(`${this.model.year} ${this.model.calWeek} ${this.weekdaysMapping[day]}`,
      "YYYY w e").format("MMM Do YY");
  }

  /** Change event handler for day picker */
  async changeDay() {
    this.loadingIndicator = true;
    const util = this.util;
    const startTime = +moment(`${this.model.year} ${this.model.calWeek} ${this.weekdaysMapping[this.model.selectedDay]} 00:00:00}`,
      "YYYY w e hh:mm:ss");
    const endTime = +moment(`${this.model.year} ${this.model.calWeek} ${this.weekdaysMapping[this.model.selectedDay]} 24:00:00}`,
      "YYYY w e hh:mm:ss");
    const freeTours = await this.tourService.getFreeTours(this.model.vehicleID, startTime, endTime);
    this.filteredTourData = this.tourData = freeTours.map(tour => {
      tour.startTime = util.extractTime(tour.startTime);
      tour.endTime = util.extractTime(tour.endTime);
      return tour;
    });
    if (this.tourFilter.model.column !== "nofilter") {
      this.tourFilter.updateFilter();
    }
    this.loadingIndicator = false;
  }

  /**
   * Filters the tours table using a column with a filter
   */
  filterToursTable = (col, filter) => {
    this.filteredTourData = this.tourData.filter(d => {
      const key = this.tourColumnsMapping[col];

      // In case we use an array value as column, we first need to convert it to a
      // string representation
      const cellVal = key ? d[key].toString() : "";
      return !filter ? 1 : cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
  }

  /**
  * Checks if this cell is linked to modal and set cursor to pointer
  * @param {any} row
  */
  getCellClass = ({row}) => {
    return {
      "text-white": this.model.selectedTour[0] ? row.tourId === this.model.selectedTour[0]["tourId"] : 0
    };
  }
}
