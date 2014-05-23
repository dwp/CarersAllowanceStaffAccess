package services


trait AccessControlServiceComponent {
  def accessControlService = if (play.api.Play.current.configuration.getBoolean("enableStub") == Some(true)){
    AccessControlServiceStub
  }else{
    AccessControlServiceImpl
  }
}
