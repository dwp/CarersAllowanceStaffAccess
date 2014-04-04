package services

import services.mock.ClaimsServiceStub

trait ClaimService {
  val claimService = ClaimsServiceStub()
}
