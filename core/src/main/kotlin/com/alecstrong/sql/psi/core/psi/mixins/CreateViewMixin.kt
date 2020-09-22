package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.psi.asColumns
import com.intellij.lang.ASTNode

internal abstract class CreateViewMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlCreateViewStmt,
    TableElement {
  override fun name() = viewName

  override fun modifySchema(schema: Schema) {
    schema.forType<TableElement>().putValue(name().text, this)
    schema.forType<SqlCreateViewStmt>().putValue(name().text, this)
  }

  override fun tableExposed() = LazyQuery(viewName) {
    val columns =
        if (columnAliasList.isEmpty())
          compoundSelectStmt?.queryExposed()?.flatMap { it.columns }
        else
          columnAliasList.asColumns()

    QueryResult(viewName, columns ?: emptyList())
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)
    if (columnAliasList.isNotEmpty()) {
      if (columnAliasList.size != compoundSelectStmt?.queryExposed()?.map { it.columns }?.size)
        annotationHolder.createErrorAnnotation(this,
            "number of aliases is different from the number of columns")
    }
  }
}
