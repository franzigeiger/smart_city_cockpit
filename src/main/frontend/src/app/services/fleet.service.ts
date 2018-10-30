/**
* Fleet service to get all vehicles / their problems / their shiftplan
*/
import { Injectable } from "@angular/core";
import { Http, Response, Headers, RequestOptions } from "@angular/http";
import { ToastrService } from "ngx-toastr";
import { UtilService } from "./util.service";

@Injectable()
export class FleetService {
  constructor(private http: Http, private toastr: ToastrService, private util: UtilService) { }

  /**
  * returns all vehicles of fleet
  * @return {Promise} promise
  */
  getAllData() {
    return this.http.get("/rest/fleet").toPromise()
      .then(result => {
        const data = result.json();
        const vehicleIDtoType = {};
        data.forEach(vehicle => {
          vehicleIDtoType[vehicle.vehicleID] = vehicle.type;
        });
        return { data, vehicleIDtoType };
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Fleet Data", { timeOut: 3000 });
        return error;
      });
  }

  /**
   * Returns the the promise which returns, if fulfilled, all fleet problems
   * @return {Promise} promise
   */
  getState() {
    return this.http.get("/rest/fleet/state").toPromise()
      .then(result => {
        const data = result.json();
        const problems = data.map(vehicle => {
          return { vehicleID: vehicle.id, type: vehicle.type, state: vehicle.state, problems: vehicle.problems };
        });
        return problems;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Fleet State", { timeOut: 3000 });
        return error;
      });
  }

  /**
  * Returns shift plan of passed vehicle
  * @param {String} vehicleID
  * @return {Promise} promise
  */
  getShiftPlan(vehicleID) {
    return this.http.get("/rest/fleet/shiftplan/" + vehicleID).toPromise()
      .then(result => {
        const data = result.json();
        return data;
      });
  }

  /** POST new vehicle
  * @param {String} name of vehicle
  * @param {String} type of vehicle
  * @return {Promise} promise
  */
  createVehicle(name, type) {
    const body = { name, type };

    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });
    const req = this.http.post("/rest/fleet", JSON.stringify(body), options)
      .toPromise()
      .then(result => {
        if (result.ok) {
          this.toastr.success("Vehicle created!", "Vehicle Creation", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Vehicle Creation", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Vehicle Creation", { timeOut: 3000 });
        return error;
      });

    return req;
  }

  /** DELETE vehicle from fleet (shutdown)
  * @param {String} vehicleID: ID of vehicle
  * @return {Promise} promise
  */
  deleteVehicle(vehicleID) {
    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });

    const req = this.http.delete("/rest/fleet/" + vehicleID, options)
      .toPromise()
      .then(result => {
        if (result.ok) {
          this.toastr.success("Vehicle shutdown!", "Vehicle is shut down", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Vehicle shutdown", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Vehicle shutdown", { timeOut: 3000 });
        return error;
      });
    return req;
  }

  /** GET: Replacement for vehicles
  * @param {String} vehicleID: Id of vehicle
  * @return {Promise} promise
  */
  getReplacementVehicle(vehicleID) {
    return this.http.get(`/rest/getReplacementFor/${vehicleID}`).toPromise()
      .then(result => {
        return result.json();
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "GET Replacement Vehicle", { timeOut: 3000 });
        return error;
      });
  }

  /** POST: Replacement for vehicles
  * @param {String} oldVehicleID: Id of vehicle which is to be replaced
  * @param {String} newVehicleID: Id of replacement vehicle
  * @return {Promise} promise
  */
  setReplacementVehicle(oldVehicleID, newVehicleID) {
    const headers = new Headers({ "Content-Type": "application/json" });
    const options = new RequestOptions({ headers: headers });
    const loadingToast = this.toastr.info(this.util.spinner, "Replace vehicle...", { disableTimeOut: true, enableHtml: true });
    return this.http.post(`/rest/setReplacementFor/${oldVehicleID}/${newVehicleID}`, {}, options).toPromise()
      .then(result => {
        this.toastr.remove(loadingToast.toastId);
        if (result.ok) {
          this.toastr.success("Vehicle replaced!", "Vehicle Replacement", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Vehicle Replacement", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.remove(loadingToast.toastId);
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")",
          "POST Replacement Vehicle", { timeOut: 3000 });
        return error;
      });
  }
}
