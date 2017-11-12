# Redirect #

A client that logs 301 and 302 redirects receive when navigating to a domain.

This is also a proof-of-concept to investigate different ways of implementing the following requirements:

1. Handle errors gracefully - Not NonFatal errors thrown
2. If there are any errors along the way locations that were retrieve prior should not be lost. We still need a record of what succeeded before the error.
3. The final result should be a list of domains that were navigated to until either a successful result or an error was received.

  http://domainA.xyz
  https://domain.com
  https://aaa.domain.com

  ------

  UnknownHostException - Could not find host aaa.domain.com.

4. Any errors should be logged out as well.
5. Should be stack safe.
6. Ideally each location should be logged out incrementally. It might be a bit confusing if nothing happens in the output for a while before the full list of redirects are dumped out. [optional]

