import java.util.UUID
import java.util.concurrent.TimeUnit

import _root_.model.{Workflow, WorkflowActor, WorkflowExecution}
import akka.actor.{ActorRef, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import cache.Cache
import com.typesafe.config.{Config, ConfigFactory}
import model.WorkflowActor.{Created => _, _}
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration

/**
  * Created by Hubert Czerpak on 17/04/2018 
  * using 11" MacBook (can't see much on this screen).
  */

// domain model
final case class CreateRequest(number_of_steps: Int)

final case class CreateResponse(workflow_id: String)

final case class ExecuteResponse(workflow_execution_id: String)

final case class FindResponse(finished: Boolean)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val createRequestFormat = jsonFormat1(CreateRequest)
  implicit val createResponseFormat = jsonFormat1(CreateResponse)
  implicit val executeResponseFormat = jsonFormat1(ExecuteResponse)
  implicit val findResponseFormat = jsonFormat1(FindResponse)

  implicit val findConversion = jsonFormat2(Find)
  implicit val incrementConversion = jsonFormat2(Increment)
  implicit val executeRequestFormat = jsonFormat1(Execute)
}

trait Service extends Directives with JsonSupport {

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit def requestTimeout = Timeout(Duration(50, TimeUnit.MILLISECONDS))

  val logger: LoggingAdapter

  def config: Config

  def handler: ActorRef

  val routes =

    pathPrefix("workflows") {
      pathEnd {
        post {
          entity(as[CreateRequest]) { request =>
            val steps = request.number_of_steps
            val id = UUID.randomUUID().toString

            respondWithHeaders(`Content-Type`(`application/json`)) {
              complete {
                (handler ? Create(steps)).map(_ => CreateResponse(id))
              }
            }
          }
        }
      }
    } ~
      path("workflows" / JavaUUID / "executions") { uid =>
        val id = uid.toString
        pathEnd {
          post {
            respondWithHeaders(`Content-Type`(`application/json`)) {
              complete {
                (handler ? Execute(id)).map {
                  case Executed(x) => Created -> executeResponseFormat.write(ExecuteResponse(x)).toString
                  case _ => NotFound -> ""
                }
              }
            }
          }
        }
      } ~
      path("workflows" / JavaUUID / "executions" / JavaUUID) { (uid, euid) =>
        val id = uid.toString
        val eid = euid.toString
        put {
          //try to find the execution and increment or fail
          complete {
            (handler ? Increment(id, eid)).map {
              case Incremented => NoContent -> ""
              case Missing => NotFound -> ""
              case _ => BadRequest -> ""
            }
          }
        }
        get {
          respondWithHeaders(`Content-Type`(`application/json`)) {
            complete {
              (handler ? Find(id.toString, eid)).map {
                case FindResult(complete) => OK -> findResponseFormat.write(FindResponse(complete)).toString()
                case Missing => NotFound -> ""
                case _ => BadRequest -> ""
              }
            }
          }
        }
      }
}

object TrayService extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  val handler = system.actorOf(WorkflowActor.actorProps(new Cache[Workflow] {}, new Cache[WorkflowExecution] {}))

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}