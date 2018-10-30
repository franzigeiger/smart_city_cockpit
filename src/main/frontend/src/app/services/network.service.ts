/**
* Network service to get all persistent netword data (lines, stations, routes)
*/
import { Injectable } from "@angular/core";
import { Http, Response } from "@angular/http";
import { UtilService } from "./util.service";

@Injectable()
export class NetworkService {
  private data;
  private lineData = {};

  constructor(private http: Http, private util: UtilService) { }

  /**
  * Returns the dataArray or the promise, if the dataArray is not yet filled
  * @return {Array} dataArray | @return {Promise} promise
  */
  getAllData() {
    if (!this.data) {
      return this.http.get("/rest/network").toPromise()
        .then(result => {
          const data = result.json();
          // map type (bus, tube) to lines
          const lineIDtoType = {};
          // map stations to their lines
          const stationToLine = {};
          // map line ids to their names
          const lineIDtoName = {};
          // map line ids to their colors
          const lineIDtoColor = {};
          // map stop ids to their names
          const stopIDtoName = {};
          data.forEach(line => {

            lineIDtoType[line.lineID] = line.type;
            lineIDtoName[line.lineID] = line.name;
            lineIDtoColor[line.lineID] = line.color;

            line.stops.forEach(stop => {
              if (stationToLine[stop.id]) {
                stationToLine[stop.id].push(line.lineID);
              } else {
                stationToLine[stop.id] = [line.lineID];
              }
              if (!stopIDtoName[stop.id]) {
                stopIDtoName[stop.id] = stop.name;
              }
            });
          });
          return this.data = { lines: data, stationToLine, lineIDtoName, lineIDtoColor, stopIDtoName, lineIDtoType };
        })
        .catch(error => {
          console.log("Couldn't fetch network", error);
          // @TODO: Add Error Toast.
          return null;
        });
    } else { return this.data; }
  }

  /** Returns current vehicles of line for live display */
  // currently very similar to getLineData, but might change when we optimize
  // by splitting requests in backend.
  getCurrentVehicles(lineId) {
    return this.http.get("/rest/network/lines/" + lineId).toPromise()
      .then(result => result.json().vehicles)
      .catch(error => {
        console.log("Couldn't fetch vehicles", error);
        // @TODO: Add Error Toast.
        return null;
      });
  }
  /**
  * Returns the the promise which returns, if fulfilled, all network problems
  * @return {Promise} promise
  */
  getState(lineId = "") {
    return this.http.get("/rest/network/state").toPromise()
      .then(result => {
        const data = result.json();
        const lineStates = [];
        const lineIDtoLineStates = {};
        data.forEach(line => {
          if (lineId === "" || line.id === lineId) {
            const stopIdToStopStates = {};
            line.stops.forEach(stop => {
              stopIdToStopStates[stop.id] = stop;
            });
            const lineState = {
              lineID: line.id,
              type: line.type,
              state: line.state,
              problems: line.problems,
              stops: line.stops,
              stopIdToStopStates
            };
            lineStates.push(lineState);
            lineIDtoLineStates[line.id] = lineState;
          }
        });
        return { lineStates, lineIDtoLineStates };
      })
      .catch(error => {
        console.log("Couldn't fetch network state", error);
        // @TODO: Add Error Toast.
        return null;
      });
  }

  getLineData(lineId) {
    if (!this.lineData[lineId]) {
      console.log("getting lineData for line " + lineId + "...");
      return this.http.get("/rest/network/lines/" + lineId).toPromise()
        .then(result => {
          this.lineData = result.json();
          return this.lineData;
        })
        .catch(error => {
          console.log("Couldn't fetch line data", error);
          // @TODO: Add Error Toast.
          return null;
        });
    } else { return this.lineData; }
  }
}
