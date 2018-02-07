![Logo image](img/sbtscalasamlogo_small.png)

# sam-sigv4-seed.g8
A template project for quickly creating serverless applications secured with 
[Signature Version 4 (sigv4)]((https://docs.aws.amazon.com/apigateway/api-reference/signing-requests/)). 
For more information see [sbt-sam](https://github.com/dnvriend/sbt-sam)

## Usage
Create a new template project:

```
sbt new dnvriend/sam-sigv4-seed.g8
```

## Tasks
- To deploy the project type `samDeploy`
- To remove the project type `samRemove`
- To remove the project type `samInfo`

## Example
To enable the `sigv4` authorization type, the following must be added to `sam.conf`:

```
// set the authorizer type
authorizer.type="sigv4" // defaults to cognito
```

Also, an environment variable has been set in `sam.conf`. You can set an arbitraty number of environment variables in `sam.conf` Please note that AWS limits the total size of all environment variables to 4kB.

```
// set arbitrary lambda environment variables
lambda-env-vars {
  foo="bar"
}
```

Because `sam.conf` makes use of [lightbend config](https://github.com/lightbend/config), you can substitute environment variables, please look at the [lightbend config](https://github.com/lightbend/config) for the syntax and usage.

## Lambdas
The project consists of three lambdas that define authorization:

```scala
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
```

## Sigv4Client
There is also a client that makes use of the `com.github.dnvriend.http.Sigv4Client` to sign the request and invoke the
lambda:

```scala
import com.github.dnvriend.http.Sigv4Client

import scala.util.Try

object Main extends App {
  Try(Sigv4Client.get("https://jre7wzveq3.execute-api.eu-west-1.amazonaws.com/dev/hello", Map.empty))
      .fold(t => t.printStackTrace(), resp => println(new String(resp.body)))

  Try(Sigv4Client.post(Person("Dennis"), "https://jre7wzveq3.execute-api.eu-west-1.amazonaws.com/dev/person", Map.empty))
    .fold(t => t.printStackTrace(), resp => println(new String(resp.body)))

  Try(Sigv4Client.put(Person("Dennis"), "https://jre7wzveq3.execute-api.eu-west-1.amazonaws.com/dev/person", Map.empty))
    .fold(t => t.printStackTrace(), resp => println(new String(resp.body)))
}
```