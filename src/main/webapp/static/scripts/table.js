data_form_id = "data_form_id"
action_control_id = "action_control_id"
state_control_id = "select_all_id"
row_control_id_prefix = "row_"
field_control_id = "field_control_id"
order_type_control_id = "order_type_control_id"
order_name_control_id = "order_name_control_id"
offset_control_id = "offset_control_id" 
limit_control_id = "limit_control_id"
format_control_id = "format_control_id" 

function select_all(rows_count) {
	if(rows_count <= 0) return;
	
	var state_control = document.getElementById(state_control_id);
	if(state_control == null) return;
	
	for (i = 0; i < rows_count + 1; i++) {
		control_checkbox_name = row_control_id_prefix + i;
		control_checkbox = document.getElementById(control_checkbox_name);
		if(control_checkbox != null) {
			control_checkbox.checked = state_control.checked;
		}
	}
}

/* Send post request to reorder data table
 * 
 * @param order_name: field name
 * @type order_name: string
 * @param order_type: order type ('ASC' or 'DESC')
 * @type order_type: string
 * */
function order_by(order_name, order_type) {
	var data_form = document.getElementById(data_form_id);
	if(data_form == null) return
	
	var action_control = document.getElementById(action_control_id);
	
	if(action_control != null) { 
		action_control.value = '' 	// clean action
	}
	
	var order_type_control = document.getElementById(order_type_control_id);
	var order_name_control = document.getElementById(order_name_control_id);
	var offset_control = document.getElementById(offset_control_id);

	if(order_type_control != null) order_type_control.value = order_type
	if(order_name_control != null) order_name_control.value = order_name
	if(offset_control != null) offset_control.value = 0
	
	data_form.submit()
}

function change_page(index, per_page) {
	if(! document.getElementById) return;
	
	var data_form = document.getElementById(data_form_id);
	if(data_form == null) return;
	
	var action_control = document.getElementById(action_control_id);
	
	if(action_control != null) { 
		action_control.value = '' 	// clean action
	}
	
	var offset_control = document.getElementById(offset_control_id);
	var limit_control = document.getElementById(limit_control_id);
	
	if(offset_control != null) offset_control.value = (index - 1) * per_page
	if(limit_control != null) limit_control.value = per_page
	
	data_form.submit()
}

function change_page_size(per_page) {
	if(! document.getElementById) return;
	
	var data_form = document.getElementById(data_form_id);
	if(data_form == null) return;
	
	var action_control = document.getElementById(action_control_id);
	
	if(action_control != null) { 
		action_control.value = '' 	// clean action
	}
	
	var offset_control = document.getElementById(offset_control_id);
	var limit_control = document.getElementById(limit_control_id);
	
	if(offset_control != null) offset_control.value = 0;
	if(limit_control != null) limit_control.value = per_page;
	
	data_form.submit()
}

function change_filter() {
	var data_form = document.getElementById(data_form_id);
	if(data_form == null) return
	
	var action_control = document.getElementById(action_control_id);
	
	if(action_control != null) { 
		action_control.value = '' 	// clean action
	}
	
	var offset_control = document.getElementById(offset_control_id);
	var limit_control = document.getElementById(limit_control_id);
	
	if(offset_control != null) offset_control.value = 0
	
	data_form.submit()
}

function change_format(export_format) {
	var data_form = document.getElementById(data_form_id);
	if(data_form == null) return
	
	var format_control = document.getElementById(format_control_id);
	
	if(format_control != null) format_control.value = export_format;
	
	data_form.submit()
	
	if(format_control != null) format_control.value = "";
}
