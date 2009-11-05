package org.jbei.registry;

import java.util.Set;

import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.models.*;

public class Tim {
	
	
	
	public boolean runTests() {
		boolean result = false;
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		Entry entry = EntryManager.get(3811);
		
		String recordId = entry.getRecordId();
		System.out.println(recordId);
		
		Set<PartNumber> partNumbers = entry.getPartNumbers();
		
		for (PartNumber i: partNumbers) {
			System.out.println(i.getPartNumber());
		
		Sequence seq = entry.getSequence();
		Set<SequenceFeature> seqFeats = seq.getSequenceFeatures();
		for (SequenceFeature seqFeat: seqFeats) {
			Feature feat = seqFeat.getFeature();
			Sequence featseq = seqFeat.getSequence();
			System.out.print(featseq.getId() + "->" + feat.getId());
			System.out.print(" " + seqFeat.getName() + " ");
			System.out.println(seqFeat.getStart() + "..." + seqFeat.getEnd());
		}
		
		
		
		Entry entry2 = EntryManager.getByPartNumber("JDK_p01592");
		System.out.println(entry2.getRecordId());
		
		}
	}
}