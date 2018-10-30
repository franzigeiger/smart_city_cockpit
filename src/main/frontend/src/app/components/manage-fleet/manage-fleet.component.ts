import { Component, OnInit, Input } from "@angular/core";
import { NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";


@Component({
  selector: "app-manage-fleet",
  templateUrl: "./manage-fleet.component.html",
  styles: []
})

export class ManageFleetComponent implements OnInit {
  @Input("payload") payload;
  @Input("parameters") parameters;

  model = {name: "", type: ""};


  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit () {
  }

}
