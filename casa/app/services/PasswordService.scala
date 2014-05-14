package services

import org.jasypt.util.password.StrongPasswordEncryptor

/**
 * A password encryptor service that passes through to the string digester on the backend.
 */
object PasswordService {

  lazy val passwordEncryptor = new StrongPasswordEncryptor()

  /**
   * @param message Plain password
   * @param digest Encrypted password
   * @return Boolean - true if passwords match, false otherwise
   */
  def checkPassword(message: String, digest: String) = passwordEncryptor.checkPassword(message, digest)

  /**
   * @param  password Plain password
   * @return String - the encrypted password
   */
  def encryptPassword(password: String) = passwordEncryptor.encryptPassword(password)
}

