package services

import play.api.Logger


trait ClaimServiceComponent {
  def claimService = if (play.api.Play.current.configuration.getBoolean("enableStub") == Some(true)){
    Logger.info("Using the mock")
    ClaimServiceMock
  } else {
    Logger.info("Using the impl")
    ClaimServiceImpl
  }
}
