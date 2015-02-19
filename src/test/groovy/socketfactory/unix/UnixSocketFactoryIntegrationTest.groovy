package socketfactory.unix

import de.gesellix.socketfactory.httpclient.DockerHttpClientFactory
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.apache.commons.lang.SystemUtils
import org.apache.http.client.HttpClient
import spock.lang.IgnoreIf
import spock.lang.Specification

@IgnoreIf({ !SystemUtils.IS_OS_LINUX })
class UnixSocketFactoryIntegrationTest extends Specification {

  File defaultDockerSocket = new File("/var/run/docker.sock")
  def runDummyDaemon = !defaultDockerSocket.exists()
  File socketFile = defaultDockerSocket
  RESTClient dockerClient

  def setup() {
    if (true || !defaultDockerSocket.exists()) {
      runDummyDaemon = true
      socketFile = new File(new File(System.getProperty("java.io.tmpdir")), "unixsocket-dummy.sock")
      socketFile.deleteOnExit()
    }
    def unixSocket = "unix://${socketFile.getCanonicalPath()}".toString()
    dockerClient = createHttpClient(unixSocket)
  }

  def createHttpClient(String dockerHost) {
    def httpClientFactory = new DockerHttpClientFactory(dockerHost)
    def restClient = new RESTClient(httpClientFactory.sanitizedUri) {

      private client

      @Override
      HttpClient getClient() {
        if (client == null) {
          this.client = httpClientFactory.createOldHttpClient()
        }
        return this.client
      }
    }
    return restClient
  }

  def "info via unix socket"() {
    given:
    def responseBody = '{"a-key":42,"another-key":4711}'
    def expectedResponse = [
        "HTTP/1.1 200 OK",
        "Content-Type: application/json",
        "Job-Name: unix socket test",
        "Date: Thu, 08 Jan 2015 23:05:55 GMT",
        "Content-Length: ${responseBody.length()}",
        "",
        responseBody
    ]

    def testserver = null
    if (runDummyDaemon) {
      testserver = new UnixSocketTestServer(socketFile)
      testserver.with {
        constantResponse = expectedResponse.join("\n")
      }
      testserver.runInNewThread()
    }

    when:
    def ping = dockerClient.get([path: "/_ping"])

    then:
    ping.data == ["a-key": 42, "another-key": 4711]

    cleanup:
    testserver?.stop()
  }
}
