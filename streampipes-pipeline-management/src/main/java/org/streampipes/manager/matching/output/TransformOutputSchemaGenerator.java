/*
 * Copyright 2018 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.streampipes.manager.matching.output;

import org.streampipes.model.SpDataStream;
import org.streampipes.model.graph.DataProcessorInvocation;
import org.streampipes.model.output.OutputStrategy;
import org.streampipes.model.output.TransformOperation;
import org.streampipes.model.output.TransformOperationType;
import org.streampipes.model.output.TransformOutputStrategy;
import org.streampipes.model.schema.EventProperty;
import org.streampipes.model.schema.EventPropertyPrimitive;
import org.streampipes.model.schema.EventSchema;
import org.streampipes.model.staticproperty.FreeTextStaticProperty;
import org.streampipes.model.staticproperty.MappingPropertyUnary;
import org.streampipes.model.staticproperty.Option;
import org.streampipes.model.staticproperty.SelectionStaticProperty;
import org.streampipes.model.staticproperty.StaticProperty;
import org.streampipes.model.util.Cloner;
import org.streampipes.sdk.helpers.Tuple2;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransformOutputSchemaGenerator extends OutputSchemaGenerator<TransformOutputStrategy> {

  private TransformOutputStrategy strategy;
  private DataProcessorInvocation dataProcessorInvocation;

  protected static final String prefix = "urn:streampipes.org:spi:";

  public static TransformOutputSchemaGenerator from(OutputStrategy strategy,
                                                    DataProcessorInvocation invocation) {
    return new TransformOutputSchemaGenerator((TransformOutputStrategy) strategy, invocation);
  }

  public TransformOutputSchemaGenerator(TransformOutputStrategy strategy, DataProcessorInvocation
   invocation) {
    super(strategy);
    this.dataProcessorInvocation = invocation;
  }

  @Override
  public Tuple2<EventSchema, TransformOutputStrategy> buildFromOneStream(SpDataStream stream) {
    // TODO exceptions
    Map<String, EventProperty> modifiedEventProperties = new HashMap<>();
    EventSchema outSchema = new EventSchema();
    EventSchema inSchema = stream.getEventSchema();
    strategy.getTransformOperations().forEach(to -> {
      Optional<MappingPropertyUnary> mappingPropertyOpt = findMappingProperty(to.getMappingPropertyInternalName(),
              dataProcessorInvocation.getStaticProperties());

      if (mappingPropertyOpt.isPresent()) {
        Optional<EventProperty> eventPropertyOpt = findEventProperty(mappingPropertyOpt.get().getSelectedProperty()
                , inSchema
                .getEventProperties());

        if (eventPropertyOpt.isPresent()) {
          EventProperty eventProperty = eventPropertyOpt.get();
          modifiedEventProperties.put(eventProperty.getElementId(), modifyEventProperty(eventProperty, to,
                  dataProcessorInvocation.getStaticProperties()));
        }
      }
    });

    List<EventProperty> newProperties = inSchema.getEventProperties().stream().map(ep -> {
      if (modifiedEventProperties.containsKey(ep.getElementId())) {
        EventProperty newProperty = modifiedEventProperties.get(ep.getElementId());
        newProperty.setElementId(prefix + UUID.randomUUID().toString());
        return newProperty;
      } else {
        return ep;
      }
    }).collect(Collectors.toList());

    outSchema.setEventProperties(newProperties);
    return makeTuple(outSchema);
  }

  private EventProperty modifyEventProperty(EventProperty eventProperty, TransformOperation to, List<StaticProperty>
          staticProperties) {

    if (to.getTargetValue() != null) {
      return modifyEventProperty(eventProperty, TransformOperationType.valueOf(to.getTransformationScope()), to
              .getTargetValue());
    } else {
      Optional<StaticProperty> sp = findStaticProperty(staticProperties, to.getSourceStaticProperty());
      if (sp.isPresent()) {
        return modifyEventProperty(eventProperty, sp.get(), TransformOperationType.valueOf(to.getTransformationScope
                ()));
      }
    }
    return new Cloner().property(eventProperty);
  }

  private EventProperty modifyEventProperty(EventProperty eventProperty, StaticProperty staticProperty,
                                            TransformOperationType
                                                    transformOperationType) {
    if (staticProperty instanceof SelectionStaticProperty) {
      return modifyEventProperty(eventProperty, transformOperationType, findSelected(((SelectionStaticProperty)
              staticProperty).getOptions()).getInternalName());
    } else if (staticProperty instanceof FreeTextStaticProperty) {
      return modifyEventProperty(eventProperty, transformOperationType, ((FreeTextStaticProperty) staticProperty)
              .getValue
                      ());
    }

    return eventProperty;
  }

  private Option findSelected(List<Option> options) {
    return options
            .stream()
            .filter(o -> o.isSelected())
            .findFirst()
            .get();
  }

  private Optional<StaticProperty> findStaticProperty(List<StaticProperty> staticProperties, String sourceStaticProperty) {

    return staticProperties
            .stream()
            .filter(sp -> sp.getInternalName().equals(sourceStaticProperty))
            .findFirst();
  }


  private EventProperty modifyEventProperty(EventProperty eventProperty, TransformOperationType
          transformOperationType, String value) {
// TODO check support for lists and nested properties
    if (transformOperationType == TransformOperationType.DATATYPE_TRANSFORMATION) {
      if (eventProperty instanceof EventPropertyPrimitive) {
        ((EventPropertyPrimitive) eventProperty).setRuntimeType(value);
      }
    } else if (transformOperationType == TransformOperationType.MEASUREMENT_UNIT_TRANSFORMATION) {
      if (eventProperty instanceof EventPropertyPrimitive) {
        ((EventPropertyPrimitive) eventProperty).setMeasurementUnit(URI.create(value));
      }

    } else if (transformOperationType == TransformOperationType.DOMAIN_PROPERTY_TRANSFORMATION) {
      eventProperty.setDomainProperties(Arrays.asList(URI.create(value)));

    } else if (transformOperationType == TransformOperationType.RUNTIME_NAME_TRANSFORMATION) {
      eventProperty.setRuntimeName(value);
    }

    return eventProperty;
  }

  private Optional<EventProperty> findEventProperty(String propertySelector, List<EventProperty>
          eventProperties) {

    return eventProperties
            .stream()
            .filter(ep -> ep.getRuntimeName().equals(removePrefix(propertySelector)))
            .map(this::cloneEp)
            .findFirst();
  }

  private String removePrefix(String propertySelector) {
    return propertySelector.split("::")[1];
  }

  private EventProperty cloneEp(EventProperty ep) {
    return new Cloner().property(ep);
  }

  private Optional<MappingPropertyUnary> findMappingProperty(String mappingPropertyInternalName, List<StaticProperty>
          staticProperties) {

    return staticProperties
            .stream()
            .filter(sp -> sp.getInternalName().equals(mappingPropertyInternalName))
            .map(sp -> (MappingPropertyUnary) sp)
            .findFirst();
  }

  @Override
  public Tuple2<EventSchema, TransformOutputStrategy> buildFromTwoStreams(SpDataStream stream1, SpDataStream
          stream2) {
    // TODO
    return buildFromOneStream(stream1);
  }

}
