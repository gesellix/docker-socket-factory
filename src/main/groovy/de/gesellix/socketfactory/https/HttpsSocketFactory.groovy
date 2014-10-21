package de.gesellix.socketfactory.https

import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLContexts
import org.apache.http.conn.ssl.SSLSocketFactory
import socketfactory.spi.SocketFactory

import static de.gesellix.socketfactory.https.KeyStoreUtil.KEY_STORE_PASSWORD

class HttpsSocketFactory implements SocketFactory {

  @Delegate
  private SSLSocketFactory delegate

  @Override
  def supports(uri) {
    def parsedUri = new URI(uri)
    def dockerCertPath = getDockerCertPath()
    (2376 == parsedUri.port || "https" == parsedUri.scheme) && new File(dockerCertPath).isDirectory()
  }

  @Override
  def sanitize(uri) {
    uri.replaceAll("^tcp://", "https://")
  }

  @Override
  def configure(httpClient, String sanitizedUri) {
    delegate = new SSLSocketFactory(createSslContext())
    def parsedUri = new URI(sanitizedUri)
    def httpsScheme = new Scheme(parsedUri.scheme, delegate, parsedUri.port)
    httpClient.connectionManager.schemeRegistry.register(httpsScheme)
  }

  def getDockerCertPath() {
    System.getProperty("docker.cert.path", System.env.DOCKER_CERT_PATH)
  }

  def createSslContext() {
    def dockerCertPath = getDockerCertPath()
    def keyStore = KeyStoreUtil.createDockerKeyStore(new File(dockerCertPath).absolutePath)

    return SSLContexts.custom()
        .useTLS()
        .loadKeyMaterial(keyStore, KEY_STORE_PASSWORD)
        .loadTrustMaterial(keyStore)
        .build()
  }
}
