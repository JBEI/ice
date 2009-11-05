var textModes = [
	{text : "contains", value : "~"},
	{text : "doesn't contain", value : "!~"},
	{text : "begins with", value : "^"},
	{text : "ends with", value : "$"},
	{text : "is", value : "="},
	{text : "is not", value : "!"},
	{text : "is empty", value : "*"},
];

var selectModes = [
	{text : "is", value : ""},
	{text : "is not", value : "!"}
];

function createSelect(name, options, first, defaultValue)
{
	// Damn IE requires to set name on creation
	var selectElement = $("<select name='" + name + "'>");
	
	if(first) selectElement.append("<option value='" + first.value + "'>" + first.text + "</option>");
	if(options) {
		for(var i = 0; i < options.length; i++) {
			var option;
			if(typeof(options[i]) == "object") {
				selectElement.append("<option value='" + options[i].value + "'>" + options[i].text + "</option>");
			} else {
				selectElement.append("<option value='" + options[i] + "'>" + options[i] + "</option>");
			}
		}
	}
	if(defaultValue) selectElement.attr("value", defaultValue);
	
	return selectElement;
}

function createButton(name, value, cls) 
{
	var inputElement = $("<input type='button' name='" + name + "'>");
	
	if(value) inputElement.attr("value", value);
	if(cls) inputElement.attr("class", cls);
	
	return inputElement;
}

function createRadioButton(name, value, isChecked)
{
	if(isChecked) { // Has to be this way because IE doesn't support dynamic if element doesn't have name
		var inputElement = $("<input type='radio' name='" + name + "' checked='checked'>");
	} else {
		var inputElement = $("<input type='radio' name='" + name + "'>");
	}
	if(value) inputElement.attr("value", value);
	
	return inputElement;
}

function createTextBox(name, value, cls)
{
	var inputElement = $("<input type='text' name='" + name + "'>");
	
	if(value) inputElement.attr("value", value);
	if(cls) inputElement.attr("class", cls);
	
	return inputElement;
}

function createHidden(name, value)
{
	var inputElement = $("<input type='hidden' name='" + name + "'>");
	
	if(value) inputElement.attr("value", value);
	
	return inputElement;
}

var rows = []
var nextRowId = 1;

function createNewRow(defaulValue)
{
	if(!defaulValue) defaulValue = '';
	
	var currentRowID = "rowId" + nextRowId;
	rows.push(currentRowID)
	
	var filterRow = $("<tr id=\"" + currentRowID + "\"></tr>").appendTo("#filtersTable");
	filterRow.append("<td></td>").append("<td " + "id=\"" + currentRowID + "_td2\"" + " width='320' style='white-space: nowrap' nowrap='true'></td>").append("<td><nobr></nobr></td>");
	
	var filterSelect = $(createSelect("", filters, {"text" : "Select filter:", "value" : ""}, defaulValue)).appendTo("#" + currentRowID + " td:nth-child(1)").bind('change', function(event) { rowId = $(this).parents("tr")[0].id; setFilter(rowId, this.value); });
	var removeButton = $(createButton("", "-", "queryButton")).appendTo("#" + currentRowID + " td:nth-child(3) nobr").bind('click', function(event) { rowId = $(this).parents("tr")[0].id; removeRow(rowId); });
	var addButton = $(createButton("", "+", "queryButton")).appendTo("#" + currentRowID + " td:nth-child(3) nobr").bind('click', function(event) { createNewRow(); });
	
	nextRowId++;
	
	return currentRowID
}

function removeRow(rowId) 
{
	if(rows.length <= 1) { // don't remove if there is only one filter available
		return;
	}
	
	for(var i = 0; i < rows.length; i++) { // remove id from row filters array
		if(rows[i] == rowId) {
			rows.splice(i, 1);
			break;
		}
	}
	
	$("#" + rowId).remove(); // remove row from DOM
}

function getFilterByName(name) 
{
	for(var i = 0; i < filters.length; i++) { // remove id from row filters array
		if(filters[i].value == name) {
			return filters[i];
		}
	}
	return null;
}

