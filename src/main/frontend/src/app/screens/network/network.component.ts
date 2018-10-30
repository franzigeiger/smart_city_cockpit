import { Component, OnInit, ViewChild, OnDestroy, AfterViewChecked } from "@angular/core";
import { Router } from "@angular/router";
import { NetworkService } from "../../services/network.service";
import { D3Service, D3 } from "d3-ng2-service";
import { UtilService } from "../../services/util.service";
import { ToastrService } from "ngx-toastr";

@Component({
  selector: "app-network",
  templateUrl: "./network.component.html",
  styleUrls: ["./network.component.scss"]
})
export class NetworkComponent implements OnInit, OnDestroy, AfterViewChecked {

  tableData = [];
  statusStyles = {
    "OK": {
      style: {
        "color": "#28a745"
      },
      icon: "check-circle",
      btn: "danger"
    },
    "Warning": {
      style: {
        "color": "#ffc107"
      },
      icon: "exclamation",
      btn: "warning"
    },
    "Critical": {
      style: {
        "color": "#dc3545"
      },
      icon: "exclamation-triangle"
    }
  };

  selectLayerStyle = {
    "display": "inline",
    "width": "auto",
    "height": "auto",
    "font-size": "1.3rem"
  };

  problemLogCards = [];
  // loadingIndicator for Newsfeed network table
  loadingIndicator = true;

  selectedLayer = "default";
  layerList = [
    { id: "default", itemName: "Default" },
    { id: "toner-lite", itemName: "Toner" },
    { id: "terrain", itemName: "Terrain" },
    { id: "watercolor", itemName: "Watercolor" }
  ];
  private layers = {
    "default": new ol.layer.Tile({
      source: new ol.source.OSM()
    }),
    "custom": layer => new ol.layer.Tile({
      source: new ol.source.Stamen({
        layer
      })
    })
  };

  private d3: D3;
  private lines;
  private lineStates;
  private lineIDtoLineStates;
  private stationToLine;
  private lineIDtoName;
  private lineIDtoColor;
  private lineIDtoType;
  private stopIDtoName;
  private loadingToast;

  private layer1;
  private layer2;

  private map;
  private mapID = "#map";

  private xColumn = "lon";
  private yColumn = "lat";
  private svg;
  private g;
  private tooltip;
  private posFunc;
  private filteredLines = [];
  private interval;
  private intervalTime = 10000;

  private radius = type => (type === "bus" ? 5 : 10);
  private strokeWidth = type => (type === "bus" ? 1 : 3);



  constructor(private d3Service: D3Service, private networkService: NetworkService, private router: Router, private util: UtilService,
    private toastr: ToastrService) {
    this.d3 = d3Service.getD3();
  }

  /** @async Fetches all relevant data from network service */
  async ngOnInit() {
    setTimeout(() => {
      this.loadingToast = this.toastr.info(this.util.spinner, "Loading...", { disableTimeOut: true, enableHtml: true });
    });

    const d3 = this.d3;
    const allNetworkData = await this.networkService.getAllData();
    this.lines = allNetworkData.lines;
    this.stationToLine = allNetworkData.stationToLine;
    this.lineIDtoName = allNetworkData.lineIDtoName;
    this.lineIDtoColor = allNetworkData.lineIDtoColor;
    this.lineIDtoType = allNetworkData.lineIDtoType;
    this.stopIDtoName = allNetworkData.stopIDtoName;

    this.renderMap();
    this.fillNewsfeed();
    // refill Newsfeed table every 10 seconds
    this.interval = setInterval(_ => {
      this.fillNewsfeed();
      this.filterNetwork(null, null);
    }, this.intervalTime);

    setTimeout(() => {
      this.toastr.clear(this.loadingToast.toastId);
    });
  }

  ngOnDestroy() {
    clearInterval(this.interval);
    if (this.loadingToast) {
      this.toastr.clear(this.loadingToast.toastId);
    }
  }

  /** Filter problems again after liveEngine update*/
  ngAfterViewChecked() {
    this.filterProblems();
  }

