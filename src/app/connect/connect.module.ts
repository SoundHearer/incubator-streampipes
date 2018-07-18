import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatGridListModule, MatFormField, MatFormFieldModule } from '@angular/material';
import { FlexLayoutModule } from '@angular/flex-layout';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';


import { NewComponent } from './new-adapter/new.component';
import { MainComponent } from './main/main.component';
import { AllAdaptersComponent } from './all-adapters/all.component';

import { SelectProtocolComponent } from './select-protocol-component/select-protocol.component';
import { FormatFormComponent } from './format-form/format-form.component';

import { EventSchemaComponent } from './schema-editor/event-schema/event-schema.component';
import { EventPropertyPrimitiveComponent } from './schema-editor/event-property-primitive/event-property-primitive.component';
import { EventPropertyComponent } from './schema-editor/event-property/event-property.component';
import { EventPropertyNestedComponent } from './schema-editor/event-property-nested/event-property-nested.component';
import { EventPropoertyListComponent } from './schema-editor/event-propoerty-list/event-propoerty-list.component';

import { EventPropertyBagComponent } from './schema-editor/event-property-bag/event-property-bag.component';

import { StaticPropertyComponent } from './static-properties/static-property.component';

import { CustomMaterialModule } from '../CustomMaterial/custom-material.module';


import { RestService } from './rest.service';

import { DragulaModule } from 'ng2-dragula';
import { DataTypesService } from './schema-editor/data-type.service';
import { AdapterStartedDialog } from './new-adapter/component/adapter-started-dialog.component';
import {MatInputModule} from '@angular/material';

import {StaticNumberInputComponent} from './static-properties/static-number-input/static-number-input.component';
import {StaticUrlInputComponent} from './static-properties/static-url-input/static-url-input.component';
import {StaticTextInputComponent} from './static-properties/static-text-input/static-text-input.component';
import { StaticFreeInputComponent } from './static-properties/static-free-input/static-free-input.component';
import { StaticPropertyUtilService } from './static-properties/static-property-util.service'
import { SetStreamComponent } from './set-stream/set-stream.component';

import {SelectStaticPropertyComponent} from './select-static-property-component/select-static-property.component'

import {SelectStaticPropertiesComponent} from './select-static-properties-component/select-static-properties.component'

import {ProtocolComponent} from './protocol-component/protocol.component'

import {ProtocolListComponent} from './protocol-list-component/protocol-list.component'
import { FormatListComponent } from './format-list-component/format-list.component';
import { FormatComponent } from './format-component/format.component';

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        CommonModule,
        FlexLayoutModule,
        MatGridListModule,
        CustomMaterialModule,
        DragulaModule,
        MatProgressSpinnerModule,
        MatInputModule,
        MatFormFieldModule
    ],
    exports: [
        StaticPropertyComponent
    ],
    declarations: [
        NewComponent,
        MainComponent,
        AllAdaptersComponent,
        SelectProtocolComponent,
        FormatFormComponent,
        EventSchemaComponent,
        EventPropertyBagComponent,
        EventPropertyPrimitiveComponent,
        EventPropertyComponent,
        EventPropertyNestedComponent,
        EventPropoertyListComponent,
        StaticPropertyComponent,
        AdapterStartedDialog,
        StaticNumberInputComponent,
        StaticUrlInputComponent,
        StaticTextInputComponent,
        StaticFreeInputComponent,
        SetStreamComponent,
        SelectStaticPropertyComponent,
        SelectStaticPropertiesComponent,
        ProtocolComponent,
        ProtocolListComponent,
        FormatListComponent,
        FormatComponent


    ],
    providers: [
        RestService,
        DataTypesService,
        StaticPropertyUtilService
    ],
    entryComponents: [
        MainComponent,
        AdapterStartedDialog,
    ]
})
export class ConnectModule {
}