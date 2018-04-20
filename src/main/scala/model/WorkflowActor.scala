package model

/**
  * Created by Hubert Czerpak on 17/04/2018 
  * using 11" MacBook (can't see much on this screen).
  */

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import cache.Cache

import scala.concurrent.ExecutionContextExecutor

object WorkflowActor {

  def actorProps(c1: Cache[Workflow],
                 c2: Cache[WorkflowExecution]):Props = Props(new WorkflowActor(c1, c2))

  //messages
  case class Create(stepsCount: Int)            //creates new workflow
  case class Created(id: String)
  case class Execute(id: String)                //creates new execution
  case class Executed(eid: String)
  case class Increment(id:String, eid: String)  //increments an execution by id & exId
  case class Incremented(id:String, eid:String)
  case class Find(id:String, eid: String)       //gets an execution by id & exId
  case class FindResult(complete:Boolean)

  case class Missing()
  case class Invalid()
}

class WorkflowActor(workflows: Cache[Workflow],
                    executions: Cache[WorkflowExecution]) extends Actor with ActorLogging {

  implicit val ec: ExecutionContextExecutor = context.dispatcher

  import WorkflowActor._

  override def receive: Receive = {

    case Create(stepsCount) =>
      val id = UUID.randomUUID().toString
      workflows.set(id, new Workflow(id, stepsCount))
      sender ! Created(id)

    case Execute(id) =>
      workflows.get(id) match {
        case Some(_) =>
          val eid = UUID.randomUUID().toString
          executions.set(eid, new WorkflowExecution(id, eid, 0))
          sender ! Executed(eid)
        case None => sender ! Missing
      }

    case Increment(id, eid) =>
      workflows.get(id) match {
        case Some(w) =>
          executions.get(eid) match {
            case Some(exec) if exec.currentStep < w.numberOfSteps - 1 =>
              executions.set(eid, new WorkflowExecution(id, eid, exec.currentStep + 1))
              sender ! Incremented(id, eid)
            case Some(_) => sender ! Invalid
            case None => sender ! Missing
          }
        case None => sender ! Missing
      }
    case Find(id, eid) =>
      workflows.get(id) match {
        case Some(w) =>
          executions.get(eid) match {
            case Some(exec) => sender ! FindResult(exec.currentStep >= w.numberOfSteps - 1)
            case None => sender ! Missing
          }
        case None => sender ! Missing
      }
    case _ => sender ! Invalid
  }
}