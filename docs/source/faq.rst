Frequently Asked Questions
==========================

How can I get more quota?
-------------------------

If your application is in a humanitarian, academic, governmental, or not-for-profit organisation, you may be eligible for the *collaborative plan*.

You can request an upgrade to the *collaborative plan* in the `HeiGIT account dashboard <https://account.heigit.org/>`_.


When does my quota reset?
-------------------------

All remaining quota is shown in the `HeiGIT account dashboard <https://account.heigit.org/>`_.

The *daily limit* is reset after 24h, starting from the first time you request anything. Thus, your 24h-window might shift over the days.

The *minutely limit* is enforced as a sliding window, meaning that any consecutive period of 60 seconds may only contain 15 requests.

If you run into the *daily limit*, you will receive a ``403 - Forbidden`` HTTP error with the message "Quota Exceeded".

If you run into the *minutely limit*, you will receive a ``429 - Too many requests`` HTTP error with the message "Rate Limit Exceeded".

The remaining daily quota can be checked programmatically by comparing the headers ``x-ratelimit-remaining`` and ``x-ratelimit-reset``.
