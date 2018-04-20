import java.util.UUID

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity
import model.Workflow
import model.WorkflowActor.Execute

/**
  * Created by Hubert Czerpak on 18/04/2018 
  * using 11" MacBook (can't see much on this screen).
  */
/**
  * Specs pt. 2
  */
class CreateExecutionSpec extends ApiSpecsDefaults {

  val id: String = UUID.randomUUID().toString
  workflows.set(id, new Workflow(id, 10)) //executions can be created for this workflow

  "POST /workflows/<workflow_id>/executions with empty body" should "return 201 CREATED" in {

    workflows.set(id, new Workflow(id, 10)) //executions can be created for this workflow

    Post(s"/workflows/$id/executions", HttpEntity(`application/json`, executeRequestFormat.write(Execute(id)).toString)) ~> routes ~> check {
      status shouldBe Created
      contentType shouldBe `application/json`
    }
  }

  it should "and json body: {\"workflow_execution_id\": <string>} if the execution could be created" in {
    val responeFormat = """{"workflow_execution_id":[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}"""

    workflows.set(id, new Workflow(id, 10)) //executions can be created for this workflow

    Post(s"/workflows/$id/executions", HttpEntity(`application/json`, executeRequestFormat.write(Execute(id)).toString())) ~> routes ~> check {
      val resp = responseAs[ExecuteResponse]
      it should "create a new execution and initial value of current step should be 0" in {
        executions.get(resp.workflow_execution_id) match {
          case Some(e) => assert(e.currentStep == 0)
          case None => fail("didn't create an execution")
        }
      }
    }
  }

  it should "404 NOT FOUND if the workflow does not exist" in {
    val invalidId = UUID.randomUUID().toString
    Post(s"/workflows/$invalidId/executions", HttpEntity(`application/json`, executeRequestFormat.write(Execute(invalidId)).toString())) ~> routes ~> check {
      status shouldBe NotFound
    }
  }

  it should "be able to support several hundred of invocations per second" in {
    fail("TODO: write scala meter benchmark")
  }
}
