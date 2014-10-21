package socketfactory.spi

import org.apache.http.conn.scheme.SchemeSocketFactory

interface SocketFactory extends SchemeSocketFactory {

  def supports(uri)

  def sanitize(uri)

  def configure(httpClient, String sanitizedUri)
}
