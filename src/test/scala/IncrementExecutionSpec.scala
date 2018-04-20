import java.time.LocalDateTime
import java.util.UUID

import model.{Workflow, WorkflowExecution}
import model.WorkflowActor.Increment
import akka.http.scaladsl.model.StatusCodes._

/**
  * Created by Hubert Czerpak on 18/04/2018 
  * using 11" MacBook (can't see much on this screen).
  */
/**
  * Specs pt. 3
  */
class IncrementExecutionSpec extends ApiSpecsDefaults {

  val id: String = UUID.randomUUID().toString
  val eid: String = UUID.randomUUID().toString

  "PUT /workflows/<workflow_id>/executions/<workflow_execution_id> with empty body" should "increments current step " +
    "by 1 if current step < number of steps - 1 (number of steps of the workflow corresponding to this execution) or " +
    "does nothing otherwise" in {

    workflows.set(id, new Workflow(id, 10))
    executions.set(eid, new WorkflowExecution(id, eid, 0, LocalDateTime.now()))

    Put(s"/workflows/$id/executions/$eid", Increment(id, eid)) ~> routes ~> check {
      executions.get(eid) match {
        case Some(x) => x.currentStep shouldEqual 1
        case None => fail(s"there should be an execution with id=$eid")
      }
    }

  }

  it should "return 204 NO CONTENT if the current step could be incremented" in {

    workflows.set(id, new Workflow(id, 10))
    executions.set(eid, new WorkflowExecution(id, eid, 0, LocalDateTime.now()))

    Put(s"/workflows/$id/executions/$eid", Increment(id, eid)) ~> routes ~> check {
      status shouldBe NoContent
    }
  }

  it should "return 400 BAD REQUEST otherwise" in {

    workflows.set(id, new Workflow(id, 10))
    executions.set(eid, new WorkflowExecution(id, eid, 9 /* last step */, LocalDateTime.now()))

    Put(s"/workflows/$id/executions/$eid", Increment(id, eid)) ~> routes ~> check {
      status shouldBe BadRequest
    }
  }

  it should "404 NOT FOUND if the workflow or the workflow execution does not exist" in {
    val randomid = UUID.randomUUID().toString
    workflows.set(id, new Workflow(id, 10))
    executions.set(eid, new WorkflowExecution(id, eid, 0, LocalDateTime.now()))

    Put(s"/workflows/$id/executions/$randomid", Increment(id, randomid)) ~> routes ~> check {
      status shouldBe NotFound
    }

    Put(s"/workflows/$randomid/executions/$eid", Increment(randomid, eid)) ~> routes ~> check {
      status shouldBe NotFound
    }
  }

  it should "be able to support several thousand of invocations per second" in {
    fail("TODO: write scala meter benchmark")
  }
}
