package models


case class PasswordData(userId: String = "", password1: String = "",password2: String = "")

object PasswordData extends {

  def validatePassword(passwordData: PasswordData) = passwordData.password1 == passwordData.password2

}