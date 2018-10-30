import {Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import { TableFilterComponent } from "../../components/table-filter/table-filter.component";
import { FeedbackService } from "../../services/feedback.service";
import { NetworkService } from "../../services/network.service";
import { ToastrService } from "ngx-toastr";
import { ModalWrapperComponent } from "../../components/modal-wrapper/modal-wrapper.component";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { UtilService } from "../../services/util.service";
import * as moment from "moment";
import { ServiceService } from "../../services/service.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: "app-service",
  templateUrl: "./feedback.component.html",
  styleUrls: ["./feedback.component.scss"],
})
export class FeedbackComponent implements OnInit, OnDestroy {
  @ViewChild("feedbackdetail") feedbackDetailModal: ModalWrapperComponent;
  @ViewChild("feedbackfilter") feedbackFilter: TableFilterComponent;
  @ViewChild("manageservice") ManageServiceModal: ModalWrapperComponent;

  @ViewChild("tableFinished") tableFinished: any;

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
  finishedFeedbackData = [];

  filteredFeedbackData = [];
  filteredFinishedFeedbackData = [];

  private loadingToast;


  // Loading Indicator for both feedback tables
  loadingIndicator = true;

  constructor(private feedbackService: FeedbackService,
    private route: ActivatedRoute,
    private networkService: NetworkService,
    private toastr: ToastrService,
    private serviceService: ServiceService,
    private util: UtilService) {
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

    await this.loadFeedbacks();

    setTimeout(() => {
      this.toastr.clear(this.loadingToast.toastId);
    });

    // listen to queryParams and filter accordingly
    this.route.queryParams.subscribe(async queryParams => {
      if (Object.keys(queryParams).length === 0) { return; }
      this.feedbackPreselect = { column: "Affected Vehicle / Stop / Line", filter: queryParams.ID };
      this.filterFeedbackTable(this.feedbackPreselect.column, this.feedbackPreselect.filter);
    });

    // Info: Do not refresh periodically anymore, because this will shuffle all feedbacks
    // and will destroy workflow
  }

  ngOnDestroy() {
    if (this.loadingToast) {
      this.toastr.clear(this.loadingToast.toastId);
    }
  }

  async loadFeedbacks() {
    this.loadingIndicator = true;
    const networkData = await this.networkService.getAllData();
    const allFeedbacks = await this.feedbackService.getAllFeedbacks();
    allFeedbacks.sort((a, b) => a.timestamp > b.timestamp ? 1 : -1);

    allFeedbacks.forEach(f => {
      // Target and Creation Time with Formatted Strings
      f.timestampFormatted = this.util.makeDateStr(f.timestamp);
      switch (f.type) {
        case "Vehicle":
          f.targetFormatted = f.targetId;
          break;
        case "Line":
          f.targetFormatted = networkData.lineIDtoName[f.targetId];
          break;
        case "Stop":
          f.targetFormatted = networkData.stopIDtoName[f.targetId];
          break;
        default:
          f.targetFormatted = f.targetId;
      }
      f.affected = f.targetFormatted === "" ? f.type : f.type + ": " + f.targetFormatted;
    });

    this.feedbackData = allFeedbacks.filter(f => !f.finished);
    this.finishedFeedbackData = allFeedbacks.filter(f => f.finished);

    // update feedback and finished-feedback table
    // set filtered data and refresh filter
    this.filteredFeedbackData = this.feedbackData;
    this.filteredFinishedFeedbackData = this.finishedFeedbackData;
    this.feedbackFilter.updateFilter();

    this.loadingIndicator = false;
  }

  /**
   * Filters the current events table using a column with a filter
   */
  filterFeedbackTable = (col, filter) => {
    this.filteredFeedbackData = this.feedbackData.filter(d => {
      const key = this.feedbackColumnMapping[col];

      // In case we use an array value as column, we first need to convert it to a
      // string representation
      const cellVal = key ? d[key].toString() : "";
      return !filter ? 1 : cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
    // Finished Table
    this.filteredFinishedFeedbackData = this.finishedFeedbackData.filter(d => {
      const key = this.feedbackColumnMapping[col];

      // In case we use an array value as column, we first need to convert it to a
      // string representation
      const cellVal = key ? d[key].toString() : "";
      return !filter ? 1 : cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
  }


  /**
   *
   * Feedback
   *
   */

  handleModal = (payload) => {
    if (payload.action === "setFinished") {
      this.finishFeedback(payload, null);
    } else if (payload.action = "openServiceRequest") {
      this.openServiceRequest(payload, null);
    }
  }

  /** click to finish feedback */
  finishFeedback = async (row, rowIndex, withSR = false) => {
    this.loadingIndicator = true;
    let result;
    if (!withSR) {
      result = await this.feedbackService.setFeedbackFinished(row.id);
    }
    if (withSR || result.ok) {
      const finishedFeedback = this.feedbackData.splice(this.feedbackData.indexOf(row), 1);
      finishedFeedback[0].newlyFinished = true;
      finishedFeedback[0].finished = true;
      // add new feedback always to the beginning of the new table
      this.finishedFeedbackData.unshift(finishedFeedback[0]);

      // refresh table data
      this.feedbackData = [...this.feedbackData];
      this.filteredFeedbackData = this.feedbackData;
      this.finishedFeedbackData = [...this.finishedFeedbackData];

      // refresh pending filter to see pending object
      this.feedbackFilter.updateFilter();

      // Go to first page of finished table to see finished feedback
      this.tableFinished.offset = 0;
    }
    this.loadingIndicator = false;
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
    this.ManageServiceModal.show(payload, { mode: "add" }, this.createService);
  }

  createService = async service => {
    service.dueDate = moment(service.dueDate).format("MMM Do YY");

    const editedService = Object.assign({}, service);
    service.feedback = service.feedback.map(fb => fb.itemName);
    editedService.feedback = editedService.feedback.map(fb => fb.id);

    editedService.referenceId = editedService.objectId;
    editedService.type = editedService.objectType;
    editedService.dueDate = moment(editedService.dueDate, "MMM Do YYY").valueOf();
    editedService.priority = this.serviceService.getPriority(editedService.priority);

    const result = await this.serviceService.createService(editedService);
    // Always set finished if creation was successfull, because backend is doing this
    if (result.ok) {
      editedService.feedback.forEach(fid => {
        // search for each feedback in data and set to finished
        this.feedbackData.forEach(fdata => {
          if (fid === fdata.id) {
            this.finishFeedback(fdata, null, true);
          }
        });
      });
    }
  }

  /**
   * Checks if the row is a newly finished row and makes it green with css class
   * @param row (of datatable)
   * @returns {{"feedback-finished": any}}
   */
  getRowClass(row) {
    return {
      "bg-success": row.newlyFinished,
      "row-finished": row.newlyFinished
    };
  }

  /**
   * Checks if this cell is linked to modal and set cursor to pointer
   * @param {any} row
   * @param {any} column
   * @param {any} value
   * @returns {any}
   */
  getCellClass({ row, column, value }): any {
    return {
      "text-white": row.newlyFinished,
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
   * Opens a feedback-detail modal
   * @param row (of datatable)
   */
  showDetail(row) {
    this.feedbackDetailModal.show(row, {}, this.handleModal.bind(this));
  }
}
