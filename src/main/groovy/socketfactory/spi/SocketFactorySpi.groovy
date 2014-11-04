package socketfactory.spi

import org.apache.http.conn.scheme.SchemeLayeredSocketFactory
import org.apache.http.conn.socket.ConnectionSocketFactory

interface SocketFactorySpi extends ConnectionSocketFactory, SchemeLayeredSocketFactory {

  def supports(uri)

  def sanitize(uri)

  def configureFor(uri)
}
