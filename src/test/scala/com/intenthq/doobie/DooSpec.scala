package com.intenthq.doobie

import doobie.util.invariant.NonNullableParameter

class DooSpec extends DbSpecification {

  private val intentHQ = Company(CompanyId(1), "Intent HQ", Some("https://www.intenthq.com"))

  sequential
  "basic operations" >> {

    "select company names" >> {

      "should return all the companies in the db sorted by name ASC" >> {
        Doo.companyNames must beRight(be_===(List("Another Company", "Intent HQ")))
      }

    }

    "select companies as tuples" >> {

      "should return all the companies in the db sorted by name ASC" >> {
        Doo.companyTuples must beRight(
          be_===(List(("Another Company", None), ("Intent HQ", Some("https://www.intenthq.com"))))
        )
      }

    }

    "select companies as case classes" >> {

      "should return all the companies in the db sorted by name ASC" >> {
        Doo.companyCaseClasses must beRight(
          be_===(List(
            Company(CompanyId(2), "Another Company", None),
            intentHQ
          ))
        )
      }

      "should find a single company" >> {
        Doo.companyCaseClass(intentHQ.id) must beRight(beSome(intentHQ))
      }

    }

    "insert a company" >> {

      "returns you the autogenerated id (if needed/wanted)" >> {
        val name = "Very Company"
        Doo.createCompany(name) must beRight[CompanyId]
        Doo.companyNames.exists(_.contains(name)) must beTrue
      }

    }

    "update the name of a company" >> {

      "should update the company name in the db" >> {
        val name = "So Company"
        val newName = "Much Company"
        Doo.createCompany(name) must beRight[CompanyId].which { id =>
          Doo.updateCompanyName(id, newName) must beRight(be_===(1))
          Doo.companyNames must beRight.which { names =>
            names must contain(newName)
            names must not(contain(name))
          }
        }
      }

    }

  }

  "basic join between companies and job offers" >> {

    "as tuples" >> {
      Doo.jobOffersTuples must beRight { offers: List[(String, String)] =>
        offers must contain(("Intent HQ", "Software Engineer"))
      }
    }

    "as case classes" >> {
      Doo.jobOffersCaseClasses must beRight { offers: List[JobOffer] =>
        offers must contain(
          JobOffer(JobOfferId(1), "Software Engineer",
            "We would like to grow our team and are looking for somebody passionate about technology, " +
            "sensitive about client needs and that collaborates well in a team environment.",
            Company(CompanyId(1), "Intent HQ", Some("https://www.intenthq.com")))
        )
      }
    }

  }

  "transactions" >> {

    "inserting a company and its first job offer in the same transaction" >> {
      val companyName = "New Company"
      val offerSummary = "Job offer summary"
      val offerDescription = "Job offer description"
      Doo.createJobOffer(companyName, offerSummary, offerDescription) must beRight.which { case (cid, joid) =>
        Doo.jobOffersCaseClasses must beRight.which { offers =>
          offers.find(_.id == joid) must beSome(
            JobOffer(joid, offerSummary, offerDescription, Company(cid, companyName, None))
          )
        }
      }
    }

  }

  "other interesting things" >> {
//    import doobie.imports._

    "setting null in a non nullable parameter throws an exception!" >> {
      Doo.createCompany(null) must throwAn[NonNullableParameter]
    }

    "asking for a single result if the query returns more than one row returns an error" >> {
//      Doo.Q.companyCaseClasses.option.transact(dbContext.xa).attempt
//        .unsafePerformIO must beLeft(UnexpectedContinuation)
//      Doo.Q.companyCaseClasses.unique.transact(dbContext.xa).attempt
//        .unsafePerformIO must beLeft(UnexpectedContinuation)
      pending
    }

    "asking for a unique result if the query returns an empty resultset returns an error" >> {
//      Doo.Q.companyCaseClass(CompanyId(-1000)).unique.transact(dbContext.xa).attempt
//        .unsafePerformIO must beLeft(UnexpectedEnd)
      pending
    }

    "how about little bobby tables?" >> {
      Doo.createCompany("Robert'); DROP TABLE companies;--") must beRight[CompanyId]
    }
  }

}
