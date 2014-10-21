package de.gesellix.socketfactory.https

import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser

import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Utility class for building up a keystore which can be used in
 * SSL communication.
 *
 * @author roland
 * @since 20.10.14
 */
public class KeyStoreUtil {

  def static KEY_STORE_PASSWORD = "docker".toCharArray()

  def static KeyStore createDockerKeyStore(String certPath) throws IOException, GeneralSecurityException {
    PrivateKey privKey = loadPrivateKey(new File(certPath, "key.pem").absolutePath)
    Certificate[] certs = loadCertificates(new File(certPath, "cert.pem").absolutePath)

    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    keyStore.load(null)

    keyStore.setKeyEntry("docker", privKey, KEY_STORE_PASSWORD, certs)
    addCA(keyStore, new File(certPath, "ca.pem").absolutePath)
    return keyStore
  }

  def static PrivateKey loadPrivateKey(String keyPath) throws IOException, GeneralSecurityException {
    PEMKeyPair keyPair = loadPEM(keyPath)
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyPair.getPrivateKeyInfo().getEncoded())
    return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
  }

  static <T> T loadPEM(String keyPath) throws IOException {
    PEMParser parser = new PEMParser(new BufferedReader(new FileReader(keyPath)))
    return (T) parser.readObject()
  }

  static void addCA(KeyStore keyStore, String caPath) throws KeyStoreException, FileNotFoundException, CertificateException {
    for (Certificate cert : loadCertificates(caPath)) {
      X509Certificate crt = (X509Certificate) cert
      String alias = crt.getSubjectX500Principal().getName()
      keyStore.setCertificateEntry(alias, crt)
    }
  }

  static Certificate[] loadCertificates(String certPath) throws FileNotFoundException, CertificateException {
    InputStream is = new FileInputStream(certPath)
    Collection<? extends Certificate> certs = CertificateFactory.getInstance("X509").generateCertificates(is)
    return new ArrayList<>(certs).toArray(new Certificate[certs.size()])
  }
}
