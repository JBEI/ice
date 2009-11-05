package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
/**
 * Easy way to construct a choice renderer. generates the List
 * and the ChoiceRenderer object for use with Wicket choice objects
 * such as DropDownChoice.
 * 
 * @author tham
 *
 * @param <T>
 * 
 */
public class ChoiceRendererHelper<T> {
	
	public class CustomChoiceRenderer implements IChoiceRenderer {
		protected LinkedHashMap<T, T> hashMap;
		
		public CustomChoiceRenderer(LinkedHashMap<T, T> hashMap) {
			this.hashMap = hashMap;
		}
		
		public Object getDisplayValue(Object object) {
			String result = (String) hashMap.get(object); 
			return result;
		
		}

		public String getIdValue(Object object, int index) {
			return object.toString();
			
		}
		
	}
	protected LinkedHashMap<T, T> hashMap;
	
	public ChoiceRendererHelper(LinkedHashMap<T, T> hashMap) {
		this.hashMap = hashMap;
	}
	
	/**
	 * 
	 * @return
	 * 		The List<T> object to be used with the ChoiceRenderer
	 */
	public ArrayList getChoiceList() {
		ArrayList result = new ArrayList();
		for (Object item: this.hashMap.keySet()) {
			result.add(item);
		}
		return result;
	}
	
	/**
	 * 
	 * @return
	 * 	The ChoiceRender<T>
	 */
	public IChoiceRenderer getChoiceRenderer() {
		return new CustomChoiceRenderer(this.hashMap);
	}
	
}
