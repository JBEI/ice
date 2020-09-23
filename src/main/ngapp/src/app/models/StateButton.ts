import {DesignState} from "./DesignState";

export class StateButton {
    name: string;
    action: string;
    nextState: DesignState;

    constructor(name: string, action: string, nextState: DesignState) {
        this.name = name;
        this.action = action;
        this.nextState = nextState;
    }
}
