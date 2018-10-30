/**
* Network service to get all persistent netword data (lines, stations, routes)
*/
import { Injectable } from "@angular/core";
import { Http, Response } from "@angular/http";

@Injectable()
export class TimetableService {

  constructor(private http: Http) { }

  /** get timetable data for a line */
  getTimetable (lineId) {
    return this.http.get("/rest/timetables/" + lineId).toPromise()
      .then(result => {
        return result.json();
      });
  }

  /** returns available vehicles for a line */
  getAvailableVehicles (lineId, startTime) {
    return this.http.get("/rest/availableVehicles/" + lineId + "?startTime=" + startTime).toPromise()
      .then(result => {
        return result.json();
      });
  }
}
