<a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" /></a>
<br/>

Unleashing the Power of HTTP APIs: The Http4s Library 
================================================

Once we learnt how to define Monoids, Semigroups, Applicative, Monads, and so on, it's time to 
understand how to use them to build a production-ready application. Nowadays, Many applications 
exposes APIs over an HTTP channel. So, it's worth to spend some time to study libraries implementing
such use case.

If we learnt the basics of functional programming using the Cats ecosystem, it's straightforward to
choose the `http4s` library to implement HTTP endpoints. Let's see how to implement some aspects of
HTTP APIs using it.

## Introduction

First thing first, we need a good example to work with. In this case, we need a domain model easy 
enough to allow us focusing on the exposition of simple APIs. So, imagine we've just finished 
watching the Snyder Cut of the Justice League movie, and we are very excited about the film. We
really want to tell the world how much we enjoyed the movie, and then we decide to build our 
personal "Rotten Tomatoes" application (ugh...).

As we are very experienced developers, we chose to start coding from the backend. Hence, we began in
defining the resources we need in terms of domain.

For sure, we need a `Movie`, and a movie has a `Director`, many `Actors`, a `Synopsis`, and finally 
a `Review`.