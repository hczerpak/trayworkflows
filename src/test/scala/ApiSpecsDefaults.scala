import akka.event.Logging
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActorRef
import cache.Cache
import com.typesafe.config.Config
import model.{Workflow, WorkflowExecution, WorkflowActor}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Hubert Czerpak on 18/04/2018 
  * using 11" MacBook (can't see much on this screen).
  */
abstract class ApiSpecsDefaults extends FlatSpec
  with ScalatestRouteTest with Service with Matchers {

  override val logger = Logging(system, getClass)
  val workflows: Cache[Workflow] = new Cache[model.Workflow] {}
  val executions: Cache[WorkflowExecution] = new Cache[model.WorkflowExecution] {}
  val handler: TestActorRef[WorkflowActor] = TestActorRef[WorkflowActor](new WorkflowActor(workflows, executions))

  override def config: Config = testConfig
}