package $package$

import com.github.dnvriend.http.Sigv4Client

import scala.util.Try

object Main extends App {
  // replace url with the one from samInfo
  Try(Sigv4Client.get("https://jre7wzveq3.execute-api.eu-west-1.amazonaws.com/dev/hello", Map.empty))
      .fold(t => t.printStackTrace(), resp => println(new String(resp.body)))

  // replace url with the one from samInfo
  Try(Sigv4Client.post(Person("Dennis"), "https://jre7wzveq3.execute-api.eu-west-1.amazonaws.com/dev/person", Map.empty))
    .fold(t => t.printStackTrace(), resp => println(new String(resp.body)))

  // replace url with the one from samInfo
  Try(Sigv4Client.put(Person("Dennis"), "https://jre7wzveq3.execute-api.eu-west-1.amazonaws.com/dev/person", Map.empty))
    .fold(t => t.printStackTrace(), resp => println(new String(resp.body)))
}