  /** Fill Newsfeed table */
  async fillNewsfeed() {
    this.loadingIndicator = true;
    const d3 = this.d3;
    const networkState = await this.networkService.getState();
    this.lineStates = networkState.lineStates;
    this.lineIDtoLineStates = networkState.lineIDtoLineStates;

    // reset tableData and currentProblems and problemLogCards
    this.tableData = [];
    this.problemLogCards = [];
    // reset stroke-colors
    d3.selectAll("circle").attr("stroke", "#000");
    // fill in new data
    this.lineStates.forEach(line => {
      const obj = {};
      obj["lineID"] = line.lineID;
      obj["status"] = line.state === "Red" ? "Critical" : (line.state === "Yellow" ? "Warning" : "OK");
      obj["type"] = this.lineIDtoType[line.lineID] === "tube" ?
        `<i class="newsfeed fa fa-subway" aria-hidden="true"></i>` :
        `<i class="newsfeed fa fa-bus" aria-hidden="true"></i>`;
      obj["line"] = this.lineIDtoName[line.lineID];
      this.tableData.push(obj);
      for (const problem of line.problems) {

        // this.currentProblems[key2] = probs[key2].description;
        const severity = problem.severity;
        const btn = problem.state === "Yellow" ? "warning" : "danger";

        // calculate url for station / line
        const url = `/#/line/${line.lineID}`;
        this.problemLogCards.push({
          title: this.lineIDtoName[line.lineID],
          description: problem.description,
          type: "Line",
          url,
          btn: `btn btn-${btn}`,
          lineID: line.lineID
        });
      }

      for (const stop of line.stops) {
        for (const problem of stop.problems) {

          const severity = problem.severity;
          const btn = stop.state === "Yellow" ? "warning" : "danger";

          // calculate url for station / line
          const url = `/#/line/${line.lineID}?stopID=${stop.id}`;
          this.problemLogCards.push({
            title: this.util.replaceUndergroundStationTextWithIcon(this.stopIDtoName[stop.id]),
            description: problem.description,
            type: "Stop",
            url,
            btn: `btn btn-${btn}`,
            lineID: line.lineID
          });

          // style stations according to their problem severity
          d3.selectAll("circle").filter((d: any) => d.id === stop.id && stop.state === "Yellow").attr("stroke", "#ffc107");
          d3.selectAll("circle").filter((d: any) => d.id === stop.id && stop.state === "Red").attr("stroke", "#dc3545");
        }
      }
    }
    );

    // sort Problem Log - Lines first
    this.problemLogCards.sort((a, b) => {
      return (a.type === "Stop" && b.type === "Line" ? 1 : -1);
    });
    // Hide loading indicator of newsfeed table
    this.loadingIndicator = false;
  }


  /** Start render process of Open street maps */
  renderMap(): void {
    const that = this;
    this.map = new ol.Map({
      controls: ol.control.defaults().extend([
        new ol.control.FullScreen()
      ]),
      target: "map",
      layers: [
        this.layers.default
      ],
      view: new ol.View({
        center: ol.proj.fromLonLat([-0.13, 51.5412]),
        zoom: 10.7
      })
    });

    this.map.once("precompose", function() {
      that.posFunc = that.init_ol_d3(this);
      that.drawNetwork();
    });
  }

