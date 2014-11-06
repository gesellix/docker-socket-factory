package de.gesellix.socketfactory.https

import org.apache.http.HttpHost
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.SSLContexts
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import org.apache.http.protocol.HttpContext
import socketfactory.spi.SocketFactorySpi

import static de.gesellix.socketfactory.https.KeyStoreUtil.KEY_STORE_PASSWORD

class HttpsSocketFactory implements SocketFactorySpi {

  @Delegate
  private SSLConnectionSocketFactory delegate

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
  def configureFor(sanitizedUri) {
    delegate = new SSLConnectionSocketFactory(createSslContext())
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

  @Override
  @Deprecated
  Socket createSocket(HttpParams params) throws IOException {
    return createSocket((HttpContext) null)
  }

  @Override
  @Deprecated
  Socket connectSocket(
      Socket socket,
      InetSocketAddress remoteAddress,
      InetSocketAddress localAddress,
      HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {

    int connTimeout = HttpConnectionParams.getConnectionTimeout(params)
    return connectSocket(connTimeout, socket, new HttpHost(remoteAddress.address), remoteAddress, localAddress, null)
  }

  @Override
  @Deprecated
  Socket createLayeredSocket(
      Socket socket,
      String target,
      int port,
      HttpParams params) throws IOException, UnknownHostException {
    return delegate.createLayeredSocket(socket, target, port, null)
  }

  @Override
  @Deprecated
  boolean isSecure(Socket sock) throws IllegalArgumentException {
    return true
  }
}
