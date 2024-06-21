package cz.startnet.utils.pgdiff.schema


class UnsupportedType(o: Any) : Error("Unsupported object for visitor $o")


fun unsupported(o: Any): Nothing = throw UnsupportedType(o)


interface PGVisitor<R> {

    fun accept(o: Any): R {
        return when (o) {
            is PgTableBase -> visit(o)
            is PgViewBase -> visit(o)
            is PgType -> visit(o)
            is PgDomain -> visit(o)
            is PgOperator -> visit(o)
            is PgSequence -> visit(o)
            is PgConstraint -> visit(o)
            is PgFunction -> visit(o)
            is PgRule -> visit(o)
            is PgDatabase -> visit(o)
            is PgSchema -> visit(o)
            is PgRelation<*, *> -> visit(o)
            is DBObject -> visit(o)
            else -> error("unsupported type $o")
        }
    }

    fun visit(o: DBObject): R = unsupported(o)
    fun visit(o: PgFunction): R = unsupported(o)
    fun visit(o: PgRule): R = unsupported(o)
    fun visit(o: PgSchema): R = unsupported(o)
    fun visit(o: PgDatabase): R = unsupported(o)
    fun visit(o: PgTableBase): R = unsupported(o)
    fun visit(o: PgViewBase): R = unsupported(o)
    fun visit(o: PgConstraint): R = unsupported(o)
    fun visit(o: PgSequence): R = unsupported(o)
    fun visit(o: PgType): R = unsupported(o)
    fun visit(o: PgDomain): R = unsupported(o)
    fun visit(o: PgOperator): R = unsupported(o)
    fun visit(o: PgRelation<*, *>): R = unsupported(o)
}

abstract class WalkingVisitor : PGVisitor<Unit> {

    lateinit var table: PgTableBase
    lateinit var db: PgDatabase
    lateinit var schema: PgSchema

    override fun visit(o: PgDatabase) {
        db = o
        o.schemas.forEach(::visit)
    }

    override fun visit(o: PgSchema) {
        schema = o
        o.functions.forEach(::visit)
        o.tables.forEach(::visit)
        o.views.forEach(::visit)
        o.sequences.forEach(::visit)
        o.types.forEach(::visit)
    }

    override fun visit(o: PgTableBase) {
        table = o
        o.rules.forEach(::visit)
        o.constraints.forEach(::visit)
    }


}

