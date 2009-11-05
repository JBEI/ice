var pickerElement = '';

function pickerWindow(url, pickerId) {
	var newWindow = window.open(url, 'Query', 'status=yes,resizable=yes,top=100,left=400,width=800,height=650,scrollbars=yes');
	pickerElementId = pickerId;
	newWindow.focus();
}

function select(value) {
	var pickerElement = $('#' + pickerElementId)
	
	if (!pickerElement) { return; }
	
	if(pickerElement.attr('value') == '') {
		pickerElement.attr("value", value);
	} else {
		pickerElement.attr("value", pickerElement.attr('value') + ", " + value);
	}
}
