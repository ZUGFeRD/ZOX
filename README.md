# ZOX
Zugferd Over Xmpp: Proof of concept for a ZUGFeRD protocol

## Prerequisites

You'll need a XMPP Server like [OpenFire] (https://www.igniterealtime.org/projects/openfire/).
Currently, both server name (jochens-air@fritz.box) and user credentials ("zox" with password "zox") are still hardcoded.
.
Additionally, a second user is required and use a xmpp client like psi to login.
Have those users roster each other e.g. with the openfire admin console at :9090 it's important that both users are on each other's roster, subscribed to each others online status and may send each other messages.

## Build
Build `mvn clean compile assembly:single` and start `java -jar target/zox-0.0.1-SNAPSHOT-jar-with-dependencies.jar` should start the software and login the zox user. Now use psi to send this user a ZUGFeRD file like the [mustang sample](www.mustangproject.org/MustangGnuaccountingBeispielRE-20170509_505.pdf). The zox user will accept, receive and parse the file and reply with the invoice amount it parsed.
