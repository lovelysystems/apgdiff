/**
 * Copyright 2018 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff;

import cz.startnet.utils.pgdiff.schema.PgSchema;
import java.io.PrintWriter;
import java.util.List;

/**
 * Diffs rules.
 *
 * @author jalissonmello
 */
public class PgDiffGrant {

    /**
     * Outputs statements for creation of new triggers.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    public static void createGrants(final PrintWriter writer, final PgSchema oldSchema, final PgSchema newSchema) {

        final List<String> oldGrants;
        if (oldSchema == null) {
            oldGrants = null;
        } else {
            oldGrants = oldSchema.getGrants();
        }
        for (final String newGrant: newSchema.getGrants()) {

            final List<String> oldGrant;
            if (oldGrants != null && oldGrants.contains(newGrant)) {
                continue;
            }
            writer.println();
            writer.println(newGrant);
        }
    }
}
