package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

class PgSchema(val name: String) {

    override fun toString(): String {
        return "PgSchema($name)"
    }

    /**
     * List of functions defined in the schema.
     */
    val functions: MutableList<PgFunction> = ArrayList()

    /**
     * List of operators defined in the schema.
     */
    val operators = mutableListOf<PgOperator>()

    /**
     * List of sequences defined in the schema.
     */
    val sequences: DBObjectContainer<PgSequence> = DBObjectContainer()

    /**
     * List of rels defined in the schema.
     */
    val rels: MutableList<PgRelation<*, *>> = ArrayList()

    /**
     * List of types defined in the schema.
     */
    val types: MutableList<PgType> = ArrayList()

    /**
     * List of types defined in the schema.
     */
    val domains: DBObjectContainer<PgDomain> = DBObjectContainer()

    /**
     * Array of grand statements
     */
    private val grantStatements: MutableList<String> = ArrayList()

    /**
     * List of indexes defined in the schema.
     */
    val indexes: MutableList<PgIndex> = ArrayList()

    /**
     * List of primary keys defined in the schema.
     */
    private val primaryKeys: MutableList<PgConstraint> = ArrayList()


    /**
     * Schema authorization.
     */
    var authorization: String? = null

    /**
     * Optional definition of schema elements.
     */
    var definition: String? = null

    var comment: String? = null

    var owner: String? = null

    val ownerSQL: String
        get() = "ALTER SCHEMA ${PgDiffUtils.getQuotedName(name)} OWNER TO $owner;"

    /**
     * Creates and returns SQL for creation of the schema.
     *
     * @return created SQL
     */
    val creationSQL: String
        get() {
            val sbSQL = StringBuilder(50)
            sbSQL.append("CREATE SCHEMA ")
            sbSQL.append(PgDiffUtils.createIfNotExists)
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            authorization?.let {
                sbSQL.append(" AUTHORIZATION ")
                sbSQL.append(PgDiffUtils.getQuotedName(it))
            }
            sbSQL.append(';')
            if (!comment.isNullOrEmpty()) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("COMMENT ON SCHEMA ")
                sbSQL.append(PgDiffUtils.getQuotedName(name))
                sbSQL.append(" IS ")
                sbSQL.append(comment)
                sbSQL.append(';')
            }
            if (owner != null) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(ownerSQL)
            }
            return sbSQL.toString()
        }

    fun getFunction(signature: String?): PgFunction? = functions.firstOrNull { it.signature == signature }

    fun getOperator(signature: String?): PgOperator? = operators.firstOrNull { it.signature == signature }

    /**
     * Getter for grant statements. The list cannot be modified.
     *
     * @return [.String]
     */
    val grants: List<String>
        get() = grantStatements

    /**
     * Finds index according to specified index `name`.
     *
     * @param name name of the index to be searched
     *
     * @return found index or null if no such index has been found
     */
    fun getIndex(name: String?): PgIndex? {
        for (index in indexes) {
            if (index.name == name) {
                return index
            }
        }
        return null
    }

    /**
     * Finds primary key according to specified primary key `name`.
     *
     * @param name name of the primary key to be searched
     *
     * @return found primary key or null if no such primary key has been found
     */
    fun getPrimaryKey(name: String?): PgConstraint? {
        for (constraint in primaryKeys) {
            if (constraint.name == name) {
                return constraint
            }
        }
        return null
    }

    /**
     * Finds sequence according to specified sequence `name`.
     *
     * @param name name of the sequence to be searched
     *
     * @return found sequence or null if no such sequence has been found
     */
    fun getSequence(name: String?): PgSequence? {
        for (sequence in sequences) {
            if (sequence.name == name) {
                return sequence
            }
        }
        return null
    }

    /**
     * Finds table/view according to specified `name`.
     *
     * @param name name of the table/view to be searched
     *
     * @return found table or null if no such table has been found
     */
    fun getRelation(name: String?): PgRelation<*, *>? {
        for (rel in rels) {
            if (rel.name == name) {
                return rel
            }
        }
        return null
    }

    /**
     * Finds table according to specified table `name`.
     *
     * @param name name of the table to be searched
     *
     * @return found table or null if no such table has been found
     */
    fun getTable(name: String): PgTableBase? {
        return tables.firstOrNull { it.name == name }
    }

    /**
     * Get a list of tables from [.rels].
     *
     * @return list of tables
     */
    val tables: List<PgTableBase>
        get() = rels.filterIsInstance<PgTableBase>()

    /**
     * Finds view according to specified view `name`.
     *
     * @param name name of the view to be searched
     *
     * @return found view or null if no such view has been found
     */
    fun getView(name: String): PgViewBase? {
        return views.firstOrNull { it.name == name }
    }

    /**
     * Get a list of views from [.rels].
     *
     * @return list of views
     */
    val views: List<PgViewBase>
        get() = rels.filterIsInstance<PgViewBase>()

    /**
     * Adds `index` to the list of indexes.
     *
     * @param index index
     */
    fun addIndex(index: PgIndex) {
        indexes.add(index)
    }

    /**
     * Adds `primary key` to the list of primary keys.
     *
     * @param primaryKey index
     */
    fun addPrimaryKey(primaryKey: PgConstraint) {
        primaryKeys.add(primaryKey)
    }

    /**
     * Adds `function` to the list of functions.
     *
     * @param function function
     */
    fun addFunction(function: PgFunction) {
        functions.add(function)
    }

    /**
     * Adds `statement` to the list of grant statements.
     *
     * @param grant drant
     */
    fun addGrant(grant: String) {
        grantStatements.add(grant)
    }

    /**
     * Adds `sequence` to the list of sequences.
     *
     * @param sequence sequence
     */
    fun addSequence(sequence: PgSequence) {
        sequences.add(sequence)
    }

    /**
     * Adds `rel` table or view to the list of rels.
     *
     * @param rel relation
     */
    fun addRelation(rel: PgRelation<*, *>) {
        rels.add(rel)
    }

    /**
     * Adds `type` to the list of types.
     *
     * @param type type
     */
    fun addType(type: PgType) {
        types.add(type)
    }

    /**
     * Finds type according to specified name `name`.
     *
     * @param name name of the type to be searched
     *
     * @return found type or null if no such table has been found
     */
    fun getType(name: String?): PgType? {
        for (type in types) {
            if (type.name == name) {
                return type
            }
        }
        return null
    }

    fun containsType(name: String?): Boolean {
        for (type in types) {
            if (type.name == name) {
                return true
            }
        }
        return false
    }

    /**
     * Returns true if schema contains function with given `signature`,
     * otherwise false.
     *
     * @param signature signature of the function
     *
     * @return true if schema contains function with given `signature`,
     * otherwise false
     */
    fun containsFunction(signature: String?): Boolean {
        for (function in functions) {
            if (function.signature == signature) {
                return true
            }
        }
        return false
    }

    /**
     * Returns true if schema contains sequence with given `name`,
     * otherwise false.
     *
     * @param name name of the sequence
     *
     * @return true if schema contains sequence with given `name`,
     * otherwise false
     */
    fun containsSequence(name: String?): Boolean {
        for (sequence in sequences) {
            if (sequence.name == name) {
                return true
            }
        }
        return false
    }

    /**
     * Returns true if schema contains table with given `name`, otherwise
     * false.
     *
     * @param name name of the table
     *
     * @return true if schema contains table with given `name`, otherwise
     * false.
     */
    fun containsTable(name: String): Boolean {
        return getTable(name) != null
    }

}