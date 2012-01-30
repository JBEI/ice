/**
 * Permission verifiers for different controllers. 
 * <p>
 * Different controllers different permission verifiers. For example, Entry can be made 
 * viewable or editable on a per user or group basis. However, Samples and Attachments 
 * are always globally viewable, but only editable or deletable by the owner.
 */
package org.jbei.ice.controllers.permissionVerifiers;

