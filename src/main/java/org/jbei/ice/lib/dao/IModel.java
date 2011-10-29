package org.jbei.ice.lib.dao;

import java.io.Serializable;

/**
 * Interface for classes that get saved into the database.
 * <p>
 * In gd-ice, we try to maintain models saved to the database strictly as value objects: objects
 * that do not have methods other than getters and setters. In this way models can be directly
 * exported to web services such as soap, and be used by different programming languages.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public interface IModel extends Serializable {
}
