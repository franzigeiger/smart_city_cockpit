/**
* Stops service to get and set current stop state (open/closed)
*/
import { Injectable } from "@angular/core";
import { Http, Response, Headers, RequestOptions } from "@angular/http";
import { ToastrService } from "ngx-toastr";

@Injectable()
export class StopsService {

  constructor(private http: Http, private toastr: ToastrService) { }

  // gets status (open / closed) for all stops
  getStopsState() {
    return this.http.get("/rest/stops/closed").toPromise()
      .then(result => result.json())
      .catch(error => {
        console.log("Couldn't fetch updated stops", error);
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")",
          "Station Status", { timeOut: 3000 });
        return error;
      });
  }

  toggleStopStatus(stopId) {
    const loadingToast = this.toastr.info("Updating Stop Status...", "Stop Status", { disableTimeOut: true });
    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });

    const req = this.http.post("/rest/stops/closed/" + stopId, JSON.stringify({}), options)
      .toPromise()
      .then(result => {
        this.toastr.remove(loadingToast.toastId);

        if (result.ok) {
          this.toastr.success("Stop Status updated!", "Stop Status", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Stop Status", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.remove(loadingToast.toastId);
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Stop Status", { timeOut: 3000 });
        return error;
      });
    return req;
  }
}
