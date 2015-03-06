package controllers


import org.joda.time.format.DateTimeFormat
import org.specs2.mutable.Specification
import play.api.Logger
//import services.{ClaimsRepositoryComponent, WithApplicationAndRealDBAndRealRenderService}

import scala.xml.{Elem, XML}

class RenderServiceIntegrationSpec extends Specification {
//  "Application" should {
//    "retrieve rendered claim from real render service" in new WithApplicationAndRealDBAndRealRenderService with ClaimsRepositoryComponent {
//      val xml = XML.loadString(scala.io.Source.fromURL(getClass.getResource("/c3_functional3_claim_output.xml")).mkString)
//      val transactionId:String = (xml \ "DWPCATransaction" \ "TransactionId").text
//
//      DB.withConnection("carersservicedb") { implicit connection =>
//        insertClaim(transactionId, xml)
//
//        val claimType = if ((xml \"DWPCATransaction" \ "DWPCAClaim").size > 0) "claim" else if((xml \ "DWPCATransaction"\ "DWPCAChangeOfCircumstances").size > 0) "circs" else ""
//        val nameSurname = getNameSurname(xml, claimType)
//        val name = nameSurname._1
//        val surname = nameSurname._2
//        val nino = getNino(xml, claimType)
//        val claimDate = getTimeGenerated(xml, claimType)
//        val status = "received"
//
//        val propsMap = Map("claimType" -> claimType,"nino"->nino,"forename"->name,"surname"->surname,"claimDateTime"->claimDate,"status"->status)
//
//        Logger.debug("ClaimSummary keyValue Map:"+propsMap)
//
//        propsMap.foreach{ keyValue =>
//          SQL(
//            """
//          INSERT INTO carers.claimsummary VALUES({transId},{key},{value});
//            """
//          ).on("transId"->transactionId,"key"->keyValue._1,"value"->keyValue._2).executeInsert(get[String]("transid") singleOpt)
//        }
//      }
//
//      claimsRepository.claimHtml(transactionId) must not(beNone)
//    }
//  }
//
//  private def insertClaim(transactionId: String, message: Elem)(implicit connection: Connection):Option[String] =
//    SQL(
//      """
//        INSERT INTO carers.claim VALUES ({transId},{xml});
//      """
//    ).on("transId" -> transactionId,"xml" -> message.mkString).executeInsert(get[String]("transid") singleOpt)

  private def getNameSurname(message:Elem,claimType:String):(String,String) = {
    if (claimType == "claim"){
      (message \"DWPCATransaction" \ "DWPCAClaim" \ "Claimant" \ "OtherNames" \ "Answer").text -> (message \ "DWPCATransaction" \ "DWPCAClaim" \ "Claimant" \ "Surname" \ "Answer").text
    }else if(claimType == "circs"){
      val fullName = (message \"DWPCATransaction" \ "DWPCAChangeOfCircumstances" \ "ClaimantDetails" \ "FullName" \ "Answer").text
      fullName -> ""
    }else{
      "" -> ""
    }
  }

  private def getNino(message:Elem, claimType:String):String = {
    if (claimType =="claim") (message \"DWPCATransaction" \ "DWPCAClaim" \ "Claimant" \ "NationalInsuranceNumber" \ "Answer").text
    else if (claimType == "circs") (message \"DWPCATransaction" \ "DWPCAChangeOfCircumstances" \ "ClaimantDetails" \ "NationalInsuranceNumber" \ "Answer").text
    else ""
  }

  private def getTimeGenerated(message:Elem, claimType:String):String = {
    val dateText = (message \ "DWPCATransaction" \ "DateTimeGenerated").text

    DateTimeFormat.forPattern("ddMMyyyHHmm").print(DateTimeFormat.forPattern("dd-MM-yyy HH:mm").parseDateTime(dateText))
  }
}
