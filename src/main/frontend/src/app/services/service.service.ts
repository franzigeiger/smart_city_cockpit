/**
* Service service to manage server interaction for service requests
*/
import { Injectable } from "@angular/core";
import { Http, Response, Headers, RequestOptions } from "@angular/http";
import * as moment from "moment";
import { ToastrService } from "ngx-toastr";

@Injectable()
export class ServiceService {
  private data = [];

  constructor(private http: Http, private toastr: ToastrService) { }

  // mapping of service fields to column heading
  public servicesColumnMapping = {
    "name": "name",
    "priority": "priority",
    "due": "dueDate",
    "status": "state",
    "Affected Vehicle / Stop": "objectId",
    "type": "serviceType"
  };

  // get priority from integer value
  public getPriority(p: number | string) {
    if (typeof (p) === "number") {
      switch (p) {
        case 1: return "4 - immediate";
        case 2: return "3 - urgent";
        case 3: return "2 - normal";
        default: return "1 - low";
      }
    } else {
      switch (p) {
        case "4 - immediate": return 1;
        case "3 - urgent": return 2;
        case "2 - normal": return 3;
        default: return 7;
      }
    }
  }

  /**
  * Returns the dataArray or the promise, if the dataArray is not yet filled
  * @return {Array} dataArray | @return {Promise} promise
  */
  getData(): any[] | Promise<any> {
    return this.http.get("/rest/services").toPromise()
      .then(result => {
        this.data = result.json();
        this.data.forEach(service => {
          service.dueDate = moment(service.dueDate).format("MMM Do YY, hh:mm");
          service.priority = this.getPriority(service.priority);
        });
        return this.data;
      });
  }

  /** Create a new service*/
  createService(service) {
    const loadingToast = this.toastr.info("Creating Service...", "Service Creation", { disableTimeOut: true });
    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });
    console.log(service)
    const req = this.http.post("/rest/service/new", JSON.stringify(service), options)
      .toPromise()
      .then(result => {
        this.toastr.remove(loadingToast.toastId);

        if (result.ok) {
          this.toastr.success("Service created!", "Service Creation", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Service Creation", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.remove(loadingToast.toastId);
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Service Creation", { timeOut: 3000 });
        return error;
      });
    return req;
  }
}
