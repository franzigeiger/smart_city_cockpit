import {Component, Input, OnInit} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: "app-event-detail",
  templateUrl: "./event-detail.component.html",
  styleUrls: ["./event-detail.component.scss"]
})
export class EventDetailComponent {
  @Input("payload") payload;
  @Input("parameters") parameters;

  constructor(public activeModal: NgbActiveModal) {
  }
}