  /** Draw stations and lines on network map */
  drawNetwork() {
    const d3 = this.d3;
    // create tooltip
    this.tooltip = d3.select(this.mapID)
      .append("div")
      .attr("class", "d3-tooltip");
    this.g = d3.select("#map .d3-layer svg > g");

    // lines
    this.lines.forEach((v, k) => {
      const data = v.stops.map(stop => Object.assign({}, stop, { lines: [v.lineID] }, { type: v.type }));

      const lineWidth = v.type === "bus" ? 5 : 8;

      this.g.selectAll(`line${v.lineID}`).data(data)
        .enter().append("line")
        .on("click", _ => {
          this.router.navigate(["/line", v.lineID]);
        })
        .on("mousemove", this.mouseMove.bind(this))
        .on("mouseover", this.mouseOverLine.bind(this))
        .on("mouseout", this.mouseOut.bind(this))
        .attr("stroke", `#${v.color}`)
        .attr("stroke-width", lineWidth)
        .attr("stroke-opacity", 0.8)
        .style("cursor", "pointer")
        .attr("x1", d => this.posFunc([+d[this.xColumn], +d[this.yColumn]])[0])
        .attr("y1", d => this.posFunc([+d[this.xColumn], +d[this.yColumn]])[1])
        .attr("x2", (d, i) => v.stops[i + 1] ?
          this.posFunc([v.stops[i + 1][this.xColumn], +v.stops[i + 1][this.yColumn]])[0]
          :
          this.posFunc([+d.lon, +d.lat])[0])
        .attr("y2", (d, i) => v.stops[i + 1] ?
          this.posFunc([+v.stops[i + 1][this.xColumn], +v.stops[i + 1][this.yColumn]])[1]
          :
          this.posFunc([+d.lon, +d.lat])[1]);
    });
    // stations
    const ids = [];
    this.lines.forEach((v, k) => {

      // Add the available lines to each stop
      const data = v.stops.map(stop => Object.assign({}, stop, { lines: this.stationToLine[stop.id] }, { type: v.type }));
      // create all stations and assign the corresponding event handlers
      this.g.selectAll(`circle${v.lineID}`).data(data)
        .enter().append("circle")
        .on("click", d => this.clickOnStation(d))
        .on("mousemove", this.mouseMove.bind(this))
        .on("mouseover", this.mouseOverStation.bind(this))
        .on("mouseout", this.mouseOut.bind(this))
        .attr("id", d => d.id)
        .attr("cx", d => this.posFunc([+d.lon, +d.lat])[0])
        .attr("cy", d => this.posFunc([+d.lon, +d.lat])[1])
        .attr("r", d => this.radius(v.type))
        .attr("stroke", "#000")
        .attr("stroke-width", this.strokeWidth(v.type))
        .style("cursor", "pointer")
        .attr("fill", d => {
          const lines = this.stationToLine[d.id];
          let color = `#${v.color}`;
          let color1, color2, color3;
          if (lines.length === 2) {
            color1 = this.lineIDtoColor[lines[0]];
            color2 = this.lineIDtoColor[lines[1]];
            color = this.genGradient2([color1, color2]);
          } else if (lines.length > 2) {
            color1 = this.lineIDtoColor[lines[0]];
            color2 = this.lineIDtoColor[lines[1]];
            color3 = this.lineIDtoColor[lines[2]];
            color = this.genGradient3([color1, color2, color3]);
          }
          return color;
        });
    });
  }

  /** Genrates linear-gradients (svg) for station with two lines */
  genGradient2(colors: string[]) {
    const d3 = this.d3;
    let styleURL;
    if (d3.select(`#${colors[0]}-${colors[1]}`).empty()) {
      const linGrad = d3.select("#gradients").append("linearGradient").attr("id", `${colors[0]}-${colors[1]}`)
        .attr("x1", "0").attr("y1", "1").attr("x2", "0").attr("y2", "0");
      linGrad.append("stop").attr("offset", "0%").attr("stop-color", `#${colors[0]}`);
      linGrad.append("stop").attr("offset", "50%").attr("stop-color", `#${colors[0]}`);
      linGrad.append("stop").attr("offset", "50%").attr("stop-color", `#${colors[1]}`);
      linGrad.append("stop").attr("offset", "100%").attr("stop-color", `#${colors[1]}`);
    }
    return styleURL = `url(#${colors[0]}-${colors[1]})`;

  }

