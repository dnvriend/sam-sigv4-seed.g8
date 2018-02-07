package $package$

import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.HttpHandler
import play.api.libs.json._
import scalaz._
import scalaz.Scalaz._

@HttpHandler(path = "/hello", method = "get", authorization = true)
class GETHello extends ApiGatewayHandler {
  override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
    HttpResponse.ok.withBody(Json.toJson("Hello world"))
  }
}

object Person {
  implicit val format: OFormat[Person] = Json.format
}

case class Person(name: String)

@HttpHandler(path = "/person", method = "put", authorization = true)
class PUTPerson extends JsonApiGatewayHandler[Person] {
  override def handle(value: Option[Person],
                      pathParams: Map[String, String],
                      requestParams: Map[String, String],
                      request: HttpRequest, ctx: SamContext): HttpResponse = {
    val fooEnvironmentVar: Option[String] = sys.env.get("foo")
    (fooEnvironmentVar |@| value) ((_, _))
      .fold(HttpResponse.validationError.withBody(Json.toJson("Could not unmarshal the person"))) {
        case (foo, person) => HttpResponse.ok.withBody(Json.toJsObject(person) ++ Json.obj("method" -> "put", "foo" -> foo))
      }
  }
}

@HttpHandler(path = "/person", method = "post", authorization = true)
class POSTPerson extends JsonApiGatewayHandler[Person] {
  override def handle(value: Option[Person],
                      pathParams: Map[String, String],
                      requestParams: Map[String, String],
                      request: HttpRequest, ctx: SamContext): HttpResponse = {
    val fooEnvironmentVar: Option[String] = sys.env.get("foo")
    (fooEnvironmentVar |@| value) ((_, _))
      .fold(HttpResponse.validationError.withBody(Json.toJson("Could not unmarshal the person"))) {
        case (foo, person) => HttpResponse.ok.withBody(Json.toJsObject(person) ++ Json.obj("method" -> "post", "foo" -> foo))
      }
  }
}