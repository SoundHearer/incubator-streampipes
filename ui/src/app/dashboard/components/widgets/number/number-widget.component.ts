/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {Component, OnDestroy, OnInit} from "@angular/core";
import {RxStompService} from "@stomp/ng2-stompjs";
import {BaseStreamPipesWidget} from "../base/base-widget";
import {StaticPropertyExtractor} from "../../../sdk/extractor/static-property-extractor";
import {NumberConfig} from "./number-config";
import {ResizeService} from "../../../services/resize.service";
import {EventProperty} from "../../../../connect/schema-editor/model/EventProperty";
import {EventPropertyPrimitive} from "../../../../connect/schema-editor/model/EventPropertyPrimitive";
import {DashboardService} from "../../../services/dashboard.service";

@Component({
    selector: 'number-widget',
    templateUrl: './number-widget.component.html',
    styleUrls: ['./number-widget.component.css']
})
export class NumberWidgetComponent extends BaseStreamPipesWidget implements OnInit, OnDestroy {

    item: any = "-";

    selectedProperty: string;
    measurementUnitAbbrev: string;

    constructor(rxStompService: RxStompService, dashboardService: DashboardService, resizeService: ResizeService) {
        super(rxStompService, dashboardService, resizeService, false);
    }

    ngOnInit(): void {
        super.ngOnInit();
    }

    ngOnDestroy(): void {
        super.ngOnDestroy();
    }

    extractConfig(extractor: StaticPropertyExtractor) {
        this.selectedProperty = extractor.mappingPropertyValue(NumberConfig.NUMBER_MAPPING_KEY);
        let eventProperty: EventPropertyPrimitive = extractor.getEventPropertyByName(this.selectedProperty) as EventPropertyPrimitive;
        if (eventProperty.measurementUnit) {
            this.dashboardService.getMeasurementUnitInfo(eventProperty.measurementUnit).subscribe(unit => {
                this.measurementUnitAbbrev = unit.abbreviation;
            })
        }
    }

    isNumber(item: any): boolean {
        return false;
    }

    protected onEvent(event: any) {
        let value = event[this.selectedProperty];
        if (!isNaN(value)) {
            value = value.toFixed(2);
        }
        this.item = value;
    }

    protected onSizeChanged(width: number, height: number) {
    }

}