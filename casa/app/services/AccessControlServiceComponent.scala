package services

import play.api.Logger

trait AccessControlServiceComponent {
  def accessControlService = if (play.api.Play.current.configuration.getBoolean("enableStub") == Some(true)){
    Logger.info("Using ac stub")
    AccessControlServiceStub
  }else{
    Logger.info("Using ac impl")
    AccessControlServiceImpl
  }
}
