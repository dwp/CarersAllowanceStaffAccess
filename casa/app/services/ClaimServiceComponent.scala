package services


trait ClaimServiceComponent {
  def claimService = if (play.api.Play.current.configuration.getBoolean("enableStub") == Some(true)){
    ClaimServiceMock
  } else {
    ClaimServiceImpl
  }
}
