package services

import services.mock.ClaimsServiceStub

trait ClaimServiceComponent {
  val claimService = ClaimsServiceStub()
}
