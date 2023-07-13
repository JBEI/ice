import {Component, Input} from '@angular/core';
import {CommonModule} from "@angular/common";
import {Part} from "../../../../models/Part";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpService} from "../../../../services/http.service";
import {FormsModule} from "@angular/forms";
import {SampleService} from "../../../../services/sample.service";

@Component({
    selector: 'app-add-to-cart',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './add-to-cart.component.html',
    styleUrls: ['./add-to-cart.component.css']
})
export class AddToCartComponent {

    @Input() part: Part;
    sampleType: string = 'LIQUID_CULTURE';
    growthTemp = 30;
    growthTempOptions = [30, 37];
    plateDescription: string;

    constructor(public activeModal: NgbActiveModal, private http: HttpService, private samples: SampleService) {
    }

    selectedSampleType(value: string): void {
        this.sampleType = value;
    }

    addSampleToCart(): void {
        // var sampleSelection = {
        //     requestType: $scope.userData.sampleType,
        //     growthTemperature: $scope.sampleTemp.value,
        //     partData: {
        //         id: entryId
        //     },
        //     plateDescription: $scope.userData.plateDescription.value === "I will deliver my own media" ?
        //         $scope.userData.plateDescriptionText : $scope.userData.plateDescription.value
        // };
        //
        // // add selection to shopping cart
        // Util.post("rest/samples/requests", sampleSelection, function () {
        //     $rootScope.$emit("SamplesInCart");
        //     $uibModalInstance.close('');
        // });
    }

    getPlateDescriptionOptions(): string[] {
        if (this.sampleType === 'LIQUID_CULTURE')
            return this.samples.liquidCultureOptions;
        return this.samples.streakOnAgarPlateOptions;
    }

}
