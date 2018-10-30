import { Component, Input } from "@angular/core";
import { NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import { NetworkService } from "../../services/network.service";

@Component({
  selector: "app-service-detail",
  templateUrl: "./service-detail.component.html",
  styles: []
})

export class ServiceDetailComponent {
  @Input("payload") payload;
  @Input("parameters") parameters;

  constructor(public activeModal: NgbActiveModal) {}
}
