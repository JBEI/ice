import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class SampleService {

    plate96Rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
    plate96Cols = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];

    streakOnAgarPlateOptions = [
        "LB Apr50",
        "LB Spect100",
        "LB Carb100",
        "LB Kan50",
        "LB Chlor25",
        "LB Gent30",
        "LB Gent30 Kan50 Rif100",
        "LB",
        "CSM -LEU",
        "CSM -HIS",
        "CSM -HIS -LEU -URA",
        "YPD",
        "I will deliver my own media"
    ];

    liquidCultureOptions = ["LB Kan50",
        "LB Carb100",
        "LB Chlor25",
        "LB",
        "YPD",
        "I will deliver my own media"
    ];

    constructor() {
    }

    // getStreakOnAgarPlateOptions(): string[] {
    //     return this.streakOnAgarPlateOptions;
    // }
    //
    // getLiquidCultureOptions(): string[] {
    //     return this.liquidCultureOptions;
    // }
}
