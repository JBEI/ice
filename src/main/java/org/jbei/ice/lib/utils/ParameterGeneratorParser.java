package org.jbei.ice.lib.utils;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.models.Parameter;
import org.jbei.ice.lib.models.Parameter.ParameterType;

/**
 * Converts parameters to and from text
 * 
 * @author tsham
 * 
 */
public class ParameterGeneratorParser {

    public static List<Parameter> parseParameterString(String input) {
        List<Parameter> result = new ArrayList<Parameter>();
        String[] commaSplits = input.trim().split(",");
        String[] colonSplits = null;
        Parameter parameter = null;
        String newKey = null;
        String newValue = null;
        for (String commaSplit : commaSplits) {
            colonSplits = commaSplit.split(":");
            if (colonSplits.length == 2) {
                newKey = colonSplits[0].trim();
                newValue = colonSplits[1].trim();
                if (newKey.startsWith("\"") && newKey.endsWith("\"")) {
                    newKey = newKey.substring(1, newKey.length() - 1);
                    newKey = newKey.replaceAll("\\s", "_");
                    if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
                        newValue = newValue.substring(1, newValue.length() - 1);
                        parameter = new Parameter();
                        parameter.setKey(newKey);
                        parameter.setValue(newValue);
                        parameter.setParameterType(Parameter.ParameterType.TEXT);
                        result.add(parameter);
                    } else {
                        parameter = new Parameter();
                        parameter.setKey(newKey);
                        if ("true".equals(newValue.toLowerCase())) {
                            parameter.setValue("true");
                            parameter.setParameterType(Parameter.ParameterType.BOOLEAN);
                            result.add(parameter);
                        } else if ("false".equals(newValue.toLowerCase())) {
                            parameter.setValue("false");
                            parameter.setParameterType(Parameter.ParameterType.BOOLEAN);
                            result.add(parameter);
                        } else {
                            try {
                                @SuppressWarnings("unused")
                                Double tempDouble = null;
                                tempDouble = Double.parseDouble(newValue);
                                parameter.setValue(newValue);
                                parameter.setParameterType(ParameterType.NUMBER);
                                result.add(parameter);
                            } catch (NumberFormatException e) {
                                parameter.setValue(newValue);
                                parameter.setParameterType(Parameter.ParameterType.TEXT);
                                result.add(parameter);
                            }
                        }

                    }
                }
                parameter = null;
            }
        }
        return result;
    }

    public static String generateParametersString(List<Parameter> parameters) {
        String result = null;
        ArrayList<String> parameterStringList = new ArrayList<String>();
        String tempValue = null;
        for (Parameter parameter : parameters) {
            if (Parameter.ParameterType.TEXT == parameter.getParameterType()) {
                tempValue = "\"" + parameter.getValue() + "\"";
            } else {
                tempValue = parameter.getValue();
            }
            parameterStringList.add("\"" + parameter.getKey() + "\":" + tempValue);
        }
        result = Utils.join(",", parameterStringList);
        return result;
    }
}