  /** Genrates linear-gradients (svg) for station with three or more lines */
  genGradient3(colors: string[]) {
    const d3 = this.d3;
    let styleURL;
    if (d3.select(`#${colors[0]}-${colors[1]}-${colors[2]}`).empty()) {
      const linGrad = d3.select("#gradients").append("linearGradient")
        .attr("id", `${colors[0]}-${colors[1]}-${colors[2]}`)
        .attr("x1", "0").attr("y1", "1").attr("x2", "0").attr("y2", "0");
      linGrad.append("stop").attr("offset", "0%").attr("stop-color", `#${colors[0]}`);
      linGrad.append("stop").attr("offset", "33%").attr("stop-color", `#${colors[0]}`);
      linGrad.append("stop").attr("offset", "33%").attr("stop-color", `#${colors[1]}`);
      linGrad.append("stop").attr("offset", "66%").attr("stop-color", `#${colors[1]}`);
      linGrad.append("stop").attr("offset", "66%").attr("stop-color", `#${colors[2]}`);
      linGrad.append("stop").attr("offset", "100%").attr("stop-color", `#${colors[2]}`);
    }
    return styleURL = `url(#${colors[0]}-${colors[1]}-${colors[2]})`;

  }

  /** event handler for mouseMove on network map; changes the tooltip position */
  mouseMove(d) {
    const d3 = this.d3;
    // debugger
    const top = (d3.event.offsetY + 10);
    const left = (d3.event.offsetX + 30);
    this.tooltip
      .style("top", `${top}px`)
      .style("left", `${left}px`);
  }

  /** event handler for mouseover on circle
   @param d data from station
   */
  mouseOverStation(d) {
    this.tooltip
      .style("visibility", "visible")
      .html(`<b>Station: </b>${this.util.replaceUndergroundStationTextWithIcon(d.name)} <br>
        <b>ID: </b>${d.id}<br>
        <b>Available lines: </b>${d.lines.map(lineID => this.lineIDtoName[lineID]).join(", ")} <br>
        ${this.util.formatProblems(this.lineIDtoLineStates[this.stationToLine[d.id][0]].stopIdToStopStates[d.id].problems)}
        `); // use first element of lines found to select stop state, because state should be the same on every line
  }

  /**
   * event handler for click on circle
   * @param d data from station
   */
  clickOnStation(d) {
    // @TODO: Handling of multiple lines on same station
    const name = d.name.split("Underground Station")[0].trim();
    this.router.navigate(["/line", d.lines[0]], { queryParams: { stopID: d.id } });
  }

  /** event handler for mouseover on line
   @param d data from line
   */
  mouseOverLine(d) {
    this.tooltip
      .style("visibility", "visible")
      .html(`<b> Line: </b>${d.lines.map(lineID => this.lineIDtoName[lineID]).join(", ")} <br>
      ${d.lines.map(lineID => this.util.formatProblems(this.lineIDtoLineStates[lineID].problems)).join(", ")}`
      );
  }

  /** event handler for mouseout */
  mouseOut() {
    const d3 = this.d3;
    const target = d3.event.currentTarget;
    //  const oldR = +d3.select(target).data()[0]["oldR"];
    //  d3.select(target).transition().duration(500).attr("r", oldR);
    //  d3.select(target).transition().duration(500).attr("fill", "#fff");
    this.tooltip
      .style("visibility", "hidden");
  }

  goToLine($event, row) {
    this.router.navigate(["/line", row.lineID]);
  }

