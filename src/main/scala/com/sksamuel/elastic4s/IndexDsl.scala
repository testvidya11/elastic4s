package com.sksamuel.elastic4s

import org.elasticsearch.index.VersionType
import org.elasticsearch.action.index.IndexRequest.OpType
import org.elasticsearch.common.xcontent.{XContentFactory, XContentBuilder}
import org.elasticsearch.action.index.{IndexAction, IndexRequest}
import scala.collection.mutable.ListBuffer
import com.sksamuel.elastic4s.source.{DocumentSource, Source}

/** @author Stephen Samuel */
trait IndexDsl {

  def insert: IndexExpectsInto = index
  def index: IndexExpectsInto = new IndexExpectsInto

  class IndexExpectsInto {
    def into(index: String): IndexDefinition = into(index.split("/").head, index.split("/").last)
    def into(index: String, `type`: String): IndexDefinition = new IndexDefinition(index, `type`)
    def into(kv: (String, String)): IndexDefinition = into(kv._1, kv._2)
  }

  class IndexDefinition(index: String, `type`: String)
      extends RequestDefinition(IndexAction.INSTANCE) with BulkCompatibleDefinition {

    private val _request = new IndexRequest(index, `type`)
    private val _fields = new ListBuffer[(String, Any)]
    private var _source: Option[DocumentSource] = None
    def build = _source match {
      case None => _request.source(_fieldsAsXContent)
      case Some(src) => _request.source(src.json)
    }

    def _fieldsAsXContent: XContentBuilder = {
      val source = XContentFactory.jsonBuilder().startObject()
      for ( tuple <- _fields ) {
        source.field(tuple._1, tuple._2)
      }
      source.endObject()
    }

    def id(id: Any): IndexDefinition = {
      _request.id(id.toString)
      this
    }
    def routing(routing: String): IndexDefinition = {
      _request.routing(routing)
      this
    }

    def parent(parent: String): IndexDefinition = {
      _request.parent(parent)
      this
    }

    def timestamp(timestamp: String): IndexDefinition = {
      _request.timestamp(timestamp)
      this
    }

    def ttl(ttl: Long): IndexDefinition = {
      _request.ttl(ttl)
      this
    }

    def update(update: Boolean): IndexDefinition = opType(OpType.CREATE)
    def opType(opType: IndexRequest.OpType): IndexDefinition = {
      _request.opType(opType)
      this
    }

    def version(version: Int): IndexDefinition = {
      _request.version(version)
      this
    }

    def versionType(versionType: VersionType): IndexDefinition = {
      _request.versionType(versionType)
      this
    }

    def fields(map: Map[String, Any]): IndexDefinition = fields(map.toList)
    def fields(_fields: (String, Any)*): IndexDefinition = fields(_fields.toIterable)
    def fields(iterable: Iterable[(String, Any)]): IndexDefinition = {
      this._fields ++= iterable
      this
    }

    def doc(source: DocumentSource) = {
      this._source = Option(source)
      this
    }
    @deprecated("renamed to doc", "1.0")
    def source(source: Source) = doc(source)
  }
}
