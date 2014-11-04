package socketfactory

import socketfactory.spi.SocketFactorySpi

class SocketFactoryService {

  private static SocketFactoryService service
  private ServiceLoader<SocketFactorySpi> loader

  private SocketFactoryService() {
    loader = ServiceLoader.load(SocketFactorySpi)
  }

  static synchronized SocketFactoryService getInstance() {
    if (service == null) {
      service = new SocketFactoryService()
    }
    return service
  }

  def getSchemeSocketFactory(uri) {
    def socketFactory = null

    try {
      Iterator<SocketFactorySpi> socketFactories = loader.iterator()
      while (socketFactory == null && socketFactories.hasNext()) {
        SocketFactorySpi candidate = socketFactories.next()
        if (candidate.supports(uri)) {
          socketFactory = candidate
        }
      }
    }
    catch (ServiceConfigurationError serviceError) {
      socketFactory = null
      serviceError.printStackTrace()
    }
    return socketFactory
  }
}
