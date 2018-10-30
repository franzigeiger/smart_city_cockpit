import {Request, XHRBackend, XHRConnection} from "@angular/http";
import {Injectable} from "@angular/core";
import {environment} from "../environments/environment";

@Injectable()
export class ApiXHRBackend extends XHRBackend {
  createConnection(request: Request): XHRConnection {
    if (request.url.startsWith("/")) {
      request.url = environment.backendUrl + request.url;     // prefix base url
    }
    return super.createConnection(request);
  }
}
