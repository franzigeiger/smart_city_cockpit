import { Component, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { TableFilterComponent } from "../../components/table-filter/table-filter.component";
import { ServiceService } from "../../services/service.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { ModalWrapperComponent } from "../../components/modal-wrapper/modal-wrapper.component";
import { ActivatedRoute } from "@angular/router";
import * as moment from "moment";
import { ToastrService } from "ngx-toastr";
import { UtilService } from "../../services/util.service";

@Component({
  selector: "app-service",
  templateUrl: "./service.component.html",
  styles: []
})
export class ServiceComponent implements OnInit, OnDestroy {
  @ViewChild("servicesfilter") servicesFilter: TableFilterComponent;
  @ViewChild("doneservicesfilter") doneServicesFilter: TableFilterComponent;
  @ViewChild("manageservice") ManageServiceModal: ModalWrapperComponent;
  @ViewChild("servicedetail") ServiceDetailModal: ModalWrapperComponent;
  @ViewChild("table") table: any;

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

  // @TODO:  Currently, there is quite some code duplication between this and the lineview service requests table.
  //         If we have enough time, we could think about creating a component that can be used by both. On the other hand,
  //         perhaps it's best to keep them separated since they might evolve into different directions in a future build.
  servicesPreselect = null;
  servicesColumns = Object.keys(this.serviceService.servicesColumnMapping)
    .map(key => ({ name: key, prop: this.serviceService.servicesColumnMapping[key] }));
  servicesData = [];
  filteredServicesData = [];

  doneServicesData = [];
  filteredDoneServicesData = [];

  private loadingToast;
  public selectedRow = [];

  constructor(private modalService: NgbModal, private serviceService: ServiceService, private route: ActivatedRoute, private toastr: ToastrService,
    private util: UtilService) { }

  isDone = done => (done === "done");

  async ngOnInit() {
    setTimeout(() => {
      this.loadingToast = this.toastr.info(this.util.spinner, "Loading...", { disableTimeOut: true, enableHtml: true });
    });

    const allData = await this.serviceService.getData();
    allData.forEach(d => { d.dueDate = this.util.formatServicesDate(d.dueDate); });
    this.servicesData = allData.filter(r => !this.isDone(r.status));
    this.doneServicesData = allData.filter(r => this.isDone(r.status));

    this.filteredServicesData = this.servicesData;
    this.filteredDoneServicesData = this.doneServicesData;

    // listen to queryParams and filter accordingly
    this.route.queryParams.subscribe(async queryParams => {
      if (Object.keys(queryParams).length === 0) { return; }
      if (queryParams.ID) {
        this.servicesPreselect = { column: "Affected Vehicle / Stop", filter: queryParams.ID };
      }
      if (queryParams.type) {
        this.servicesPreselect = { column: "type", filter: queryParams.type };
      }
      this.updateFilters();
    });

    setTimeout(() => {
      this.toastr.clear(this.loadingToast.toastId);
    });
  }

  ngOnDestroy() {

    if (this.loadingToast) {
      this.toastr.clear(this.loadingToast.toastId);
    }
  }

  toggleExpandGroup(group) {
    this.table.groupHeader.toggleExpandGroup(group);
  }

  onActivate(event) {
    this.selectedRow = [];
    if (event.type === "click" && event.column.name !== "Actions") {
      event.cellElement.blur();
      this.showDetail(event.row);
    }
  }

  showDetail(row) {
    this.ServiceDetailModal.show(row, {});
  }

  /**
  * Use preselects from queryParams to update table filter
  */
  updateFilters() {
    this.filterServicesTable(this.servicesPreselect.column, this.servicesPreselect.filter);
    this.filterDoneServicesTable(this.servicesPreselect.column, this.servicesPreselect.filter);
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
   * Filters the done services table using a column with a filter
   */
  filterDoneServicesTable = (col, filter) => {
    this.filteredDoneServicesData = this.doneServicesData.filter(d => {
      const key = this.serviceService.servicesColumnMapping[col];

      // In case we use an array value as column, we first need to convert it to a
      // string representation
      const cellVal = key ? d[key].toString() : "";
      return cellVal.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    });
  }

  /**
   *
   * SERVICES
   *
   */

  /** add service button */
  addService() {
    this.ManageServiceModal.show({}, { mode: "add" }, this.createService);
    this.resetFilters("services");
  }

  createService = service => {
    service.dueDate = this.util.makeDateStr(service.dueDate);
    service.state = "Open";

    const editedService = Object.assign({}, service);
    service.feedback = service.feedback.map(fb => fb.itemName);
    editedService.feedback = editedService.feedback.map(fb => fb.id);

    this.servicesData.push(service);
    // force re-render (deep edits aren't detected)
    const shallowCopy = [...this.servicesData];
    this.servicesData = shallowCopy;

    editedService.referenceId = editedService.objectId;
    editedService.type = editedService.objectType;
    editedService.dueDate = moment(editedService.dueDate).valueOf();
    editedService.priority = this.serviceService.getPriority(editedService.priority);

    this.resetFilters("services");
    this.serviceService.createService(editedService);
  }


  /** resets table for selected IDs
  id: notification / services / feedback / all */
  resetFilters(id) {
    if (id === "services" || "all") { // TODO still incorrect
      this.filteredServicesData = this.servicesData;
      this.servicesFilter.resetFilter();
    }
  }

}
