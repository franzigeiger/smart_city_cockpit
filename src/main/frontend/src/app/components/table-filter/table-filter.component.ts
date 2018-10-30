import { Component, OnInit, Input, ViewChild, AfterViewChecked, ChangeDetectorRef } from "@angular/core";

@Component({
  selector: "app-table-filter",
  templateUrl: "./table-filter.component.html",
  styles: []
})

export class TableFilterComponent implements OnInit, AfterViewChecked {
  @Input("columns") columns;
  @Input("preselect") preselect;
  @Input("callback") callback;

  public model = { column: "nofilter", filter: null };

  styles = {
    container: {
      "display": "flex",
      "flex-direction": "row",
    },
    input: {
      "marginLeft": "12px",
    }
  };

  constructor(private cdRef: ChangeDetectorRef) {
  }

  /** initialize filter with either array of columns or array of column objects (with name key).
  Secondly, if a filter has been preselected (object of column & filter), set the filter. */
  ngOnInit() {
    if (this.columns && this.columns[0].name) {
      this.columns = this.columns.map(col => col.name);
    }
  }

  ngAfterViewChecked() {
    if (this.preselect) {
      this.model.column = this.preselect.column;
      this.model.filter = this.preselect.filter;
      this.preselect = null;
      this.cdRef.detectChanges();
    }
    if (this.columns && this.columns[0].name) {
      this.columns = this.columns.map(col => col.name);
      this.cdRef.detectChanges();
    }
  }

  getFilter() {
    return this.model.filter;
  }

  /** called on keyup on filter input to invoke filter of parent table data */
  updateFilter() {
    if (this.model.column === "nofilter") {
      this.resetFilter();
    }
    // Call callback e.g. on line component with filtered column & filter
    this.callback(this.model.column, this.model.filter);
  }

  /** can be used by parent components to reset filtering to defaults */
  public resetFilter() {
    this.model.filter = null;
    this.model.column = "nofilter";
  }

}
