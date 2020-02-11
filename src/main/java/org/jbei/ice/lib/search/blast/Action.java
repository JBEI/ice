package org.jbei.ice.lib.search.blast;

/**
 * Blast fasta database actions
 *
 * @author Hector Plahar
 */
public enum Action {
    FORCE_BUILD,        // rebuild the database even if it already exists
    CHECK,              // check that the database existing and if not build
    CREATE,             // create a new sequence
    UPDATE,             // update an existing sequence
    DELETE              // delete an existing sequence
}
