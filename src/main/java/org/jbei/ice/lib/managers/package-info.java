/**
 * Managers to read and write to the database.
 * <p>
 * Create, read, update, delete operations for different {@link org.jbei.ice.lib.models model} 
 * objects. Other related database operations are also performed as needed.
 * <p>
 * The managers act as intermediary between the {@link org.jbei.ice.controllers controllers} 
 * and {@link org.jbei.ice.lib.models models}, to hide database persistence.
 * If Permission checking is required, use controllers instead.
 */
package org.jbei.ice.lib.managers;

