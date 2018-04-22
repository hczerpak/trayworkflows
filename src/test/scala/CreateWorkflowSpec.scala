import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._

/**
  * Created by Hubert Czerpak on 17/04/2018
  * using 11" MacBook (can't see much on this screen).
  *
  * Specs pt. 1
  */
class CreateWorkflowSpec extends ApiSpecsDefaults {

  "POST /workflows with json body: {\"number_of_steps\": <integer>}" should "create a new workflow with a given number of steps" in {
    val stepsNumber = 10
    Post(s"/workflows", CreateRequest(stepsNumber)) ~> routes ~> check {
      val resp = responseAs[CreateResponse]
      workflows.get(resp.workflow_id) match {
        case Some(x) => x.numberOfSteps shouldBe stepsNumber
        case None => fail(s"Can't find a workflow with id=${resp.workflow_id}")
      }
    }
  }

  it should "return 201" in {
    Post(s"/workflows", CreateRequest(2)) ~> routes ~> check {
      status shouldBe Created
    }
  }

  it should """return json body: {"workflow_id": <string>}""" in {
    Post(s"/workflows", CreateRequest(2)) ~> routes ~> check {
      contentType shouldBe `application/json`
    }
  }

  it should "be able to support tens of invocations per second" in {
    //TODO: write scala meter benchmark
  }
}
