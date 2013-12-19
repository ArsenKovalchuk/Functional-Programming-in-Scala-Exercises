package com.github.robertberry.fpis

import scala.util.matching.Regex

object Chapter9 {
  trait Parsers[ParseError, Parser[+_]] { self =>
    def or[A](s1: Parser[A], s2: Parser[A]): Parser[A]

    implicit def string(s: String): Parser[String]

    implicit def operators[A](p: Parser[A]) = ParserOps[A](p)

    implicit def asStringParser[A](a: A)(implicit f: A => Parser[String]): ParserOps[String] = ParserOps(f(a))

    case class ParserOps[A](p: Parser[A]) {
      def |[B >: A](p2: Parser[B]): Parser[B] = self.or(p,p2)

      def or[B >: A](p2: => Parser[B]): Parser[B] = self.or(p,p2)

      def map[AA >: A, B](f: AA => B): Parser[B] = self.map(p)(f)

      def flatMap[AA >: A, B](f: AA => Parser[B]): Parser[B] = self.flatMap(p)(f)

      def **[B](p2: Parser[B]): Parser[(A, B)] = self.product(p, p2)
    }

    def map[A, B](a: Parser[A])(f: A => B): Parser[B]

    def char(c: Char): Parser[Char] =
      string(c.toString) map (_.charAt(0))

    def succeed[A](a: A): Parser[A] =
      string("") map (_ => a)

    def slice[A](p: Parser[A]): Parser[String]

    def product[A, B](p: Parser[A], p2: Parser[B]): Parser[(A, B)]

    /** Exercise 1
      *
      * Using product, implement map2, then use this to implement many1 in terms of many
      */
    def map2[A, B, C](p: Parser[A], p2: Parser[B])(f: (A, B) => C): Parser[C] = product(p, p2) map {
      case (a, b) => f(a, b)
    }

    def many1[A](p: Parser[A]): Parser[List[A]] = map2(p, many(p))(_ :: _)

    /** Exercise 2
      *
      * Try coming up with laws to specify the behaviour of product
      *
      * product(a, ()).map(fst) == a
      * product((), a).map(snd) == a
      */

    /** Exercise 3
      *
      * Define many in terms of or, map2 and succeed
      */
    def many[A](p: Parser[A]): Parser[List[A]] = or(map2(p, many(p))(_ :: _), succeed(Nil))

    /** Exercise 4
      *
      * Using map2 and succeed, implement listOfN combinator
      */
    def listOfN[A](n: Int, p: Parser[A]): Parser[List[A]] = if (n < 1)
        succeed(Nil)
      else
        map2(p, listOfN(n - 1, p))(_ :: _)

    def flatMap[A, B](p: Parser[A])(f: A => Parser[B]): Parser[B]

    implicit def regex(r: Regex): Parser[String]

    /** Exercise 6
      *
      * Using flatMap and any other combinators, write a parser for a single digit and then that many 'a' characters
      * following
      */
    val nAs = """\\d""".r flatMap {
      case IntegerString(n) => succeed(n) ** listOfN(n.toInt, char('a'))
    }

    /** Exercise 7
      *
      * Implement product and map2 in terms of flatMap
      */

  }
}
