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

package org.apache.streampipes.connect.adapter.model.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.streampipes.connect.adapter.Adapter;
import org.apache.streampipes.connect.adapter.AdapterRegistry;
import org.apache.streampipes.connect.adapter.exception.AdapterException;
import org.apache.streampipes.connect.adapter.exception.ParseException;
import org.apache.streampipes.model.connect.adapter.*;
import org.apache.streampipes.model.connect.grounding.ProtocolDescription;
import org.apache.streampipes.model.connect.guess.GuessSchema;
import org.apache.streampipes.model.schema.EventSchema;

public abstract class GenericAdapter<T extends AdapterDescription> extends Adapter<T> {

    private static final Logger logger = LoggerFactory.getLogger(Adapter.class);
    protected Protocol protocol;

    public GenericAdapter(T adapterDescription) {
        super(adapterDescription);
    }

    public GenericAdapter(T adapterDescription, boolean debug) {
        super(adapterDescription, debug);
    }

    public GenericAdapter() {
        super();
    }

    public abstract GenericAdapterDescription getAdapterDescription();

    public abstract void setProtocol(Protocol protocol);

    @Override
    public void startAdapter() throws AdapterException {

        GenericAdapterDescription adapterDescription = getAdapterDescription();


        Parser parser = getParser(adapterDescription);
        Format format = getFormat(adapterDescription);

        ProtocolDescription protocolDescription = ((GenericAdapterDescription) adapterDescription).getProtocolDescription();

        Protocol protocolInstance = this.protocol.getInstance(protocolDescription, parser, format);
        this.protocol = protocolInstance;

        //TODO remove
        EventSchema eventSchema = adapterDescription.getEventSchema();
        this.protocol.setEventSchema(eventSchema);

        logger.debug("Start adatper with format: " + format.getId() + " and " + protocol.getId());

        protocolInstance.run(adapterPipeline);
    }


    @Override
    public GuessSchema getSchema(T adapterDescription) throws AdapterException, ParseException {
        Parser parser = getParser((GenericAdapterDescription) adapterDescription);
        Format format = getFormat((GenericAdapterDescription) adapterDescription);

        ProtocolDescription protocolDescription = ((GenericAdapterDescription) adapterDescription).getProtocolDescription();

        Protocol protocolInstance = this.protocol.getInstance(protocolDescription, parser, format);

        logger.debug("Extract schema with format: " + format.getId() + " and " + protocol.getId());

        return protocolInstance.getGuessSchema();
    }

    private Parser getParser(GenericAdapterDescription adapterDescription) throws AdapterException {
         if (adapterDescription.getFormatDescription() == null) throw new AdapterException("Format description of Adapter ist empty");
         return AdapterRegistry.getAllParsers().get(adapterDescription.getFormatDescription().getAppId()).getInstance(adapterDescription.getFormatDescription());
    }

    private Format getFormat(GenericAdapterDescription adapterDescription) {
        return AdapterRegistry.getAllFormats().get(adapterDescription.getFormatDescription().getAppId()).getInstance(adapterDescription.getFormatDescription());
    }

}
