/*
 * Copyright 2019 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {Injectable} from "@angular/core";
import {Observable} from "rxjs";

declare const Stomp: any;

@Injectable()
export class WebsocketService {

    constructor() {
    }

    connect(url, topic): Observable<any> {
        return new Observable<any>(observable => {
            let client = Stomp.client(url + "/topic/" +topic);

            var onConnect = (frame => {
                client.subscribe("/topic/" +topic, function (message) {
                    observable.next(JSON.parse(message.body));
                }, {'Sec-WebSocket-Protocol': 'v10.stomp, v11.stomp'});
            });
            client.connect("admin", "admin", onConnect);
        });
    }

}