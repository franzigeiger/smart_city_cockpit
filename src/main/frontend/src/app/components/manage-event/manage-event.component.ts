import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

import * as moment from "moment";


@Component({
  selector: "app-manage-event",
  templateUrl: "./manage-event.component.html",
  styles: []
})

export class ManageEventComponent {

  @Input("payload") payload;
  @Input("parameters") parameters;

  model = {
    title: "",
    description: "",
    startPicker: null,
    endPicker: null,
    start: 0,
    end: 0,
    location: "",
    involved: ""
  };

  involvedParties = [
    "Chelsea Football Club",
    "Arsenal Football Club",
    "Twickenham Stoop Stadium",
    "London Theatre Direct Ltd",
    "Delfont Mackintosh Theatres Ltd",
    "Festival Republic",
    "Montefiore Centre",
    "English National Ballet",
    "Royal Opera House",
    "People’s March for Europe"
  ];

  errorMessage = "";
  warningMessage = "";

  constructor(public activeModal: NgbActiveModal) {
  }

  /**
   * Detects if start and end date are set correctly and sets errorMessage
   * or if the event is in the past and show hint
   */
  compareStartEndDate() {
    this.model.start = this.readDate(this.model.startPicker);
    this.model.end = this.readDate(this.model.endPicker);
    if (this.model.start > this.model.end) {
      this.errorMessage = "End Date cannot be before start date.";
      this.warningMessage = "";
    } else {
      this.errorMessage = "";
      if (this.model.end < +moment()) {
        this.warningMessage = "Be aware that you are creating an event in the past.";
      } else {
        this.warningMessage = "";
      }
    }
  }

  /**
   * Replaces the Date value of the DatePickers to Millis for backend
   * @param model
   * @returns {any} (modified model)
   */
  refreshModel(model) {
    this.model.start = this.readDate(this.model.startPicker);
    this.model.end = this.readDate(this.model.endPicker);
    return model;
  }

  // Converts Date to Millis
  readDate = date => +moment(date);

}
