package model

import java.time.LocalDateTime

/**
  * Created by Hubert Czerpak on 17/04/2018 
  * using 11" MacBook (can't see much on this screen).
  */
class WorkflowExecution(
                         val workflowId : String,
                         val eid : String,
                         val currentStep: Int,
                         val creationDate : LocalDateTime = LocalDateTime.now()) {

  require (currentStep >= 0)

  def ++(): WorkflowExecution = new WorkflowExecution(workflowId, eid, currentStep + 1, creationDate)
}
