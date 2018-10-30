/**
* Notifications service to handle notification interaction (especiall creation / update)
*/
import { Injectable } from "@angular/core";
import { Http, Response } from "@angular/http";
import { Headers, RequestOptions } from "@angular/http";
import { ToastrService } from "ngx-toastr";

@Injectable()
export class NotificationsService {

  constructor(private http: Http, private toastr: ToastrService) { }

  /** POST new notification */
  createNotification(stops, description, lineId) {
    const body = {
      stops: stops,
      description: description,
      lineId: lineId
    };

    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });

    const req = this.http.post("/rest/notifications", JSON.stringify(body), options)
      .toPromise()
      .then(result => {
        if (result.ok) {
          this.toastr.success("Notification created!", "Notification Creation", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Notification Creation", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Notification Creation", { timeOut: 3000 });
        return error;
      });
    return req;
  }

  /** DELETE notification */
  deleteNotification(stops, description, lineId) {
    const body = {
      stops: stops,
      description: description,
      lineId: lineId
    };

    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });

    const req = this.http.post("/rest/notifications/delete", JSON.stringify(body), options)
      .toPromise()
      .then(result => {
        if (result.ok) {
          this.toastr.success("Notification deleted!", "Notification Deletion", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Notification Deletion", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Notification Deletion", { timeOut: 3000 });
        return error;
      });
    return req;
  }
}
