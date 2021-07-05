# ARCHIVED - NO LONGER IN DEVELOPMENT

This project was originally a small hobby project of mine, mostly as a summer challenge, but also as the protocol driver for a very specific and purpose-built TeamSpeak bot. Our team has since moved platforms multiple times, so both the bot and protocol are no longer in development. 

This project was built on the brilliant work of the [ReSpeak](https://github.com/ReSpeak) team, consider checking out their projects (which are still active!):

* [ReSpeak/tsdeclarations](https://github.com/ReSpeak/tsdeclarations) - Protocol specification
* [ReSpeak/tsclientlib](https://github.com/ReSpeak/tsclientlib) - Rust implementation of the protocol
* [Splamy/TS3AudioBot](https://github.com/Splamy/TS3AudioBot) - C# implementation of the protocol

For a more up-to-date Java protocol imlpementation, which was also based off of the ReSpeak team's work, and actually has some documentation and examples, consider checking out ts3j:

* [Manevolent/ts3j](https://github.com/Manevolent/ts3j) - An open-source Java Teamspeak 3 client library using the TS3 full client protocol

<hr />

This library (Lemmmy/ts3protocol) will no longer receive updates or support, but anybody interested is more than welcome to fork it and continue its development. I apologise in advance that this was made solely for my own purposes, so the design of the library is not necessarily applicable to all applications, and there is no documentation.

<hr />

# ts3protocol

This is a pure Java implementation of the TeamSpeak3 client protocol, designed for private in-server bots. 
This is **not** a server implementation.

The project is based on the work of the [ReSpeak](https://github.com/ReSpeak) team.

* [ReSpeak/tsdeclarations](https://github.com/ReSpeak/tsdeclarations) - Protocol specification
* [ReSpeak/tsclientlib](https://github.com/ReSpeak/tsclientlib) - Rust implementation of the protocol
* [Splamy/TS3AudioBot](https://github.com/Splamy/TS3AudioBot) - C# implementation of the protocol

## Features

* Connection with persisted identity
* Voice encoding/decoding support (Speex, Opus)
* Receive/send messages
* User, channel and virtualserver management

### TODO

* Reconnect on connection lost
* Packet reordering
* Import identities from official TeamSpeak3 client
* CELT Mono support
* Files, icons and avatars support
* Higher level utilities for voice encoding
* BBCode utilities
* User/channel/group permission management support

## Known issues

* Sometimes, the low-level handshake fails. The official client deals with this by just attempting reconnection.
* There is no packet re-ordering, so packets can get decrypted incorrectly (rare occurrence).
* On occasion, there may be QuickLZ decompression errors. This could be packets decrypting incorrectly, too.

## Building and running the test client

This project is built with [Gradle](https://gradle.org/).

```sh 
git clone https://github.com/Lemmmy/ts3protocol
cd ts3protocol
./gradlew run
```

## Disclaimer

This project and its developers are not affiliated with TeamSpeak Systems GmbH in any way. It was not created
with intention of harming TeamSpeak or their product. As TeamSpeak's business model is their servers, we will
not publish any server-related code.

## Licence

This project is licenced under the [MIT Licence](https://github.com/Lemmmy/ts3protocol/blob/master/LICENCE).
