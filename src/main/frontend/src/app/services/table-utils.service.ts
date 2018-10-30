import {Injectable} from "@angular/core";

@Injectable()
export class TableUtilsService {
    constructor () {}

    // @TODO: also parse dates and times for checkNumericCell validation

    /**
     * checks whether n is a number
     * @param {[type]} n [description]
     */
    isNumber (n) {
      return !isNaN(parseFloat(n)) && !isNaN(n - 0);
    }

    /**
     * checks whether a cell is numeric and styles it accordingly
     * @param {[type]} value)    } [description]
     */
    checkNumericCell = ({ value }) => ({
      "align-cell-numeric": this.isNumber(value)
    })

    /**
     * checks whether a column header is numeric and styles it accordingly
     * to use, create a function that sets the numeric Headers.
     * @type {[type]}
     */
    checkNumericHeader = numericHeaders => ({column}) => {
      return {
        "align-header-numeric": numericHeaders.indexOf(column.name) > -1
      };
    }
}
