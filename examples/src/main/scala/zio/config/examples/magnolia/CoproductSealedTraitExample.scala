package zio.config.examples.magnolia

import zio.config._
import zio.config.magnolia.DeriveConfigDescriptor._
import zio.config.ConfigSource
import zio.config.PropertyTree._
import zio.config.examples.typesafe.EitherImpureOps

object CoproductSealedTraitExample extends App with EitherImpureOps {

  sealed trait X

  case object A                extends X
  case object B                extends X
  case object C                extends X
  case class D(detail: Detail) extends X
  case class Detail(firstName: String, lastName: String, region: Region)
  case class Region(suburb: String, city: String)

  assert(read(descriptor[X] from ConfigSource.fromMap(Map("x" -> "a"))) == Right(A))
  assert(read(descriptor[X] from ConfigSource.fromMap(Map("x" -> "b"))) == Right(B))
  assert(read(descriptor[X] from ConfigSource.fromMap(Map("x" -> "c"))) == Right(C))
  assert(
    read(
      descriptor[X] from ConfigSource.fromMap(
        Map(
          "x.detail.firstName"     -> "ff",
          "x.detail.lastName"      -> "ll",
          "x.detail.region.suburb" -> "strath",
          "x.detail.region.city"   -> "syd"
        )
      )
    ) == Right(
      D(Detail("ff", "ll", Region("strath", "syd")))
    )
  )

  assert(
    write(descriptor[X], D(Detail("ff", "ll", Region("strath", "syd")))) ==
      Right(
        Record(
          Map(
            "x" -> Record(
              Map(
                "detail" -> Record(
                  Map(
                    "region"    -> Record(Map("city" -> Leaf("syd"), "suburb" -> Leaf("strath"))),
                    "lastName"  -> Leaf("ll"),
                    "firstName" -> Leaf("ff")
                  )
                )
              )
            )
          )
        )
      )
  )

  assert(write(descriptor[X], A) == Right(Record(Map("x" -> Leaf("a")))))
  assert(write(descriptor[X], B) == Right(Record(Map("x" -> Leaf("b")))))
  assert(write(descriptor[X], C) == Right(Record(Map("x" -> Leaf("c")))))
}
