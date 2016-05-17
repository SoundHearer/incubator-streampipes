package de.fzi.cep.sepa.actions.samples.proasense;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.fzi.cep.sepa.commons.config.ClientConfiguration;
import de.fzi.cep.sepa.commons.messaging.IMessageListener;
import de.fzi.cep.sepa.commons.messaging.ProaSenseInternalProducer;
import de.fzi.cep.sepa.model.impl.graph.SecInvocation;
import eu.proasense.internal.ComplexValue;
import eu.proasense.internal.DerivedEvent;
import eu.proasense.internal.VariableType;

public class ProaSenseTopologyPublisher implements IMessageListener<byte[]> {

	private ProaSenseInternalProducer producer;
	private SecInvocation graph;
	private static final String DEFAULT_PROASENSE_TOPIC = "eu.proasense.internal.sp.internal.incoming";
	private TSerializer serializer;
	private ProaSenseEventNotifier notifier;
	private long lastTimestamp = 0;
	
	static String testJson =  "{\"ram_pos_measured\":21016.4,\"ram_vel_setpoint\":0.0,\"ram_pos_setpoint\":16510.2,\"ram_vel_measured\":-53.34777,\"pressure_gearbox\":3.423385,\"mru_vel\":-82.86029500000001,\"hook_load\":109.9758,\"mru_pos\":-179.9121,\"torque\":5.354475000000001,\"oil_temp_gearbox\":13.1959,\"wob\":-0.2115,\"ibop\":1.0,\"hoist_press_B\":112.839,\"oil_temp_swivel\":14.7423,\"rpm\":42.0743,\"hoist_press_A\":18.5644,\"eventName\":\"EnrichedEvent\",\"temp_ambient\":12.37,\"timestamp\":1387559130232}";
	   
	private static final Logger logger = LoggerFactory.getLogger(ProaSenseTopologyPublisher.class);

	private int i = 0;
	
	public ProaSenseTopologyPublisher(SecInvocation graph, ProaSenseEventNotifier notifier) {
		this.graph = graph;
		this.producer = new ProaSenseInternalProducer(ClientConfiguration.INSTANCE.getKafkaUrl(), DEFAULT_PROASENSE_TOPIC);
		this.serializer = new TSerializer(new TBinaryProtocol.Factory());
		this.notifier = notifier;
	}
	
	@Override
	public void onEvent(byte[] json) {
		//System.out.println("Sending event " +i +", " +json);
		i++;
		notifier.increaseCounter();
		if (i % 500 == 0) System.out.println("Sending, " +i);
			Optional<byte[]> bytesMessage = buildDerivedEvent(new String(json));
			if (bytesMessage.isPresent()) producer.send(bytesMessage.get());
			else System.out.println("empty event or skipping");
				
	}

	private Optional<byte[]> buildDerivedEvent(String json) throws IllegalArgumentException {
		//System.out.println(json);
		
		boolean isValidTime = true;
		DerivedEvent event = new DerivedEvent();
		
		event.setComponentId("CEP");
		event.setEventName("Generated");
		
		Map<String, ComplexValue> values = new HashMap<String, ComplexValue>();
		JsonElement element = new JsonParser().parse(json);
		
		try {
			if (element.isJsonObject())
			{
				JsonObject obj = element.getAsJsonObject();
				Set<Entry<String, JsonElement>> entries = obj.entrySet();
				
				for(Map.Entry<String,JsonElement> entry : entries){
					{
						if (entry.getKey().equals("timestamp")) {
							event.setTimestamp(obj.get("timestamp").getAsLong());
						}
						else if (entry.getKey().equals("time")) 
							{
								event.setTimestamp(obj.get("time").getAsLong());
								//System.out.println("Timestamp, " +event.getTimestamp());
								
							}
						else if (entry.getKey().equals("variable_timestamp"))
						{
							event.setTimestamp(obj.get("variable_timestamp").getAsLong());
						}
						else values.put(entry.getKey(), convert(obj.get(entry.getKey())));
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (event.getTimestamp() < lastTimestamp) 
		{
			isValidTime = false;
		}
		lastTimestamp = event.getTimestamp();
		event.setEventProperties(values);
		if (isValidTime) return serialize(event);
		else return Optional.empty();
	}

	private ComplexValue convert(JsonElement jsonElement) throws Exception {
		ComplexValue value = new ComplexValue();
		value.setType(getVariableType(jsonElement));
		value.setValue(jsonElement.getAsString());
		return value;
	}

	private VariableType getVariableType(JsonElement jsonElement) throws Exception {
		if (jsonElement.isJsonPrimitive())
		{
			JsonPrimitive primitive = (JsonPrimitive) jsonElement;
			if (primitive.isNumber()) return VariableType.DOUBLE;
			else if (primitive.isString()) return VariableType.STRING;
			else if (primitive.isBoolean()) return VariableType.BOOLEAN;
			else return VariableType.LONG;
		}
		else throw new Exception();
	}
	
	private Optional<byte[]> serialize(TBase tbase)
    {
    	try {
			return Optional.of(serializer.serialize(tbase));
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return Optional.empty();
    }
	
	public static void main(String[] args)
	{
		ProaSenseTopologyPublisher publisher = new ProaSenseTopologyPublisher(null, new ProaSenseEventNotifier(""));
		long currentTime = System.currentTimeMillis();
		for(int i = 0; i < 1000; i++) publisher.onEvent(testJson.getBytes());
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - currentTime);
	}

}