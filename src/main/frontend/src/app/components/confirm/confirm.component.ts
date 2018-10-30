import { Component, OnInit, Input } from "@angular/core";
import { NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";


@Component({
  selector: "app-confirm",
  templateUrl: "./confirm.component.html",
  styles: []
})

export class ConfirmComponent implements OnInit {
  @Input("payload") payload;
  @Input("parameters") parameters;

  model = {header: "", description: ""};


  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit () {
    Object.assign(this.model, this.payload);
  }

}
