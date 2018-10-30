import { Component, OnInit, ViewChild } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { ConfigService } from "../../services/config.service";
import { Validators, FormControl } from "@angular/forms";
import { saveAs } from "file-saver";

@Component({
  selector: "app-config",
  templateUrl: "./config.component.html",
  styleUrls: []
})
export class ConfigComponent implements OnInit {
  generationstepControl = new FormControl("", [Validators.min(1)]);
  linepercentControl = new FormControl("", [Validators.max(100), Validators.min(0)]);
  tourpercentControl = new FormControl("", [Validators.max(100), Validators.min(0)]);
  vehiclestodeleteControl = new FormControl("", [Validators.max(100), Validators.min(0)]);
  stoppercentControl = new FormControl("", [Validators.max(100), Validators.min(0)]);

  constructor(private configService: ConfigService) { }

  public model = {
    LIVE_ENGINE_FREQUENCY_SECONDS: 0,
    LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN: 0,
    LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN: 0,
    LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN: 0,
    LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN: 0,
    LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN: 0,
    LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN: 0,
    LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN: 0,
    LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN: 0,
    LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN: 0,
    LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE: 0,
    LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE: 0,
    LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE: 0,
    LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE: 0,
    LIVE_ENGINE_RUN_LIVE: true,
  };

  async ngOnInit() {
    const configData = await this.configService.getConfig();

    this.model = configData;
  }

  save() {
    // POST to backend
    this.configService.postConfig(Object.assign(this.model, { "LIVE_ENGINE_SHOULD_RESTART": false }));
  }

  saveAndRestart() {
    // POST to backend
    this.configService.postConfig(Object.assign(this.model, { "LIVE_ENGINE_SHOULD_RESTART": true }));
  }

  resetToDefaults() {
    this.model = {
      LIVE_ENGINE_FREQUENCY_SECONDS: 180,
      LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_GENERATE_EACH_RUN: 5,
      LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_GENERATE_EACH_RUN: 3,
      LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_GENERATE_EACH_RUN: 1,
      LIVE_ENGINE_NUMBER_OF_FEEDBACKS_TO_GENERATE_EACH_RUN: 1,
      LIVE_ENGINE_NUMBER_OF_VEHICLE_PROBLEMS_TO_DELETE_EACH_RUN: 2,
      LIVE_ENGINE_NUMBER_OF_STOP_PROBLEMS_TO_DELETE_EACH_RUN: 1,
      LIVE_ENGINE_NUMBER_OF_LINE_PROBLEMS_TO_DELETE_EACH_RUN: 1,
      LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_INCREASE_DELAY_EACH_RUN: 25,
      LIVE_ENGINE_PROBABILITY_OF_TOURS_TO_DECREASE_DELAY_EACH_RUN: 30,
      LIVE_ENGINE_STARTING_DELAYED_TOURS_PERCENTAGE: 20,
      LIVE_ENGINE_STARTING_VEHICLE_PROBLEMS_PERCENTAGE: 20,
      LIVE_ENGINE_STARTING_STOP_PROBLEMS_PERCENTAGE: 10,
      LIVE_ENGINE_STARTING_LINE_PROBLEMS_PERCENTAGE: 40,
      LIVE_ENGINE_RUN_LIVE: true,
    };
    this.saveAndRestart();
  }

  async dumpCSV() {
    // GET download link from backend
    const data = await this.configService.dumpCSV();
    const blob = new Blob([data["_body"]], { type: "text/csv" });
    saveAs(blob, "Vehicles and Notifications.csv");
  }
}
