/**
* Service for tours
*/
import { Injectable } from "@angular/core";
import { Http, Response, Headers, RequestOptions } from "@angular/http";
import { ToastrService } from "ngx-toastr";

@Injectable()
export class TourService {
  constructor(private http: Http, private toastr: ToastrService) { }

  /**
  * DELETE
  * Remove vehicle from tour
  * @param vehicleID: ID of vehicle
  * @param tourID: ID of tour
  */
  removeVehicleFromTour(vehicleID, tourID) {
    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({
      headers: headers
    });

    const req = this.http.delete("/rest/tours/removeVehicle/" + tourID + "/" + vehicleID, options)
      .toPromise()
      .then(result => {
        if (result.ok) {
          this.toastr.success("Vehicle removed!", "Vehicle Removal", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Vehicle Removal", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Vehicle Removal", { timeOut: 3000 });
        return error;
      });
    return req;
  }

  /**
  * POST
  * Reassign vehicle on tour
  * @param vehicleID: ID of vehicle
  * @param tourID: ID of tour
  */
  changeVehicleOnTour(vehicleID, tourID) {
    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });

    const req = this.http.post("/rest/tours/" + tourID, JSON.stringify({ vehicleId: vehicleID }), options)
      .toPromise()
      .then(result => {
        if (result.ok) {
          this.toastr.success("Vehicle assigned!", "Vehicle Assignment", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Vehicle Assignment", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Vehicle Assignment", { timeOut: 3000 });
        return error;
      });
    return req;
  }

  /**
  * DELETE tour
  * @param tourID: ID of tour
  */
  removeTour(tourID) {
    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });

    const req = this.http.delete("/rest/tours/deleteTour/" + tourID, options)
      .toPromise()
      .then(result => {
        if (result.ok) {
          this.toastr.success("Tour removed!", "Tour Removal", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Tour Removal", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Tour Removal", { timeOut: 3000 });
        return error;
      });
    return req;
  }

  /** Create a new tour */
  createTour(body) {
    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });

    const req = this.http.post("/rest/tours", JSON.stringify(body), options)
      .toPromise()
      .then(result => {
        if (result.ok) {
          this.toastr.success("Tour created!", "Tour Creation", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Tour Creation", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Tour Creation", { timeOut: 3000 });
        return error;
      });
    return req;
  }

  /** */
  getFreeTours(vehicleID, rangeStartTime, rangeEndTime) {
    const body = {
      vehicleId: vehicleID,
      rangestarttime: rangeStartTime,
      rangeendtime: rangeEndTime
    };
    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });
    const req = this.http.post("/rest/tours/getFreeTours", JSON.stringify(body), options)
      .toPromise()
      .then(result => result.json());
    return req;
  }

  /**
   * This Returns all Tours with unassigened Vehicles
   * it is used for the Status Page
   * @returns {Promise<Response>}
   */
  getAllFreeToursForStatus() {
    return this.http.get("/rest/freeTours").toPromise().then(result => {
      return result.json();
    });
  }
}
