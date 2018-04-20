import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._

/**
  * Created by Hubert Czerpak on 17/04/2018
  * using 11" MacBook (can't see much on this screen).
  */

/**
  * Specs pt. 1
  */
class CreateWorkflow extends ApiSpecsDefaults {

  "POST /workflows with json body: {\"number_of_steps\": <integer>}" should "create a new workflow with a given number of steps" in {

    val stepsNumber = 10

    Post(s"/workflows", CreateRequest(stepsNumber)) ~> routes ~> check {
      val resp = responseAs[CreateResponse]
      workflows.get(resp.workflow_id)match {
        case Some(e) => assert(e.numberOfSteps == stepsNumber)
        case None => fail("didn't create a workflow")
      }

      it should "return 201" in {
        status shouldBe Created
      }

      it should "return json body: {\"workflow_id\": <string>}" in {
        contentType shouldBe `application/json`
      }
    }
  }

  it should "be able to support tens of invocations per second" in {
    fail("TODO: write scala meter benchmark")
  }
}
