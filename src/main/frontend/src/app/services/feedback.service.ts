import { Injectable } from "@angular/core";
import { Http } from "@angular/http";
import { ToastrService } from "ngx-toastr";

@Injectable()
export class FeedbackService {

  constructor(private http: Http, private toastr: ToastrService) {

  }


  getAllFeedbacks() {
    return this.http.get("/rest/feedbacks").toPromise().then(result => {
      return result.json();
    });
  }

  setFeedbackFinished(id: number) {
    return this.http.post("/rest/feedback/setFinished/" + id, null).toPromise()
      .then(result => {
        if (result.ok) {
          this.toastr.success("Feedback finished!", "Feedback Completion", { timeOut: 3000 });
        } else {
          this.toastr.error("Something went wrong...", "Feedback Completion", { timeOut: 3000 });
        }
        return result;
      })
      .catch(error => {
        this.toastr.error("An error occured. (" + error.status + ", " + error.statusText + ")", "Feedback Completion", { timeOut: 3000 });
        return error;
      });
  }

}
