/**
* Custom pipe to loop through JS-Objects with ng-for
*/
import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "ObjNgFor",  pure: false })
export class ObjNgFor implements PipeTransform {
    transform(value: any, args: any[] = null): any {
        return Object.keys(value);
    }
}
