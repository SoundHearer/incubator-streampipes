package de.fzi.cep.sepa.declarer;

import de.fzi.cep.sepa.model.NamedSEPAElement;

public interface Declarer<D extends NamedSEPAElement> {

	public D declareModel();

}
