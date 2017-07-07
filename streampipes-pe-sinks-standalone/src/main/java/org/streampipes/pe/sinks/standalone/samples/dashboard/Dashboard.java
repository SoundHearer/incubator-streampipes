package org.streampipes.pe.sinks.standalone.samples.dashboard;

import org.streampipes.commons.config.ClientConfiguration;
import org.streampipes.messaging.EventListener;
import org.streampipes.messaging.jms.ActiveMQPublisher;

import javax.jms.JMSException;

public class Dashboard  implements EventListener<byte[]> {
    ActiveMQPublisher publisher;

    public Dashboard(String topic) {
        try {
            this.publisher = new ActiveMQPublisher(ClientConfiguration.INSTANCE.getJmsUrl(), topic);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(byte[] payload) {
        publisher.publish(payload);

    }
}