/**
 * Interfaces for {@link org.jbei.ice.lib.models models}.
 * <p>
 * Because models can be represented in multiple ways by different api mechanisms (soap, blazeds), 
 * it makes sense to create them as interfaces that hold getters and setters, and the actual
 * models be implementations.
 * <p>
 * However, we have been able to use the same model objects by using annotations, instead
 * of making separate implementations for each service. Perhaps this package should be deprecated,
 * but are left here for historical reasons.
 */
package org.jbei.ice.lib.models.interfaces;

