/**
* Config Service to get & set current live engine status.
*/
import { Injectable } from "@angular/core";
import { Http, Response } from "@angular/http";
import { Headers, RequestOptions } from "@angular/http";
import { ToastrService } from "ngx-toastr";

@Injectable()
export class ConfigService {

  constructor(private http: Http, private toastr: ToastrService) { }

  /** get current config */
  getConfig() {
    // @TODO: Uncomment once backend implemented endpoint
    return this.http.get("/rest/live-engine-config").toPromise()
      .then(result => {
        return result.json();
      });
  }

  // @TODO: Should be PUT?
  /** POST updated config */
  postConfig(config) {
    const loadingToast = this.toastr.info("Saving Config...", "Config Update", { disableTimeOut: true });
    const body = config;

    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });

    const req = this.http.post("/rest/live-engine-config", JSON.stringify(body), options)
      .toPromise()
      .then(result => {
        this.toastr.remove(loadingToast.toastId);

        if (result.ok) {
          this.toastr.success("Config updated!", "Config Update", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Config Update", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.remove(loadingToast.toastId);
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Config Update", { timeOut: 3000 });
        return error;
      });
    return req;
  }

  /** Download CSV file generated in backend */
  dumpCSV() {
    return this.http.get("/rest/csv").toPromise()
      .then(result => {
        return result;
      });
  }
}
