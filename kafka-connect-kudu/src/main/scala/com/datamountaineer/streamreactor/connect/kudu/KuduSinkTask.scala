package com.datamountaineer.streamreactor.connect.kudu

import java.util

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.connect.sink.{SinkRecord, SinkTask}

import scala.collection.JavaConverters._

/**
  * Created by andrew@datamountaineer.com on 22/02/16. 
  * stream-reactor
  */
class KuduSinkTask extends SinkTask with StrictLogging {
  private var writer : Option[KuduWriter] = None

  /**
    * Parse the configurations and setup the writer
    * */
  override def start(props: util.Map[String, String]): Unit = {
    logger.info(
      """
        |    ____        __        __  ___                  __        _
        |   / __ \____ _/ /_____ _/  |/  /___  __  ______  / /_____ _(_)___  ___  ___  _____
        |  / / / / __ `/ __/ __ `/ /|_/ / __ \/ / / / __ \/ __/ __ `/ / __ \/ _ \/ _ \/ ___/
        | / /_/ / /_/ / /_/ /_/ / /  / / /_/ / /_/ / / / / /_/ /_/ / / / / /  __/  __/ /
        |/_____/\__,_/\__/\__,_/_/  /_/\____/\__,_/_/ /_/\__/\__,_/_/_/ /_/\___/\___/_/
        |       __ __          __      _____ _       __
        |      / //_/_  ______/ /_  __/ ___/(_)___  / /__
        |     / ,< / / / / __  / / / /\__ \/ / __ \/ //_/
        |    / /| / /_/ / /_/ / /_/ /___/ / / / / / ,<
        |   /_/ |_\__,_/\__,_/\__,_//____/_/_/ /_/_/|_|
        |
        |
        |by Andrew Stevenson
      """.stripMargin)



    KuduSinkConfig.config.parse(props)
    val sinkConfig = new KuduSinkConfig(props)
    writer = Some(KuduWriter(config = sinkConfig, context = context))
  }

  override def open (partitions: util.Collection[TopicPartition]) {
    writer.get.addPartitions(partitions.asScala.toList)
  }

  /**
    * Pass the SinkRecords to the writer for Writing
    * */
  override def put(records: util.Collection[SinkRecord]): Unit = {
    require(writer.nonEmpty, "Writer is not set!")
    writer.foreach(w=>w.write(records.asScala.toList))
  }

  /**
    * Clean up writer
    * */
  override def stop(): Unit = {
    logger.info("Stopping Kudu sink.")
    writer.foreach(w => w.close())
  }

  //0.7 has
  override def flush(map: util.Map[TopicPartition, OffsetAndMetadata]): Unit = {
    require(writer.nonEmpty, "Writer is not set!")
    writer.map(w=>w.flush())
  }
  override def version(): String = ""
}
