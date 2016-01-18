package api

import com.softwaremill.clippy._
import dal.AdvicesRepository
import util.email.EmailService

import scala.concurrent.{ExecutionContext, Future}

class UiApiImpl(
    advicesRepository: AdvicesRepository,
    emailService: EmailService,
    contactEmail: String
)(implicit ec: ExecutionContext) extends UiApi {

  override def sendCannotParse(errorText: String, contributorEmail: String) = {
    emailService.send(contactEmail, "Unparseable message",
      s"""
         |Contributor email: $contributorEmail
         |Error text:
         |$errorText
       """.stripMargin)
  }

  override def sendAdviceProposal(ap: AdviceProposal): Future[Unit] = {
    advicesRepository
      .store(ap.compilationError, ap.advice, accepted = false, ap.library, ap.contributor, ap.comment)
      .flatMap { a =>
        emailService.send(contactEmail, "New advice proposal", s"Advice proposal: $a")
      }
  }

  override def listAccepted() = {
    advicesRepository.findAll().map(_.map(_.toAdviceListing))
  }
}