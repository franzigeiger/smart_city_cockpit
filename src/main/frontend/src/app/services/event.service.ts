/**
 * Network service to get all persistent netword data (lines, stations, routes)
 */
import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Headers, Http, RequestOptions } from "@angular/http";
import { ToastrService } from "ngx-toastr";

@Injectable()
export class EventService {

  private eventData;

  constructor(private http: Http, private toastr: ToastrService) {
  } // Http is deprecated, use HttpClient instead

  /**
   * Returns the dataArray or the promise, if the dataArray is not yet filled
   * @return {Array} dataArray | @return {Promise} promise
   */
  getAllData() {
      return this.http.get("/rest/events").toPromise()
        .then(result => {
          return this.eventData = result.json();
        });
  }

  /**
   * POST-Request to save Event into SAP
   * @param event Event Object with parameters for Event-Rest-Endpoint
   * @returns {Promise<boolean>}
   */
  addEvent(event) {
    const body = event;
    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });

    return this.http.post("/rest/event/new", body, options).toPromise()
      .then(result => {
        if (result.ok) {
          this.toastr.success("Event created!", "Event Creation", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Event Creation", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Event Creation", { timeOut: 3000 });
        return error;
      });
  }
}
