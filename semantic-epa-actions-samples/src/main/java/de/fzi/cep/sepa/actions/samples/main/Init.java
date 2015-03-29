package de.fzi.cep.sepa.actions.samples.main;

import java.util.ArrayList;
import java.util.List;

import de.fzi.cep.sepa.actions.samples.charts.ChartConsumer;
import de.fzi.cep.sepa.actions.samples.jms.JMSConsumer;
import de.fzi.cep.sepa.actions.samples.maps.MapsController;
import de.fzi.cep.sepa.actions.samples.table.TableViewController;
import de.fzi.cep.sepa.commonss.Configuration;
import de.fzi.cep.sepa.desc.ModelSubmitter;
import de.fzi.cep.sepa.desc.SemanticEventConsumerDeclarer;

public class Init {

	public static void main(String[] args) throws Exception
	{
		List<SemanticEventConsumerDeclarer> consumers = new ArrayList<>();
		
		consumers.add(new JMSConsumer());
		consumers.add(new ChartConsumer());
		consumers.add(new MapsController());
		consumers.add(new TableViewController());

		ModelSubmitter.submitConsumer(consumers, Configuration.ACTION_BASE_URL, 8091);
	}
}
