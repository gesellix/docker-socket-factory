package de.gesellix.socketfactory.unix

import org.apache.http.HttpHost
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.conn.socket.LayeredConnectionSocketFactory
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import org.apache.http.protocol.HttpContext
import org.newsclub.net.unix.AFUNIXSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import socketfactory.spi.SocketFactorySpi

class UnixSocketFactory implements SocketFactorySpi {

  @Delegate
  LayeredConnectionSocketFactory delegate

  File socketFile

  def UnixSocketFactory() {
  }

  @Override
  def supports(uri) {
    def scheme = uri[0..uri.indexOf(':') - 1]
    "unix" == scheme
  }

  @Override
  def sanitize(uri) {
    uri.replaceAll("^unix://", "unix://localhost")
  }

  @Override
  def configureFor(sanitizedUri) {
    this.socketFile = new File(sanitizedUri.replaceAll("unix://localhost", "") as String)
  }

  @Override
  Socket createSocket(HttpContext context) throws IOException {
    AFUNIXSocket socket = AFUNIXSocket.newInstance();
    return socket
  }

  @Override
  Socket connectSocket(
      int connectTimeout,
      Socket socket,
      HttpHost host,
      InetSocketAddress remoteAddress,
      InetSocketAddress localAddress,
      HttpContext context) throws IOException {
    try {
      socket.connect(new AFUNIXSocketAddress(socketFile), connectTimeout)
    }
    catch (SocketTimeoutException e) {
      throw new ConnectTimeoutException("Connect to '" + socketFile + "' timed out")
    }

    return socket
  }

  @Override
  @Deprecated
  Socket createSocket(HttpParams params) throws IOException {
    return createSocket((HttpContext) null)
  }

  @Override
  @Deprecated
  Socket createLayeredSocket(
      Socket socket,
      String target,
      int port,
      HttpParams params) throws IOException, UnknownHostException {
    return createSocket(params)
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
  boolean isSecure(Socket sock) throws IllegalArgumentException {
    // this is a fake
    return true
  }
}
