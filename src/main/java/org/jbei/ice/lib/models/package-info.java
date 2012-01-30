/**
 * Data models for ice objects.
 * <p>
 * Models here represent the core data representation used by gd-ice. They are stored 
 * and retrieved into the database.
 * <p>
 * In gd-ice, we try to maintain models saved to the database strictly as value objects: objects
 * that do not have methods other than getters and setters. In this way models can be directly
 * exported to web services such as soap, and be used by different programming languages. 
 * <p>
 * However, if a value objects that do not need to be saved into the database are needed,
 * they should go into the {@link org.jbei.ice.lib.vo} package.
 * <p>
 * All models are derived from {@link org.jbei.ice.lib.dao.IModel}.
 */
package org.jbei.ice.lib.models;

