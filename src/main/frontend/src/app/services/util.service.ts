/**
 * Service for useful functions / constants etc. which are often used across the project
 */
import { Injectable } from "@angular/core";
import * as moment from "moment";


@Injectable()
export class UtilService {

  /** Constants */
  spinner = `<div class="sk-cube-grid"> <div class="sk-cube sk-cube1"></div> <div class="sk-cube sk-cube2"></div>
    <div class="sk-cube sk-cube3"></div> <div class="sk-cube sk-cube4"></div>
    <div class="sk-cube sk-cube5"></div> <div class="sk-cube sk-cube6"></div>
    <div class="sk-cube sk-cube7"></div> <div class="sk-cube sk-cube8"></div>
    <div class="sk-cube sk-cube9"></div> </div>`;

  /** format date to locale format */
  makeDateStr = ts => moment(ts).format("YYYY-MM-DD kk:mm");
  makeDateOnly = ts => moment(ts).format("MM-DD");
  makeTimeOnly = ts => moment(ts).format("HH:mm");
  // new Date(ts).toLocaleString("en-GB");
  /** Extracts the week number of a date */
  extractWeekNum = date => date ? moment(date).format("w") : "-";
  /** Extracts the year of a date */
  extractYear = date => moment(date).format("YYYY");
  /** Format Services time to our common makeDateStr */
  formatServicesDate = ts => moment(ts, "MMM Do YY, hh:mm").format("YYYY-MM-DD kk:mm");
  /** Format shiftPlan endTime and startTime */
  formatShiftPlanTime = date => moment(date, "MMM DD, YYYY, hh:mm:ss a").format("hh:mm a");
  /** Extract day of week (0-6) */
  extractDay = date => moment(date).format("e");
  /** Extract day of week */
  extractDay2 = date => moment(date).format("MMM Do YY");
  /** Extract time from date */
  extractTime = date => moment(date).format("hh:mm a");


  /** Returns color in RGB-HEX (#) for States Red, Yellow and Green */
  getRGBStateColor(state: string): string {
    let color = "#000";
    switch (state) {
      case "Red":
        color = "#DC3545";
        break;
      case "Yellow":
        color = "#FFC107";
        break;
      case "Green":
        color = "#28A745";
        break;
      default:
        console.log("Invalid State " + state + ". Use #000 as fallback.");
    }
    return color;
  }


  problemSingularPlural(count: number) {
    return (count === 1 ? "Problem" : "Problems");
  }

  feedbackSingularPlural(count: number) {
    return (count === 1 ? "Feedback" : "Feedbacks");
  }

  serviceRSingularPlural(count: number) {
    return (count === 1 ? "Service Request" : "Service Requests");
  }


  /**
   * Converts array of problems to HTML String
   * @param problems
   */
  formatProblems(problems) {
    let problemHTML = "";
    if (problems && problems.length > 0) {
      problemHTML += "<b>" + this.problemSingularPlural(problems.length) + ":</b>";
      if (problems.length > 5) {
        problems.slice(0, 5).forEach(problem => {
          problemHTML += `<br>${problem.description} (Severity: ${problem.severity})`;
        });
        problemHTML += "<br>...";
      } else {
        problems.forEach(problem => {
          problemHTML += `<br>${problem.description} (Severity: ${problem.severity})`;
        });
      }
    }
    return problemHTML;
  }

  /**
   * Formats the delayTime in seconds to Html
   * @param delayTime
   * @returns {string}
   */
  formatDelay(delayTime) {
    let delayString = "<b>Delay: </b>";
    if (delayTime > 0) {
      if (delayTime >= 120) {
        // Delay Time >= 2 min show as minutes
        delayString += Math.round(delayTime / 60) + " min";
      } else {
        // show seconds
        delayString += delayTime + " sec";
      }
    } else {
      // no delay
      delayString += "none";
    }
    return delayString;
  }

  /**
   * Gets a station name as input and Replaces 'Undergroud Statoin' with html tube icon
   * @param {string} text
   * @returns {string}
   */
  replaceUndergroundStationTextWithIcon(text: string) {
    return text.replace("Underground Station", `<i class="fa fa-subway" aria-hidden="true"></i>`);
  }
}
