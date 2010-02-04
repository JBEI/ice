function initializeCollections() {
	var selectionMarkersCollection = [ ${selectionMarkersCollection} ];
	$(".selectionMarkersInput").autocomplete(
			selectionMarkersCollection, {
				multiple : true,
				autoFill : true
			});
	
	var promotersCollection = [ ${promotersCollection} ];
	$(".promotersInput").autocomplete(
			promotersCollection, { 
				multiple: true, 
				autoFill: true 
			});
		
	var originOfReplicationsCollection = [ ${originOfReplicationsCollection} ];
	$(".originOfReplicationInput").autocomplete(
			originOfReplicationsCollection, { 
				multiple: true, 
				autoFill: true
			});
	
	var plasmidsCollection = [ ${plasmidsCollection} ];
	$(".plasmidsInput").autocomplete(
			plasmidsCollection, { 
				multiple: true, 
				autoFill: true
			});
}
