import {TestBed} from "@angular/core/testing";
import {AppComponent} from "./app.component";
// import {HelloWorldComponent} from "./HelloWorldComponent";
// import {CalculatorComponent} from "./Calculator/calculator.component";
// import {ChatComponent} from "./chat.component";
// import {ServiceRequestTestComponent} from "./ServiceRequests/service-request-test.component";
// import {TubeVisTestComponent} from "./LondonAPI/tube-vis-test.component";
import {FormsModule} from "@angular/forms";
import {HttpModule} from "@angular/http";
import {BrowserModule} from "@angular/platform-browser";
// import {CalculatorService} from "./Calculator/calculator.service";
// import {ChatService} from "./chat.service";
// import {ServiceRequestTestService} from "./ServiceRequests/service-request-test.service";


describe("AppComponent", () => {
  beforeEach(async() => {
    TestBed.configureTestingModule({
      declarations: [
        AppComponent,
        // HelloWorldComponent,
        // CalculatorComponent,
        // ChatComponent,
        // TubeVisTestComponent,
        // ServiceRequestTestComponent
      ],
      imports: [
        BrowserModule,
        HttpModule,
        FormsModule,
      ],
      providers: [/*CalculatorService, ChatService, ServiceRequestTestService*/],
    }).compileComponents();
  });
  it("should create the app-component", () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });
/*  it(`should have as title 'app'`, async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app.title).toEqual('app');
  }));
  it('should render title in a h1 tag', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.debugElement.nativeElement;
    expect(compiled.querySelector('h1').textContent).toContain('Welcome to app!');
  }));*/
});

