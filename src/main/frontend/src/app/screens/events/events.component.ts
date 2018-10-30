import {ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {TableFilterComponent} from "../../components/table-filter/table-filter.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ModalWrapperComponent} from "../../components/modal-wrapper/modal-wrapper.component";
import {EventService} from "../../services/event.service";
import {UtilService} from "../../services/util.service";
import {ToastrService} from "ngx-toastr";
import {DatatableComponent} from "@swimlane/ngx-datatable";

@Component({
  selector: "app-service",
  templateUrl: "./events.component.html",
  styleUrls: ["./events.component.scss"],
})
export class EventsComponent implements OnInit, OnDestroy {
  @ViewChild("eventsfilter") eventsFilter: TableFilterComponent;
  @ViewChild("manageevent") ManageEventModal: ModalWrapperComponent;
  @ViewChild("eventdetail") eventDetailModal: ModalWrapperComponent;

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

  eventsPreselect = null;
  eventsColumnMapping = {
    "Title": "title",
    "Start Date/Time": "startDateTimeFormatted",
    "End Date/Time": "endDateTimeFormatted"
  };
  eventsColumns = [{
    name: "Title",
    prop: "title",
    flexGrow: 2
  }, {
    name: "Start Date/Time",
    prop: "startDateTimeFormatted",
    flexGrow: 2.4
  }, {
    name: "End Date/Time",
    prop: "endDateTimeFormatted",
    flexGrow: 2.4
  }];

  currentEventsData = [];
  oldEventsData = [];
  upcomingEventsData = [];

  filteredCurrentEventsData = [];
  filteredOldEventsData = [];
  filteredUpcomingEventsData = [];

  loadingToast;

  showOldEvents = false;
  loadingIndicator = true;


  constructor(private modalService: NgbModal,
              private eventService: EventService,
              private util: UtilService,
              private toastr: ToastrService) {
  }

  async ngOnInit() {
    setTimeout(() => {
      this.loadingToast = this.toastr.info(this.util.spinner, "Loading...", {
        disableTimeOut: true,
        enableHtml: true
      });
    });

    await this.loadEvents();

    setTimeout(() => {
      this.toastr.clear(this.loadingToast.toastId);
    });

  }

  ngOnDestroy() {
    if (this.loadingToast) {
      this.toastr.clear(this.loadingToast.toastId);
    }

  }

  /**
   * Loads all events and filters the tables
   * @returns {Promise<void>}
   */
  async loadEvents() {
    this.loadingIndicator = true;
    const allData = await this.eventService.getAllData();

    allData.forEach(e => {
      // replace Date Millis with formatted Date
      e.startDateTimeFormatted = this.util.makeDateStr(e.start);
      e.endDateTimeFormatted = this.util.makeDateStr(e.end);
    });

    // put all events into categories (current, upcoming, old)
    this.currentEventsData = allData.filter(e => !(e.end < Date.now()) && (e.start < Date.now()));
    this.upcomingEventsData = allData.filter(e => !(e.end < Date.now()) && !(e.start < Date.now()));
    this.oldEventsData = allData.filter(e => e.end < Date.now());

    this.eventsFilter.updateFilter();

    this.loadingIndicator = false;
  }

  /**
   * Filters the all event tables using a column with a filter
   */
  filterCurrentEventsTable = (col, filter) => {
    this.filteredCurrentEventsData = this.currentEventsData.filter(d => {
      const key = this.eventsColumnMapping[col];

      // In case we use an array value as column, we first need to convert it to a
      // string representation
      const cellVal = key ? d[key].toString() : "";
      return !filter ? 1 : cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
    this.filteredUpcomingEventsData = this.upcomingEventsData.filter(d => {
      const key = this.eventsColumnMapping[col];
      const cellVal = key ? d[key].toString() : "";
      return !filter ? 1 : cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
    this.filteredOldEventsData = this.oldEventsData.filter(d => {
      const key = this.eventsColumnMapping[col];
      const cellVal = key ? d[key].toString() : "";
      return !filter ? 1 : cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
  }

  /**
   *
   * EVENTS
   *
   */

  /** add event button */
  addEvent() {
    this.ManageEventModal.show({}, {mode: "add"}, this.saveEvent.bind(this));
  }

  async saveEvent(event) {
    this.loadingIndicator = true;
    const result = await this.eventService.addEvent(event);
    if (result.ok) {
      this.loadEvents();
    } else {
      this.loadingIndicator = false;
    }
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

  /** Listener for datatable
   * onclick open Modal
   * @param event
   */
  onActivate(event) {
    if (event.type === "click" && event.column.prop !== "actions") {
      event.cellElement.blur();
      this.showDetail(event.row);
    }
  }

  /**
   * Opens a event-detail modal
   * @param row (of datatable)
   */
  showDetail(row) {
    this.eventDetailModal.show(row, {});
  }

}