  /**
   * init ol (OpenLayers) for usage with D3
   * @param map - OpenLayers-map
   * @return {function} scaleLonLat - computes the scaled lon/lat positions on the map
   */
  init_ol_d3(map): (lonlat: any) => any {
    const d3 = this.d3;
    const that = this;
    d3.selectAll("#" + map.getTarget() + " .d3-layer").remove();
    const g = d3.select("#" + map.getTarget() + " .ol-viewport")
      .insert("div", "canvas").classed("d3-layer", true) // div before gui for gui to work
      .style("position", "absolute")
      .style("bottom", "0px")
      .style("left", "0px")
      .style("width", "100%")
      .style("height", "100%")
      .append("svg")
      .style("background-color", "rgba(255,255,255,0.3)") // fade map a little to make stations pop
      .style("width", "100%")
      .style("height", "100%")
      .append("g");
    const init00 = map.getPixelFromCoordinate(ol.proj.fromLonLat([0, 0]));
    const init11 = map.getPixelFromCoordinate(ol.proj.fromLonLat([1, 1]));
    const initScale = [init11[0] - init00[0], init11[1] - init00[1]];
    let curr00 = init00;
    let rescale = [1, 1];

    map.on("postrender", _ => { // retransform
      curr00 = map.getPixelFromCoordinate(ol.proj.fromLonLat([0, 0]));
      const curr11 = map.getPixelFromCoordinate(ol.proj.fromLonLat([1, 1]));
      if (curr11.length > 0) {
        const currScale = [curr11[0] - curr00[0], curr11[1] - curr00[1]];
        rescale = [currScale[0] / initScale[0], currScale[1] / initScale[1]];
        const retranslate = [curr00[0] - init00[0] * rescale[0], curr00[1] - init00[1] * rescale[1]];
        g.attr("transform", // p_curr = (p_init-O_init)*rescale + O_curr
          "translate(" + retranslate[0] + "," + retranslate[1] + ")" +
          " scale(" + rescale[0] + "," + rescale[1] + ")");

        d3.selectAll(".map .d3-layer circle")
          .attr("r", d => (that.radius(d["type"]) / +rescale[0]))
          .attr("stroke-width", d => (that.strokeWidth(d["type"]) / +rescale[0]));
      }
    });

    const scaleLonLat = lonlat => {
      const p = map.getPixelFromCoordinate(ol.proj.fromLonLat(lonlat));
      p[0] = (p[0] - curr00[0]) / rescale[0] + init00[0];
      p[1] = (p[1] - curr00[1]) / rescale[1] + init00[1];
      return p;
    };
    return scaleLonLat;
  }

  /**
   * Filter stations and lines in the network map
   * @param event - clicked event
   * @param row - data from the clicked table-row
   */
  filterNetwork(event, row): void {
    const d3 = this.d3;
    const that = this;
    if (event && row) {
      // Get parentElement if user clicked on icon
      const target = event.target.type !== "button" ? event.target.parentElement : event.target;
      const index = this.filteredLines.indexOf(row.lineID);
      target.classList.toggle("filterOn");
      if (index === -1) {
        this.filteredLines.push(row.lineID);
      } else {
        this.filteredLines.splice(index, 1);
      }
    }
    // Show all stations / lines
    d3.selectAll("circle, line").style("visibility", "visible");

    // Filter all stations / lines or circles / lines respectively which are defined in the filteredLines array
    if (this.filteredLines.length > 0) {
      d3.selectAll("circle, line").filter((d: any) => !that.filteredLines.reduce(
        (acc, v) => acc || d.lines.includes(v), false)).style("visibility", "hidden");
      this.filterProblems();
    }
  }

  filterProblems(): void {
    const d3 = this.d3;
    const that = this;
    // Show all problem log cards
    Array.from(document.querySelectorAll(
      `.logCard`
    )).forEach((val: any) => {
      val.style.display = "initial";
    });
    // Filter all stations / lines or circles / lines respectively which are defined in the filteredLines array
    if (this.filteredLines.length > 0) {
      // Filter all problem log cards which are defined in the filteredLines array
      Array.from(document.querySelectorAll(
        `.logCard`
      )).filter(val => !that.filteredLines.reduce(
        (acc, v) => acc || val.getAttribute("data-lineid").includes(v), false)).forEach((val: any) => {
          val.style.display = "none";
        });
    }
  }

  /** Change network map layer */
  changeMapLayer() {
    this.map.setLayerGroup(new ol.layer.Group());
    if (this.selectedLayer === "default") {
      this.map.addLayer(this.layers["default"]);
    } else {
      this.map.addLayer(this.layers.custom(this.selectedLayer));
    }
    // Add labels to watercolor layer
    if (this.selectedLayer === "watercolor") {
      this.map.addLayer(this.layers.custom("terrain-labels"));
    }
  }
  /** Listener for datatable
   * onclick navigate to Line-View
   * @param event
   */
  onActivate(event) {
    if (event.type === "click" && event.column.prop !== "actions") {
      event.cellElement.blur();
      this.router.navigate(["/line", event.row.lineID]);
    }
  }
}
