package cz.startnet.utils.pgdiff.schema

/** base class for all relation types
 * from https://www.postgresql.org/docs/12/catalog-pg-class.html
 * r = ordinary table,
 * i = index,
 * S = sequence,
 * t = TOAST table,
 * v = view,
 * m = materialized view,
 * c = composite type,
 * f = foreign table,
 * p = partitioned table,
 * I = partitioned index
 */
sealed class PgRel(name: String, objectType: String) : DBObject(objectType, name) {
}