function setFilter(rowId, filterName, values)
{
	if(!values) values = ['', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', ''] // dummy values
	$("#" + rowId + "_td2 *").remove(); // remove all previous filter DOM elements
	var filterCell = $("#" + rowId + " td:nth-child(2)");
	var filter = getFilterByName(filterName);
	if(filter.type == 'radio') {
		for(var i = 0; i < filter.options.length; i++) {
			$(createRadioButton(filter.value, filter.options[i].value, values[i])).appendTo(filterCell);
			$("<label>" + filter.options[i].text + "</label>").appendTo(filterCell);
		}
	} else if(filter.type == 'text') {
		$(createSelect("", textModes, "", values[0])).appendTo(filterCell);
		$(createTextBox("", values[1], "inputbox")).appendTo(filterCell);
		$(createHidden(filter.value, values[2])).appendTo(filterCell);
	} else if(filter.type == 'select') {
		$(createSelect("", selectModes, "", values[0])).appendTo(filterCell);
		$(createSelect("", filter.options, "", values[1])).appendTo(filterCell);
		$(createHidden(filter.value, values[2])).appendTo(filterCell);
	}
}

function deserializeForm(serializedForm) 
{
	eForm = eval(serializedForm);
	if(eForm && eForm.length > 0) {
		for(var i = 0; i < eForm.length; i++) {
			if(eForm[i]) {
				var rowId = createNewRow(eForm[i]['value']);
				setFilter(rowId, eForm[i]['value'], eForm[i]['options']);
			}
		}
	}
}

function onSubmit()
{
	var serializedForm = "["
	for(var i = 0; i < rows.length; i++) {
		var filterName = $("#" + rows[i] + " td:nth-child(1) select")[0].value;
		
		if(!filterName || filterName == '') continue; // filter is empty
		
		var filter = getFilterByName(filterName);
		// adjust complex filters
		if(filter.type == 'text') {
			serializedForm += "{";
			serializedForm += "'type' : 'text',";
			serializedForm += "'value' : '" + filterName + "',";
			
			var prefix = $("#" + rows[i] + " td:nth-child(2) select")[0].value;
			var inputElement = $("#" + rows[i] + " td:nth-child(2) input")[0];
			var inputHiddenElement = $("#" + rows[i] + " td:nth-child(2) input:hidden")[0];
			
			inputHiddenElement.value = prefix + inputElement.value;
			serializedForm += "'options' : ['" + prefix + "', '" + inputElement.value + "', '" + inputHiddenElement.value + "']";
			serializedForm += "}"
		} else if(filter.type == 'select') {
			serializedForm += "{";
			serializedForm += "'type' : 'select',";
			serializedForm += "'value' : '" + filterName + "',";
			
			var prefix = $("#" + rows[i] + " td:nth-child(2) select")[0].value;
			var selectElement = $("#" + rows[i] + " td:nth-child(2) select")[1];
			var inputHiddenElement = $("#" + rows[i] + " td:nth-child(2) input:hidden")[0];
			
			inputHiddenElement.value = prefix + selectElement.value;
			serializedForm += "'options' : ['" + prefix + "', '" + selectElement.value + "', '" + inputHiddenElement.value + "']";
			serializedForm += "}"
		} else if(filter.type == 'radio') {
			serializedForm += "{";
			serializedForm += "'type' : 'radio',";
			serializedForm += "'value' : '" + filterName + "',";
			
			var radioElements = $("#" + rows[i] + " td:nth-child(2) input:radio");
			serializedForm += "'options' : ["
			for(var j = 0; j < radioElements.length; j++) {
				serializedForm += radioElements[j].checked;
				if(j < radioElements.length - 1) { serializedForm += "," }
			}
			serializedForm += "]";
			serializedForm += "}"
		}
		
		if(i < rows.length - 1) { serializedForm += ","	}
	}
	serializedForm += "]"
	var serializedFormElement = $("#serializedForm")[0];
	serializedFormElement.value = serializedForm;
	
	$("#data_form_id").submit();
}

$(document).ready(function() {
	var serializedFormElement = $("#serializedForm")[0];
	if(serializedFormElement.value == "") {
		createNewRow();
	} else {
		deserializeForm(serializedFormElement.value);
	}
});
