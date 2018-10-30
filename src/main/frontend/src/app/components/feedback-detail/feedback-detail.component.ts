import {Component, Input} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: "app-feedback-detail",
  templateUrl: "./feedback-detail.component.html",
  styleUrls: ["./feedback-detail.component.scss"]
})
export class FeedbackDetailComponent {
  @Input("payload") payload;
  @Input("parameters") parameters;

  constructor(public activeModal: NgbActiveModal) {
  }

  handleClosing(action) {
    this.payload.action = action;
    this.activeModal.close(this.payload);
  }
}
