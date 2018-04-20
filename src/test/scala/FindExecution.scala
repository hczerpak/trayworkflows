import java.time.LocalDateTime
import java.util.UUID

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._
import model.WorkflowActor.Find
import model.{Workflow, WorkflowExecution}

import scala.util.Random

/**
  * Created by Hubert Czerpak on 17/04/2018
  * using 11" MacBook (can't see much on this screen).
  */

/**
  * Specs pt. 4
  */
class FindExecution extends ApiSpecsDefaults {

  val id: String = UUID.randomUUID().toString
  val eid: String = UUID.randomUUID().toString

  val numberOfSteps = Random.nextInt(100) + 1
  workflows.set(id, new Workflow(id, numberOfSteps))
  executions.set(eid, new WorkflowExecution(id, eid, numberOfSteps - 1, LocalDateTime.now()))

  "GET /workflows/<workflow_id>/executions/<workflow_execution_id>" should "return: 200 OK" in {

    Get(s"/workflows/$id/executions/$eid", Find(id, eid)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
    }
  }

  it should """return json {"finished": <is_finished> } where is_finished is true if current step >= number of steps - 1""" in {
    Get(s"/workflows/$id/executions/$eid", Find(id, eid)) ~> routes ~> check {
      responseAs[FindResponse].finished shouldBe true
    }
  }

  it should "return false otherwise" in {
    executions.set(eid, new WorkflowExecution(id, eid, 0, LocalDateTime.now()))
    Get(s"/workflows/$id/executions/$eid", Find(id, eid)) ~> routes ~> check {

      status shouldBe OK
      responseAs[FindResponse].finished shouldBe false
    }
  }

  it should "return 404 NOT FOUND if the workflow or the execution does not exist" in {
    val randomId = UUID.randomUUID().toString

    Get(s"/workflows/$randomId/executions/$eid", Find(randomId, eid)) ~> routes ~> check {
      status shouldBe NotFound
    }

    Get(s"/workflows/$id/executions/$randomId", Find(id, randomId)) ~> routes ~> check {
      status shouldBe NotFound
    }
  }

  it should "should be able to support several thousand of invocations per second" in {
    fail("TODO: write scala meter benchmark")
  }
}
