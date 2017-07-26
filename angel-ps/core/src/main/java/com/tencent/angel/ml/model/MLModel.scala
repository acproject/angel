package com.tencent.angel.ml.model

import java.util.{HashMap, Map}
import com.tencent.angel.conf.AngelConf
import com.tencent.angel.ml.feature.LabeledData
import com.tencent.angel.ml.predict.PredictResult
import com.tencent.angel.worker.storage.DataBlock
import com.tencent.angel.worker.task.TaskContext
import org.apache.hadoop.conf.Configuration
import scala.collection.JavaConversions._

/**
  * Model for a Algorithm. One MLModel is composed by one or multi PSModel, each one can be referred directly with Model name
  *
  * MLModel can be load from Path and save to Path, used to do prediction.
  *
  * @param _ctx
  */

abstract class MLModel(conf: Configuration, _ctx: TaskContext = null) {
  implicit def ctx : TaskContext = _ctx
  private val psModels: Map[String, PSModel[_]] = new HashMap[String, PSModel[_]]

  /**
    * Get all PSModels
    *
    * @return a name to PSModel map
    */
  def getPSModels: Map[String, PSModel[_]] = {
    return psModels
  }

  /**
    * Get a PSModel use name. With this method, user can refer to one PSModel simply with mlModel.psModelName
    *
    * @param name PSModel name
    * @return
    */
  def getPSModel(name: String): PSModel[_] = {
    return psModels.get(name)
  }

  /**
    * Add a new PSModel
    *
    * @param name PSModel name
    * @param psModel PSModel
    */
  def addPSModel(name: String, psModel: PSModel[_]):this.type={
    psModels.put(name, psModel)
    this
  }

  /**
    * Add a new PSModel
    *
    * @param psModel PSModel
    */
  def addPSModel(psModel: PSModel[_]):this.type = {
    psModels.put(psModel.modelName, psModel)
    this
  }

  /**
    * Predict use the PSModels and predict data
    *
    * @param storage predict data
    * @return predict result
    */
  def predict(storage: DataBlock[LabeledData]): DataBlock[PredictResult]

  /**
    * Set save path for the PSModels
    *
    * @param conf Application configuration
    */
  def setSavePath(conf: Configuration): this.type ={
    val path = conf.get(AngelConf.ANGEL_SAVE_MODEL_PATH)
    if (path != null)
      psModels.values().foreach {case model: PSModel[_] =>
        if (model.needSave) model.setSavePath(path)
      }
    this
  }

  /**
    * Set PSModels load path
    *
    * @param conf Application configuration
    */
  def setLoadPath(conf: Configuration): this.type ={
    val path = conf.get(AngelConf.ANGEL_LOAD_MODEL_PATH)
    if (path != null)
      psModels.values().foreach {case model: PSModel[_] =>
        if (model.needSave) model.setLoadPath(path)
      }
    this
  }


}
